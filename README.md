# RAG Project V2

## Introduction

This project is a Retrieval-Augmented Generation (RAG) application designed to process and query documents using a multi-service architecture. It is an improved and extended version of [My RAG project V1](https://github.com/sebampuero/spring_webflux_ws).

The system consists of the following components:

- **Frontend**: A React-based user interface built with Vite.
- **Backend**: A Java Spring Boot application that orchestrates requests and manages communication between the frontend and the RAG engine.
- **Ragchain**: A Python-based RAG engine powered by FastAPI and LangChain. It handles document processing, vector storage (ChromaDB), and integration with LLMs such as MistralAI and DeepSeek.
- **Cache/Session Store**: Redis is utilized for storing sessions (LangChain memory). It will be used for Celery workers in the future.

## How to build

The project is containerized with Docker.

### Prerequisites

- Docker
- Docker Compose

### Running the application

1. Clone the repository.
2. Ensure you have the necessary environment variables configured in the respective `.env` files within `backend/`, `frontend/`, and `ragchain/`.
3. Run the following command from the root directory:

```bash
docker-compose up --build
```

The services will be available at:
- Frontend: `https://localhost:8443`
- Backend API: `http://localhost:8080`

### Manual build

#### Backend (Java)
Navigate to the `backend` directory and use Maven:
```bash
./mvnw clean install
./mvnw spring-boot:run
```

#### Ragchain (Python)
Navigate to the `ragchain` directory and use `uv` or `pip`:
```bash
uv sync
uv run python main.py
```

#### Frontend (React)
Navigate to the `frontend` directory and use npm:
```bash
npm install
npm run dev
```

## Open TODOs

- Add testing
- Include celery workers to ingest daily
- Scalability with external session data store
