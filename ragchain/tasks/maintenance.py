from ragchain.core.celery_app import celery_app
from ragchain.services.ingestion import ingest
from ragchain.core.document_loader import get_document_loader
from ragchain.core.vector_store import get_vector_store


@celery_app.task(name="ragchain.tasks.maintenance.refresh_rag_documents")
def refresh_rag_documents():
    loader = get_document_loader()
    vector_store = get_vector_store()
    ingest(vector_store, loader)