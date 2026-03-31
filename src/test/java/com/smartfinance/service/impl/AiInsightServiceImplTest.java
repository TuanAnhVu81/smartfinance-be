package com.smartfinance.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfinance.dto.response.AiInsightResponse;
import com.smartfinance.entity.AiInsight;
import com.smartfinance.entity.User;
import com.smartfinance.repository.AiInsightRepository;
import com.smartfinance.repository.TransactionRepository;
import com.smartfinance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AiInsightServiceImplTest {

    @Mock
    private AiInsightRepository aiInsightRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestTemplate geminiRestTemplate;

    @InjectMocks
    private AiInsightServiceImpl aiInsightService;

    private User testUser;
    private AiInsight testInsight;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiInsightService, "geminiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiInsightService, "geminiApiUrl", "http://test-url");
        ReflectionTestUtils.setField(aiInsightService, "aiModel", "test-model");

        testUser = new User();
        testUser.setId(1L);

        testInsight = new AiInsight();
        testInsight.setId(1L);
        testInsight.setUser(testUser);
        testInsight.setMonth(10);
        testInsight.setYear(2023);
        testInsight.setAiResponse("AI Insight Text");
        testInsight.setIsOutdated(false);
    }

    @Test
    void getInsight_CacheHit_Success() {
        // Arrange
        when(aiInsightRepository.findByUserIdAndMonthAndYear(1L, 10, 2023)).thenReturn(Optional.of(testInsight));

        // Act
        AiInsightResponse response = aiInsightService.getInsight(1L, 10, 2023);

        // Assert
        assertNotNull(response);
        assertEquals("AI Insight Text", response.getAiResponse());
        verify(aiInsightRepository, times(1)).findByUserIdAndMonthAndYear(1L, 10, 2023);
        verify(geminiRestTemplate, times(0)).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    void markOutdated_Success() {
        // Arrange
        when(aiInsightRepository.findByUserIdAndMonthAndYear(1L, 10, 2023)).thenReturn(Optional.of(testInsight));

        // Act
        aiInsightService.markOutdated(1L, 10, 2023);

        // Assert
        assertTrue(testInsight.getIsOutdated());
        verify(aiInsightRepository, times(1)).save(testInsight);
    }
}
