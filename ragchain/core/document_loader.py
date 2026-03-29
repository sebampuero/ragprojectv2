from ragchain.core.config import settings
from langchain_community.document_loaders import S3DirectoryLoader


def get_document_loader() -> S3DirectoryLoader:
    return S3DirectoryLoader(
        bucket=settings.S3_BUCKET,
        prefix=settings.S3_DOC_PREFIX,
        aws_access_key_id=settings.S3_ACCESS_KEY_ID,
        aws_secret_access_key=settings.S3_SECRET_ACCESS_KEY
    )