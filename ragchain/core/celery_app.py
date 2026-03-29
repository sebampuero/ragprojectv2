from celery import Celery
from ragchain.core.config import settings

celery_app = Celery("rag_app")
celery_app.conf.update(
    broker_url=settings.REDIS_URL,
    result_backend=settings.REDIS_URL,
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
    imports=("ragchain.tasks.maintenance",),
    beat_schedule={
        "refresh-rag-documents-every": {
            "task": "ragchain.tasks.maintenance.refresh_rag_documents",
            "schedule": settings.BEAT_SCHEDULE_REFRESH_RAG_DOCUMENTS_EVERY_SECONDS,
        },
    },
)