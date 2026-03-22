from fastapi import FastAPI
from ragchain.api.router import router
from contextlib import asynccontextmanager
from ragchain.services.chat_service import ChatService

@asynccontextmanager
async def lifespan(app: FastAPI):
    ChatService()
    yield

app = FastAPI(title="Chatapp RAG", lifespan=lifespan)

app.include_router(router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
