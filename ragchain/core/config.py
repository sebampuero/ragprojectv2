from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    PROJECT_NAME: str = "ragchain"
    REDIS_URL: str = "redis://localhost:6379/0"
    S3_ACCESS_KEY_ID: str
    S3_SECRET_ACCESS_KEY: str
    S3_BUCKET: str
    S3_REGION: str = "eu-central-1"
    S3_DOC_PREFIX: str = "doc"
    ROOT_LOG_LEVEL: str = "DEBUG"
    APP_LOG_LEVEL: str = "DEBUG"
    LOG_FORMAT: str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    LOG_DATE_FORMAT: str = "%Y-%m-%d %H:%M:%S"

    BEAT_SCHEDULE_REFRESH_RAG_DOCUMENTS_EVERY_SECONDS: int = 60 * 60 * 24

    SPLITTER_CHUNK_SIZE: int = 2000
    SPLITTER_CHUNK_OVERLAP: int = 200
    CHROMA_PERSIST_DIRECTORY: str

    VECTOR_STORE_COLLECTION_NAME: str

    MISTRAL_EMBED_MODEL: str = "mistral-embed"
    MISTRAL_API_KEY: str
    
    DEEPSEEK_API_KEY: str
    
    model_config = SettingsConfigDict(
        env_file=".env",
        env_ignore_empty=True,
        extra="ignore",
    )

settings = Settings()
