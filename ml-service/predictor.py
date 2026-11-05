"""
Núcleo de inferência do modelo RandomForestClassifier (.joblib) para detecção de vazamentos.

Suporta execução tanto em memória (importado pelo microserviço FastAPI `app.py`)
quanto via linha de comando (`python predictor.py --json '{...}'`).
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import warnings
from pathlib import Path
from typing import Any, Dict, List, Optional

import joblib
import numpy as np

# Parâmetros de referência calibrados a partir do perfil histórico residencial do condomínio
# (utilizados como fallback caso sejam enviadas leituras brutas sem baseline explícito).
DEFAULT_REF_STATS: Dict[str, float] = {
    "mu_med": 408.7336,
    "sd_med": 53.1663,
    "mu_max": 483.7436,
    "sd_max": 62.2806,
    "mu_std": 47.7795,
    "sd_std": 8.2275,
}

EXPECTED_FEATURES = ("Z_D_Med", "Z_D_Max", "Z_D_Std")


def resolve_model_path() -> Path:
    env_path = os.getenv("MODEL_PATH")
    if env_path:
        candidate = Path(env_path)
        if candidate.is_file():
            return candidate

    base_dir = Path(__file__).resolve().parent
    candidates = [
        base_dir / "modelo_rf_recomendado_api.joblib",
        base_dir.parent / "modelo_rf_recomendado_api.joblib",
        Path.cwd() / "modelo_rf_recomendado_api.joblib",
    ]
    for candidate in candidates:
        if candidate.is_file():
            return candidate

    raise FileNotFoundError(
        "Arquivo 'modelo_rf_recomendado_api.joblib' não encontrado. "
        "Defina a variável de ambiente MODEL_PATH ou mantenha o arquivo na raiz do projeto."
    )


class ModeloVazamentoPredictor:
    def __init__(self, model_path: Optional[Path] = None) -> None:
        self.model_path = model_path or resolve_model_path()
        self.model = joblib.load(self.model_path)
        self.feature_names: List[str] = (
            [str(f) for f in self.model.feature_names_in_]
            if hasattr(self.model, "feature_names_in_")
            else list(EXPECTED_FEATURES)
        )
        self.supports_proba: bool = hasattr(self.model, "predict_proba")
        self.positive_class_index: int = self._find_positive_class_index()

    def _find_positive_class_index(self) -> int:
        if hasattr(self.model, "classes_"):
            classes = list(self.model.classes_)
            if 1 in classes:
                return classes.index(1)
            return len(classes) - 1
        return 1

    @staticmethod
    def _safe_zscore(val: float, mu: float, sd: float) -> float:
        denom = sd if abs(sd) > 1e-6 else 1.0
        z = (val - mu) / denom
        return float(np.clip(z, -5.0, 5.0))

    @staticmethod
    def _get_first_present(data: Dict[str, Any], *keys: str) -> Optional[Any]:
        for k in keys:
            if k in data and data[k] is not None:
                return data[k]
        return None

    def extrair_features(self, payload: Dict[str, Any]) -> Dict[str, float]:
        z_med = self._get_first_present(payload, "Z_D_Med", "z_d_med", "zDMed")
        z_max = self._get_first_present(payload, "Z_D_Max", "z_d_max", "zDMax")
        z_std = self._get_first_present(payload, "Z_D_Std", "z_d_std", "zDStd")

        if z_med is not None and z_max is not None and z_std is not None:
            return {
                "Z_D_Med": float(z_med),
                "Z_D_Max": float(z_max),
                "Z_D_Std": float(z_std),
            }

        leituras_raw = self._get_first_present(
            payload, "leiturasLitros", "leituras_litros", "leituras"
        )
        if leituras_raw is not None:
            leituras = np.asarray(leituras_raw, dtype=np.float64)
            leituras = leituras[np.isfinite(leituras) & (leituras >= 0)]
            if leituras.size < 3:
                raise ValueError(
                    "São necessárias ao menos 3 leituras válidas de demanda hídrica para calcular as features."
                )

            d_med = float(np.mean(leituras))
            d_max = float(np.max(leituras))
            d_std = float(np.std(leituras, ddof=1))

            ref = payload.get("referencia") or DEFAULT_REF_STATS
            return {
                "Z_D_Med": self._safe_zscore(
                    d_med, float(ref["mu_med"]), float(ref["sd_med"])
                ),
                "Z_D_Max": self._safe_zscore(
                    d_max, float(ref["mu_max"]), float(ref["sd_max"])
                ),
                "Z_D_Std": self._safe_zscore(
                    d_std, float(ref["mu_std"]), float(ref["sd_std"])
                ),
            }

        raise ValueError(
            "Informe as features padronizadas ('Z_D_Med', 'Z_D_Max', 'Z_D_Std') "
            "ou a série 'leiturasLitros' com ao menos 3 medições."
        )

    @staticmethod
    def classificar_risco(probabilidade_percentual: float, vazamento_detectado: bool) -> str:
        if probabilidade_percentual >= 70.0:
            return "Alto"
        if probabilidade_percentual >= 40.0 or vazamento_detectado:
            return "Moderado"
        return "Baixo"

    def prever_dict(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        features_map = self.extrair_features(payload)
        ordered_vector = np.array(
            [[features_map[name] for name in self.feature_names]], dtype=np.float64
        )

        with warnings.catch_warnings():
            warnings.filterwarnings(
                "ignore",
                message="X does not have valid feature names.*",
                category=UserWarning,
            )
            classe_predita = int(self.model.predict(ordered_vector)[0])
            if self.supports_proba:
                probas = self.model.predict_proba(ordered_vector)[0]
                prob_vazamento = float(probas[self.positive_class_index]) * 100.0
            else:
                prob_vazamento = 100.0 if classe_predita == 1 else 0.0

        vazamento_detectado = bool(classe_predita == 1)
        prob_arredondada = round(prob_vazamento, 2)
        nivel_risco = self.classificar_risco(prob_arredondada, vazamento_detectado)

        return {
            "vazamentoDetectado": vazamento_detectado,
            "classePredita": classe_predita,
            "probabilidadeVazamento": prob_arredondada,
            "nivelRisco": nivel_risco,
            "featuresUtilizadas": {k: round(v, 4) for k, v in features_map.items()},
        }


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Executa inferência de detecção de vazamentos com o modelo .joblib."
    )
    parser.add_argument(
        "--json",
        dest="json_payload",
        help="Payload JSON contendo features Z-Score ou série de leituras em litros.",
    )
    args = parser.parse_args()

    try:
        raw_input = args.json_payload if args.json_payload else sys.stdin.read()
        data = json.loads(raw_input)
        predictor = ModeloVazamentoPredictor()
        result = predictor.prever_dict(data)
        print(json.dumps(result, ensure_ascii=False))
        return 0
    except Exception as exc:
        print(json.dumps({"erro": str(exc)}, ensure_ascii=False), file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
