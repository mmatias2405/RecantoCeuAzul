"""
Microserviço REST (FastAPI) para Detecção Preditiva de Vazamentos Hídricos.

Mantém o modelo `modelo_rf_recomendado_api.joblib` carregado em memória e expõe:
  - GET /health  -> verificação de integridade e metadados do modelo
  - POST /predict -> inferência de vazamento a partir do perfil de demanda hídrica
"""

from __future__ import annotations

from contextlib import asynccontextmanager
from typing import Dict, List, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, ConfigDict, Field

from predictor import ModeloVazamentoPredictor


class ReferenciaDemanda(BaseModel):
    mu_med: float = Field(..., description="Média de referência para D_Med")
    sd_med: float = Field(..., description="Desvio padrão de referência para D_Med")
    mu_max: float = Field(..., description="Média de referência para D_Max")
    sd_max: float = Field(..., description="Desvio padrão de referência para D_Max")
    mu_std: float = Field(..., description="Média de referência para D_Std")
    sd_std: float = Field(..., description="Desvio padrão de referência para D_Std")


class PredicaoVazamentoRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    residencia_id: Optional[int] = Field(default=None, alias="residenciaId")
    z_d_med: Optional[float] = Field(default=None, alias="Z_D_Med")
    z_d_max: Optional[float] = Field(default=None, alias="Z_D_Max")
    z_d_std: Optional[float] = Field(default=None, alias="Z_D_Std")
    leituras_litros: Optional[List[float]] = Field(default=None, alias="leiturasLitros")
    referencia: Optional[ReferenciaDemanda] = None


class PredicaoVazamentoResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    vazamentoDetectado: bool
    classePredita: int
    probabilidadeVazamento: float
    nivelRisco: str
    featuresUtilizadas: Dict[str, float]


predictor_instance: Optional[ModeloVazamentoPredictor] = None


def get_predictor() -> ModeloVazamentoPredictor:
    global predictor_instance
    if predictor_instance is None:
        predictor_instance = ModeloVazamentoPredictor()
    return predictor_instance


@asynccontextmanager
async def lifespan(_app: FastAPI):
    get_predictor()
    yield


app = FastAPI(
    title="Recanto Céu Azul — Serviço de Inferência de Vazamentos",
    description="Microserviço FastAPI para inferência preditiva de vazamentos hídricos via modelo Scikit-Learn (.joblib).",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
def health_check():
    predictor = get_predictor()
    return {
        "status": "UP",
        "modelType": type(predictor.model).__name__,
        "features": predictor.feature_names,
        "supportsPredictProba": predictor.supports_proba,
    }


@app.post("/predict", response_model=PredicaoVazamentoResponse)
def predict_endpoint(payload: PredicaoVazamentoRequest):
    try:
        predictor = get_predictor()
        result = predictor.prever_dict(payload.model_dump(by_alias=True, exclude_none=True))
        return PredicaoVazamentoResponse(**result)
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail=f"Erro interno na inferência do modelo: {exc}"
        ) from exc
