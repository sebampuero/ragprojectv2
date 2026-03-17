from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    REDIS_URL: str = "redis://localhost:6379/0"
    S3_ACCESS_KEY_ID: str
    S3_SECRET_ACCESS_KEY: str
    S3_BUCKET: str
    S3_REGION: str = "eu-central-1"
    S3_DOC_PREFIX: str = "doc"

    SPLITTER_CHUNK_SIZE: int = 2000
    SPLITTER_CHUNK_OVERLAP: int = 200

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
