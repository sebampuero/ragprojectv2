from langchain_chroma import Chroma
from langchain_mistralai import MistralAIEmbeddings
from ragchain.core.config import settings

def get_vector_store() -> Chroma:
    return Chroma(
        collection_name=settings.VECTOR_STORE_COLLECTION_NAME,
        embedding_function=MistralAIEmbeddings(
            model=settings.MISTRAL_EMBED_MODEL,
            api_key=settings.MISTRAL_API_KEY,
        ),
        persist_directory=settings.CHROMA_PERSIST_DIRECTORY,
    )