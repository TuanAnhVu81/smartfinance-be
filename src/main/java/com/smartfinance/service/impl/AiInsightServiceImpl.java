package com.smartfinance.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfinance.dto.request.AiChatRequest;
import com.smartfinance.dto.response.AiInsightResponse;
import com.smartfinance.dto.response.CategoryChartProjection;
import com.smartfinance.entity.AiInsight;
import com.smartfinance.entity.User;
import com.smartfinance.enums.CategoryType;
import com.smartfinance.exception.AppException;
import com.smartfinance.exception.ErrorCode;
import com.smartfinance.repository.AiInsightRepository;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.repository.UserRepository;
import com.smartfinance.service.AiInsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightServiceImpl implements AiInsightService {

    private final AiInsightRepository aiInsightRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Use the dedicated Gemini RestTemplate bean (with custom timeouts)
    @Qualifier("geminiRestTemplate")
    private final RestTemplate geminiRestTemplate;

    @Value("${app.ai.gemini-api-key}")
    private String geminiApiKey;

    @Value("${app.ai.gemini-api-url}")
    private String geminiApiUrl;

    @Value("${app.ai.model}")
    private String aiModel;

    @Override
    @Transactional
    public AiInsightResponse getInsight(Long userId, Integer month, Integer year) {
        return aiInsightRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .map(insight -> {
                    // Cache hit: if data is still fresh, return immediately (zero latency)
                    if (!insight.getIsOutdated()) {
                        log.debug("Returning cached AI insight for user={} month={} year={}", userId, month, year);
                        return toResponse(insight);
                    }
                    // Cache stale: fetch fresh from Gemini, then update DB
                    return generateAndSave(insight, userId, month, year);
                })
                // Cache miss: no record yet → generate from scratch
                .orElseGet(() -> generateAndSave(null, userId, month, year));
    }

    @Override
    @Transactional
    public AiInsightResponse refreshInsight(Long userId, Integer month, Integer year) {
        // Force-refresh: bypass freshness flag entirely
        AiInsight existing = aiInsightRepository.findByUserIdAndMonthAndYear(userId, month, year).orElse(null);
        return generateAndSave(existing, userId, month, year);
    }

    @Override
    @Transactional
    public void markOutdated(Long userId, Integer month, Integer year) {
        // Called by TransactionService whenever user mutates transaction data
        aiInsightRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .ifPresent(insight -> {
                    insight.setIsOutdated(true);
                    aiInsightRepository.save(insight);
                    log.debug("Marked AI Insight outdated for user={} month={} year={}", userId, month, year);
                });
    }

    @Override
    public String chatWithAi(Long userId, AiChatRequest request) {
        log.info("AI Chat started - User: {}, Question: '{}'", userId, request.message());
        
        String financialContext = buildContext(userId, request.month(), request.year());
        String systemInstruction = "Bạn là cố vấn tài chính chuyên nghiệp. " +
                "Dưới đây là tóm tắt dữ liệu của người dùng trong tháng: \n" + financialContext +
                "\nHãy trả lời câu hỏi của người dùng ngắn gọn, hữu ích và bằng tiếng Việt.";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(geminiApiKey);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", aiModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemInstruction),
                            Map.of("role", "user", "content", request.message())
                    )
            ));

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String raw = geminiRestTemplate.postForObject(geminiApiUrl, entity, String.class);
            JsonNode root = objectMapper.readTree(raw);
            return root.at("/choices/0/message/content").asText("Xin lỗi, tôi không thể xử lý câu hỏi này lúc này.");


        } catch (RestClientException e) {
            log.error("AI Service Error (REST): {}", e.getMessage());
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("AI Unexpected Error: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    // --- Internal logic ---

    private AiInsightResponse generateAndSave(AiInsight existing, Long userId, Integer month, Integer year) {
        String prompt = buildPrompt(userId, month, year);
        String aiText = callGemini(prompt);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        AiInsight insight = (existing != null) ? existing : AiInsight.builder()
                .user(user)
                .month(month)
                .year(year)
                .build();

        insight.setPromptSummary(prompt);
        insight.setAiResponse(aiText);
        insight.setIsOutdated(false);
        aiInsightRepository.save(insight);

        log.debug("AI Insight generated and cached for user={} month={} year={}", userId, month, year);
        return toResponse(insight);
    }

    private String buildContext(Long userId, Integer month, Integer year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.calculateTotalAmountByTypeAndPeriod(
                userId, CategoryType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.calculateTotalAmountByTypeAndPeriod(
                userId, CategoryType.EXPENSE, startDate, endDate);

        List<CategoryChartProjection> categories = transactionRepository.getCategoryBreakdownByPeriod(
                userId, CategoryType.EXPENSE, startDate, endDate);

        String breakdown = categories.stream()
                .map(c -> String.format("  - %s: %,.0f VND", c.getCategoryName(), c.getTotalAmount()))
                .collect(Collectors.joining("\n"));

        return String.format("""
                - Total Income: %,.0f VND
                - Total Expense: %,.0f VND
                - Expense Breakdown:
                %s""", totalIncome, totalExpense, breakdown.isBlank() ? "  (No expense transactions)" : breakdown);
    }

    // Build prompt with actual financial data from DB for this user/month
    private String buildPrompt(Long userId, Integer month, Integer year) {
        String context = buildContext(userId, month, year);

        return String.format("""
                You are a personal finance expert. Here is the financial data summary for month %d/%d:
                %s

                Analyze the data and provide 3-5 concise, practical financial tips in Vietnamese.
                """, month, year, context);
    }

    // Call API using OpenAI Compatibility Layer standard for Vendor Agnosticism
    private String callGemini(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(geminiApiKey); // Use standard OpenAI Bearer Token

            // Build request body according to the OpenAI Chat Completions Standard
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", aiModel, // Dynamically use model from config (to avoid 404/429 on different accounts)
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a professional and dedicated financial advisor."),
                            Map.of("role", "user", "content", prompt)
                    )
            ));

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String raw = geminiRestTemplate.postForObject(geminiApiUrl, entity, String.class);

            // Parse response matching OpenAI's schema: choices[0].message.content
            JsonNode root = objectMapper.readTree(raw);
            return root.at("/choices/0/message/content").asText("Unable to analyze data at this moment.");

        } catch (RestClientException e) {
            log.error("Gemini API call failed (timeout or network): {}", e.getMessage());
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        } catch (Exception e) {
            log.error("Unexpected error parsing Gemini response: {}", e.getMessage());
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private AiInsightResponse toResponse(AiInsight insight) {
        return AiInsightResponse.builder()
                .id(insight.getId())
                .month(insight.getMonth())
                .year(insight.getYear())
                .aiResponse(insight.getAiResponse())
                .isOutdated(insight.getIsOutdated())
                .updatedAt(insight.getUpdatedAt())
                .build();
    }
}
