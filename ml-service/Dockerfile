FROM python:3.12-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    MODEL_PATH=/app/modelo_rf_recomendado_api.joblib

COPY ml-service/requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY modelo_rf_recomendado_api.joblib ./modelo_rf_recomendado_api.joblib
COPY ml-service/predictor.py ./predictor.py
COPY ml-service/app.py ./app.py

EXPOSE 8000

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
