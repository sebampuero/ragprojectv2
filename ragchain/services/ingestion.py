import logging
from ragchain.core.config import settings
from langchain_core.vectorstores import VectorStore
from langchain_core.document_loaders import BaseLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter

logger = logging.getLogger(__name__)

def ingest(vector_store: VectorStore, loader: BaseLoader):
    logger.debug(f"Using loader: {loader} and vector store: {vector_store}")

    docs = loader.load()
    logger.debug(f"Loaded {len(docs)} documents")

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=settings.SPLITTER_CHUNK_SIZE,
        chunk_overlap=settings.SPLITTER_CHUNK_OVERLAP,
        add_start_index=True,
    )

    all_splits = []
    all_ids = []

    for doc in docs:
        source = doc.metadata.get("source")
        if not source:
            raise ValueError("Document metadata must include 'source'")

        splits = text_splitter.split_documents([doc])

        for i, split in enumerate(splits):
            split.metadata["source"] = source
            all_splits.append(split)
            all_ids.append(f"{source}::{i}")

    vector_store.delete(ids=all_ids)
    logger.debug(f"Deleted {len(all_ids)} existing chunk IDs")

    vector_store.add_documents(documents=all_splits, ids=all_ids)
    logger.debug(f"Added {len(all_splits)} chunks to vector store")
