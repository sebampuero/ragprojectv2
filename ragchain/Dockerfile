FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    libxcb1 \
    poppler-utils \
    tesseract-ocr \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app && chown -R 1000:1000 /app
WORKDIR /app

RUN mkdir -p /data/chroma && chown -R 1000:1000 /data/chroma

USER 1000

ENV UV_COMPILE_BYTECODE=1
ENV PYTHONPATH=/app
ENV PATH="/app/.venv/bin:$PATH"
ENV NUMBA_CACHE_DIR=/tmp/numba_cache
ENV UV_CACHE_DIR=/tmp/uv_cache

COPY --chown=1000:1000 pyproject.toml uv.lock ./

RUN uv sync --frozen --no-dev --no-install-project

COPY --chown=1000:1000 . /app/ragchain

EXPOSE 8000

CMD ["uvicorn", "ragchain.main:app", "--host", "0.0.0.0", "--port", "8000"]
