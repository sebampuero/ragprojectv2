import logging
from ragchain.core.config import settings
from langchain_core.vectorstores import VectorStore
from langchain_core.document_loaders import BaseLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter

logger = logging.getLogger(__name__)

def ingest(vector_store: VectorStore, loader: BaseLoader):
    logger.debug(f"Using loader: {loader} and vector store: {vector_store}")

    vector_store.delete()
    logger.debug("Deleted all documents from vector store")

    docs = loader.load()
    logger.debug(f"Loaded {len(docs)} documents")

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=settings.SPLITTER_CHUNK_SIZE,
        chunk_overlap=settings.SPLITTER_CHUNK_OVERLAP,
        add_start_index=True,
    )
    logger.debug(f"Using text splitter: {text_splitter}")

    all_splits = text_splitter.split_documents(docs)
    logger.debug(f"Split into {len(all_splits)} chunks")

    vector_store.add_documents(documents=all_splits)
    logger.debug(f"Added {len(all_splits)} documents to vector store")
