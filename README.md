# 🏦 SmartFinance - Core Backend API (Java 21 & Spring Boot 3.5)

SmartFinance is a sophisticated **Personal Finance Management** ecosystem, leveraging the power of **Java 21**, **Spring Boot 3.5**, and **Generative AI** to deliver real-time financial tracking and actionable spending insights. Engineered for high performance, security, and scalability.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring_Boot-3.5.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![TiDB Serverless](https://img.shields.io/badge/Database-TiDB_Serverless-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://tidbcloud.com)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/API_Docs-Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://smartfinance-be.onrender.com/api-docs)

---

## 📸 Visual Preview

<p align="center">
  <img src="https://via.placeholder.com/800x450.png?text=SmartFinance+Dashboard+Preview" width="800" alt="Dashboard Preview">
  <br>
  <em>Figure 1: AI-Powered Financial Dashboard with real-time analytics.</em>
</p>

<p align="center">
  <img src="https://via.placeholder.com/400x250.png?text=AI+Financial+Advisor" width="400" alt="AI Chat Preview">
  <img src="https://via.placeholder.com/400x250.png?text=Mobile+Responsive+UI" width="400" alt="Mobile Preview">
  <br>
  <em>Figure 2: Generative AI Advisor (Left) & Mobile Responsive Design (Right).</em>
</p>

---

## 📖 Project Overview

SmartFinance Core API acts as the central intelligence of a finance tracking application. It solves the complexity of modern financial management by providing an automated, secure, and insightful way to track transactions, manage budgets, and chat with an AI advisor.

### 🎯 The Problem it Solves:
- **Invisible Spending**: Users often lose track of where their money goes.
- **Complexity in Budgeting**: Manually calculating limits against actual spending is tedious.
- **Lack of Advice**: Standard apps just show tables; SmartFinance uses **AI** to provide actual advice based on your data.

---

## 💎 Enterprise-Grade Features

*   **🛡️ Stateless JWT Authentication**: Implemented a robust security layer with **Spring Security 6**. Optimized with **In-Memory Authorization**—user roles and IDs are carried in token claims to achieve zero database-bound overhead for standard API requests.
*   **🤖 AI-Powered Financial Insights**: Integrated with **Google Gemini (OpenAI-compatible)** via a vendor-agnostic adapter. Features **Smart Cache Invalidation**: Financial advice is automatically marked as `stale` whenever a transaction is modified, ensuring advice is always data-accurate while minimizing API latency and costs.
*   **📈 Real-time Analytics Dashboard**: Custom JPQL aggregation queries provide instantaneous data for complex financial visualizations (Category distributions, Monthly trends).
*   **📂 Professional Data Exports**: Support for high-volume **CSV** and **PDF** exports with **Vietnamese Unicode** support. Designed with a **Data Streaming Architecture** to prevent Out-of-Memory (OOM) errors during heavy traffic.
*   **🐳 Container-First Development**: Fully dockerized with a multi-stage **Dockerfile** for minimal image footprints and optimized production runtime.
*   **📜 API Standardization**: Uses a **Generic Response Wrapper** (`ApiResponse<T>`) and **Global Exception Handling** to ensure consistent API behavior across all endpoints.

---

## 🏗 Project Architecture (N-Tier)

The project follows a clean N-tier architecture, ensuring separation of concerns and high maintainability.

```text
smartfinance-be/
├── src/main/java/com/smartfinance/
│   ├── config/             # Configuration (Security, JWT, Swagger)
│   ├── controller/         # REST Controllers (API Endpoints)
│   ├── dto/                # Data Transfer Objects (Request/Response)
│   ├── entity/             # JPA Entities (Database Mapping)
│   ├── enums/              # Domain-specific Enums
│   ├── exception/          # Global Exception Handling & ErrorCodes
│   ├── mapper/             # MapStruct Interfaces (Entity ↔ DTO)
│   ├── repository/         # Spring Data JPA Repositories
│   ├── security/           # JWT & Spring Security Implementation
│   └── service/            # Business Logic (Interface + Impl)
├── Dockerfile              # Prodcution Build Script
└── README.md               # You are here
```

---

## 🚀 Key Technical Stack

- **Lomok**: To eliminate boilerplate code.
- **MapStruct**: For high-performance, type-safe bean mapping (Compile-time generation).
- **SpringDoc-OpenAPI**: Automated Swagger UI documentation.
- **Java 21 Records**: For immutable, concise request payloads.
- **TiDB Serverless**: NewSQL distributed database for permanent cloud storage.

---

## 📖 API Documentation

The API's interactive documentation is available via **Swagger UI** once the application is running.

- **Endpoint**: `/swagger-ui/index.html`
- **Swagger Docs**: `/v3/api-docs`

---

## 🔧 Local Setup & Installation

### 1. Prerequisites
- **JDK 21** & **Maven 3.x**
- **MySQL 8** (or TiDB Serverless Connection)

### 2. Environment Variables
Create a `.env` file in the root directory or configure your IDE with the following:
```env
SPRING_DATASOURCE_URL=jdbc:mysql://your-host:4000/db_name
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
JWT_SECRET=your_32_character_base64_secret
GEMINI_API_KEY=your_gemini_api_key
CORS_ALLOWED_ORIGINS=http://localhost:5173
```

### 3. Build & Run
**Using Maven:**
```bash
mvn clean install -Dmaven.test.skip=true
mvn spring-boot:run
```

**Using Docker:**
```bash
docker build -t smartfinance-api .
docker run -p 8080:8080 --env-file .env smartfinance-api
```

---

## 📧 Contact & Contribution

**Tuan Anh** - [GitHub](https://github.com/TuanAnhVu81) - [Email](youremail@example.com)

"Developing clean code that empowers financial freedom."
