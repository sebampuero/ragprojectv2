from .config import settings
from langchain_core.embeddings import Embeddings
from langchain_core.vectorstores import VectorStore
from langchain_core.document_loaders import BaseLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter

def ingest(vector_store: VectorStore, loader: BaseLoader):
    docs = loader.load()
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=settings.SPLITTER_CHUNK_SIZE,
        chunk_overlap=settings.SPLITTER_CHUNK_OVERLAP,
        add_start_index=True,
    )
    all_splits = text_splitter.split_documents(docs)
    vector_store.add_documents(documents=all_splits)
