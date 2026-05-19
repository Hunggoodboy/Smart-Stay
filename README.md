# 🏠 RoomNest – Smart Room Rental & Management Platform

> A full-stack web application that connects landlords and tenants through an intelligent, AI-powered rental experience.

---

## 📌 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [AI Integration](#ai-integration)
- [Security](#security)
- [Screenshots](#screenshots)
- [Contributing](#contributing)
- [License](#license)

---

## 📖 Overview

**RoomNest** is a smart room rental platform built for two types of users:

- **Landlords** – List rooms, manage tenants, and generate bills effortlessly.
- **Tenants** – Browse available rooms, chat with an AI consultant, and book viewings or submit rental requests automatically.

The platform is powered by a RAG-based AI assistant that understands your data and helps tenants find the perfect room through natural conversation.

---

## ✨ Features

### 🏡 For Landlords
- Create, update, and delete room listings with detailed property information
- Manage current tenants and their rental history
- Generate and track invoices/bills for each tenant
- Dashboard overview of occupancy and revenue

### 🔍 For Tenants
- Browse and search available rooms with filters (price, location, amenities, etc.)
- Chat with an AI consultant to get personalized room recommendations
- Allow the AI to automatically schedule viewing appointments
- Submit rental requests directly through the AI chat interface

### 🤖 AI-Powered Consultation (RAG)
- Room data is vectorized and stored for semantic search
- AI reads persistent chat history to maintain multi-turn conversational context
- AI agent can autonomously trigger actions (schedule appointments, send rental requests) based on user intent

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot |
| Database | PostgreSQL |
| Authentication | JWT (JSON Web Token) |
| AI Integration | RAG (Retrieval-Augmented Generation) |
| Vector Storage | pgvector / compatible vector store |
| Real-time (optional) | WebSocket |
| Frontend | React / Thymeleaf *(update as needed)* |
| Build Tool | Maven / Gradle |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Client (Browser)                  │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP / WebSocket
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot REST API                    │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ Auth Module │  │ Room Module  │  │ AI Module  │  │
│  │  (JWT)      │  │ Tenant/Bill  │  │ RAG + Chat │  │
│  └─────────────┘  └──────────────┘  └────────────┘  │
└──────────┬──────────────────────────────┬───────────┘
           │                              │
┌──────────▼──────┐             ┌─────────▼──────────┐
│   PostgreSQL    │             │   Vector Store      │
│  (Main Data)    │             │  (Embeddings/RAG)   │
└─────────────────┘             └────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 14+
- Maven or Gradle
- An AI API key (e.g., OpenAI, Google Gemini, etc.)

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/username/RoomNest.git
cd RoomNest

# 2. Configure environment variables (see below)
cp .env.example .env

# 3. Create the PostgreSQL database
psql -U postgres -c "CREATE DATABASE roomnest;"

# 4. Run the application
./mvnw spring-boot:run
```

The server will start at `http://localhost:8080`.

---

## ⚙️ Environment Variables

Create a `.env` file or configure `application.properties`:

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/roomnest
SPRING_DATASOURCE_USERNAME=your_db_user
SPRING_DATASOURCE_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_MS=86400000

# AI Configuration
AI_API_KEY=your_ai_api_key
AI_MODEL=gemini-pro   # or gpt-4, etc.
AI_EMBEDDING_MODEL=text-embedding-ada-002

# Vector Store
VECTOR_STORE_URL=your_vector_store_connection
```

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT |

### Rooms
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/rooms` | Get all available rooms |
| POST | `/api/rooms` | Create a new room (landlord) |
| PUT | `/api/rooms/{id}` | Update room details |
| DELETE | `/api/rooms/{id}` | Delete a room |

### Tenants & Billing
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tenants` | Get all tenants (landlord) |
| POST | `/api/bills` | Create a bill for a tenant |
| GET | `/api/bills/{tenantId}` | Get billing history |

### AI Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/ai/chat` | Send message to AI consultant |
| GET | `/api/ai/history` | Retrieve chat history |
| POST | `/api/ai/book` | AI creates a booking/appointment |

---

## 🧠 AI Integration

RoomNest uses **Retrieval-Augmented Generation (RAG)** to power its AI consultant:

1. **Vectorization** – All room data (descriptions, amenities, pricing) is embedded and stored in a vector database.
2. **Semantic Search** – When a user asks a question, the system retrieves the most relevant room data using vector similarity search.
3. **Context-Aware Responses** – The AI reads the full chat history to maintain conversational context across multiple turns.
4. **Agentic Actions** – The AI can autonomously:
    - Schedule a room viewing appointment
    - Submit a rental request on behalf of the user

```
User Message
     │
     ▼
Embed Query ──► Vector Search ──► Retrieve Relevant Rooms
                                          │
                              Chat History + Room Data
                                          │
                                          ▼
                                    AI Response
                               (+ Optional Action)
```

---

## 🔐 Security

- **JWT Authentication** – All protected routes require a valid Bearer token.
- **Role-Based Access Control** – `LANDLORD` and `TENANT` roles with separate permissions.
- **Password Encryption** – Passwords are hashed using BCrypt.
- **Stateless Sessions** – No server-side session storage; fully stateless REST API.

---

## 📸 Screenshots

> *(Add your screenshots here)*

| Room Listing | AI Chat | Landlord Dashboard |
|---|---|---|
| ![room]() | ![chat]() | ![dashboard]() |

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

```bash
# 1. Fork the repository
# 2. Create a feature branch
git checkout -b feature/your-feature-name

# 3. Commit your changes
git commit -m "feat: add your feature"

# 4. Push and open a Pull Request
git push origin feature/your-feature-name
```

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/username">Your Name</a>
</p>