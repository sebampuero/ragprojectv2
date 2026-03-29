from ragchain.core.celery_app import celery_app
from ragchain.services.ingestion import ingest
from ragchain.core.config import settings
from langchain_community.document_loaders import S3DirectoryLoader
from langchain_chroma import Chroma
from langchain_mistralai.embeddings import MistralAIEmbeddings


@celery_app.task(name="ragchain.tasks.maintenance.refresh_rag_documents")
def refresh_rag_documents():
    loader = S3DirectoryLoader(
        bucket=settings.S3_BUCKET,
        prefix=settings.S3_DOC_PREFIX,
        aws_access_key_id=settings.S3_ACCESS_KEY_ID,
        aws_secret_access_key=settings.S3_SECRET_ACCESS_KEY
    )
    vector_store = Chroma(
        collection_name=settings.VECTOR_STORE_COLLECTION_NAME,
        persist_directory=settings.CHROMA_PERSIST_DIRECTORY,
        embedding_function=MistralAIEmbeddings(
            model=settings.MISTRAL_EMBED_MODEL,
            api_key=settings.MISTRAL_API_KEY
        ),
    )
    ingest(vector_store, loader)