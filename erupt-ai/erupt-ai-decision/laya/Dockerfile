FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt server.py ./
RUN pip install --no-cache-dir -r requirements.txt

# Checkpoints are fetched from Hugging Face on first start; mount this to keep them
VOLUME /root/.cache/huggingface

EXPOSE 8000
CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "8000"]
