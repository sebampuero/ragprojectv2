from fastapi import FastAPI
from ragchain.api.router import router
from ragchain.core.config import settings
from ragchain.core.logging import setup_logging
from contextlib import asynccontextmanager
from ragchain.services.chat_service import ChatService


setup_logging(
    root_level=settings.ROOT_LOG_LEVEL,
    app_level=settings.APP_LOG_LEVEL,
    log_format=settings.LOG_FORMAT,
    date_format=settings.LOG_DATE_FORMAT,
)

@asynccontextmanager
async def lifespan(app: FastAPI):
    ChatService()
    yield

app = FastAPI(title="Chatapp RAG", lifespan=lifespan)

app.include_router(router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
