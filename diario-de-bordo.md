# Diário de Bordo — Integração do Módulo de Detecção Preditiva de Vazamentos

Projeto: **Sistema Inteligente de Gestão Hídrica e Detecção de Vazamentos — Residencial Recanto do Céu Azul**

---

## Etapa 1 — Diagnóstico Arquitetural e Inspeção do Modelo Preditivo (`.joblib`)

- **Data/Hora e Etapa**: `2026-11-04 21:22 BRT` — Etapa 1: Mapeamento Arquitetural do Repositório, Schema do InfluxDB/MySQL e Inspeção Técnica do Modelo `.joblib`.
- **Arquivos Criados/Modificados**:
  - `diario-de-bordo.md` (criado)
- **Decisões de Arquitetura e Implementação**:
  1. **Mapeamento da Estrutura do Repositório e Infraestrutura (`docker-compose.yml`)**:
     - O sistema é composto por dois módulos Java 17 + Spring Boot 3.5.6 independentes e dois bancos de dados orquestrados via `docker-compose.yml` na rede `recantonet`:
       - **`api/` (`com.recantoceuazul.api`, porta `8081`)**: Backend REST + consumidor MQTT (`Spring Integration MQTT` / Eclipse Paho) + cliente InfluxDB (`influxdb-client-java` v7.5.0) + persistência relacional (`Spring Data JPA` / MySQL Connector). Possui `MqttConfig`, `AtorController` (`/api/ator`), `MedicaoController` (`/api/medicao`), `ResidenciaController` (`/api/residencia`), `InfluxDbService`, `MedicaoService` e `MqttService`.
       - **`web/` (`com.recantoceuazul.web`, porta `8080`)**: Aplicação web MVC com `Spring Boot + Thymeleaf + Bootstrap 5.3.2 + Chart.js`. Centraliza rotas de interface e proxy no `HomeController`, comunicando-se com o módulo `api` via `RestTemplate` (`${API_URL}`, padrão `http://api:8081/api/` no Docker). Possui templates em `web/src/main/resources/templates/` (`index.html`, `cadastro.html`, `dashboardMorador.html`, `dashboardAdmin.html`, `consumo.html`, `registrarResidencia.html`) e estilização em `web/src/main/resources/static/customStyles.css`.
       - **`db` (MySQL 8.0, porta `3306`)**: Armazena o cadastro de `ator`, `residencia`, tabela associativa `possuir`, histórico consolidado mensal `medicao` (`volume_agua` acumulado e `delta` mensal em m³) e views analíticas (`v_media_consumo_condominio` e `v_dashboard_mensal`), inicializados por `mysql-init/init.sql`.
       - **`influxdb` (InfluxDB 2.8.0, porta `8086`)**: Banco de séries temporais configurado com organização padrão `docs` e bucket `home` (`${INFLUXDB_ORG}` / `${INFLUXDB_BUCKET}`).

  2. **Inspeção Técnica do Modelo de Machine Learning (`modelo_rf_recomendado_api.joblib`)**:
     - Localizado na raiz do repositório: `/modelo_rf_recomendado_api.joblib` (~4,56 MB).
     - Inspecionado via script Python com `joblib` (v1.6.0) e `scikit-learn` (v1.9.1):
       - **Classe do Estimador**: `sklearn.ensemble._forest.RandomForestClassifier`
       - **Hiperparâmetros Principais**: `n_estimators=100`, `criterion='gini'`, `max_features='sqrt'`, `min_samples_split=4`, `min_samples_leaf=2`, `bootstrap=True`, `random_state=42`, treinado com `_n_samples=22000` (profundidade das árvores entre 22 e 36).
       - **Número de Features Esperadas (`n_features_in_`)**: `3`
       - **Nomes Exatos das Features (`feature_names_in_`)**: `['Z_D_Med', 'Z_D_Max', 'Z_D_Std']`
         - `Z_D_Med`: Z-Score da demanda média no período/janela analisada (importância: `32,98%`, limiares de corte entre `-3,57` e `+3,67`, mediana `-0,0017`).
         - `Z_D_Max`: Z-Score da demanda máxima no período/janela analisada (importância: `39,44%`, limiares de corte entre `-4,01` e `+3,14`, mediana `+0,1949`).
         - `Z_D_Std`: Z-Score do desvio padrão da demanda no período/janela analisada (importância: `27,58%`, limiares de corte entre `-2,79` e `+4,38`, mediana `-0,1624`).
       - **Classes de Saída (`classes_`)**: `[0, 1]` (`0` = Rede Normal / Sem Vazamento; `1` = Anomalia / Provável Vazamento).
       - **Métodos Suportados**: Suporta tanto `predict(X)` (classificação binária `0`/`1`) quanto `predict_proba(X)` (vetor de probabilidades `[P(classe=0), P(classe=1)]`).

  3. **Análise do Fluxo Atual MQTT -> InfluxDB -> Spring Boot -> Interface Web**:
     - **Ingestão (`MqttService` -> `InfluxDbService.processarMensagemMqtt`)**: Mensagens JSON recebidas no tópico MQTT contendo `residenciaId` e `volume_litros` (> 0) são gravadas no InfluxDB:
       - **Bucket**: `${influx.bucket}` (`home`)
       - **Measurement**: `consumo_agua`
       - **Tag**: `residenciaId` (identificador da residência em String, ex: `"1"` a `"20"`, conforme também observado em `influxdb-init/dummy-data.lp`)
       - **Field**: `volume_litros` (`double`, volume consumido em litros)
       - **Timestamp**: `Instant.now()` com precisão de milissegundos (`WritePrecision.MS`).
     - **Consulta de Consumo em Tempo Real (`InfluxDbService.getConsumoHoje` -> `MedicaoService.getMedicaoHoje`)**:
       - `InfluxDbService.getConsumoHoje(String residenciaId)` executa uma query Flux filtrando `_measurement == "consumo_agua"`, `_field == "volume_litros"` e `r["residenciaId"] == "<id>"` desde o início do mês corrente (`date.truncate(t: now(), unit: 1mo)` no fuso `America/Sao_Paulo`), aplicando `|> sum()`.
       - `MedicaoService.getMedicaoHoje(int residenciaId)` converte os litros acumulados no mês para m³ (`/ 1000`) e soma à última leitura consolidada no MySQL (`Medicao.volumeAgua`), retornando `MedicaoHojeResponse` (`consumo` e `hora`).
       - No frontend (`dashboardMorador.html`), o valor inicial é renderizado via Thymeleaf no hidrômetro digital e atualizado assincronamente a cada 30 segundos por `fetch('/medicao/hoje/residencia/' + idResidencia)` -> `HomeController` -> `GET /api/medicao/hoje/residencia/{id}`.
     - **Consolidação Mensal (`InfluxDbService.medicoesDoMesPassado` -> `MedicaoService.consolidarConsumoMesAnterior`)**: Rotina `@Scheduled(cron = "0 0 0 1 * ?")` soma os litros do mês anterior no InfluxDB agrupados por `residenciaId` e persiste o fechamento mensal no MySQL.

- **Validação/Testes Realizados**:
  - Inspeção automatizada via script Python em ambiente isolado carregando `modelo_rf_recomendado_api.joblib` com `joblib.load()`, validando `type(rf)`, `n_features_in_`, `feature_names_in_`, `classes_`, `feature_importances_` e chamadas reais a `predict()` e `predict_proba()`.
  - Verificação de conectividade e inspeção do schema do InfluxDB ativo na porta `8086` (organização `docs`, bucket `home`) e dos scripts `influxdb-init/dummy-data.lp` e `mysql-init/init.sql`.
- **Hash/Mensagem do Commit Associado**:
  - Mensagem: `docs(diario): adiciona mapeamento arquitetural e inspecao do modelo joblib`
