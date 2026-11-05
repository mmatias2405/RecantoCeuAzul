package com.recantoceuazul.web.dto;

import java.util.Collections;
import java.util.Map;

public class AnaliseVazamentoResponse {

    private Integer residenciaId;
    private boolean analiseDisponivel;
    private boolean vazamentoDetectado;
    private String status;
    private Double probabilidadeVazamento;
    private String nivelRisco;
    private String mensagemOrientativa;
    private Integer quantidadeLeituras;
    private Double demandaMediaLitros;
    private Double demandaMaximaLitros;
    private Double desvioPadraoLitros;
    private Map<String, Double> featuresUtilizadas;
    private String dataHoraAnalise;

    public AnaliseVazamentoResponse() {
        this.probabilidadeVazamento = 0.0;
        this.featuresUtilizadas = Collections.emptyMap();
    }

    public static AnaliseVazamentoResponse indisponivelPadrao(Integer residenciaId) {
        AnaliseVazamentoResponse dto = new AnaliseVazamentoResponse();
        dto.setResidenciaId(residenciaId);
        dto.setAnaliseDisponivel(false);
        dto.setVazamentoDetectado(false);
        dto.setStatus("Dados insuficientes para análise");
        dto.setProbabilidadeVazamento(0.0);
        dto.setNivelRisco("Indisponível");
        dto.setQuantidadeLeituras(0);
        dto.setDataHoraAnalise("--:--");
        dto.setMensagemOrientativa(
                "Não foi possível obter dados suficientes de telemetria no momento. "
                        + "O diagnóstico da rede hidráulica será atualizado automaticamente assim que houver leituras disponíveis."
        );
        return dto;
    }

    public Integer getResidenciaId() {
        return residenciaId;
    }

    public void setResidenciaId(Integer residenciaId) {
        this.residenciaId = residenciaId;
    }

    public boolean isAnaliseDisponivel() {
        return analiseDisponivel;
    }

    public void setAnaliseDisponivel(boolean analiseDisponivel) {
        this.analiseDisponivel = analiseDisponivel;
    }

    public boolean isVazamentoDetectado() {
        return vazamentoDetectado;
    }

    public void setVazamentoDetectado(boolean vazamentoDetectado) {
        this.vazamentoDetectado = vazamentoDetectado;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getProbabilidadeVazamento() {
        return probabilidadeVazamento;
    }

    public void setProbabilidadeVazamento(Double probabilidadeVazamento) {
        this.probabilidadeVazamento = probabilidadeVazamento;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public String getMensagemOrientativa() {
        return mensagemOrientativa;
    }

    public void setMensagemOrientativa(String mensagemOrientativa) {
        this.mensagemOrientativa = mensagemOrientativa;
    }

    public Integer getQuantidadeLeituras() {
        return quantidadeLeituras;
    }

    public void setQuantidadeLeituras(Integer quantidadeLeituras) {
        this.quantidadeLeituras = quantidadeLeituras;
    }

    public Double getDemandaMediaLitros() {
        return demandaMediaLitros;
    }

    public void setDemandaMediaLitros(Double demandaMediaLitros) {
        this.demandaMediaLitros = demandaMediaLitros;
    }

    public Double getDemandaMaximaLitros() {
        return demandaMaximaLitros;
    }

    public void setDemandaMaximaLitros(Double demandaMaximaLitros) {
        this.demandaMaximaLitros = demandaMaximaLitros;
    }

    public Double getDesvioPadraoLitros() {
        return desvioPadraoLitros;
    }

    public void setDesvioPadraoLitros(Double desvioPadraoLitros) {
        this.desvioPadraoLitros = desvioPadraoLitros;
    }

    public Map<String, Double> getFeaturesUtilizadas() {
        return featuresUtilizadas;
    }

    public void setFeaturesUtilizadas(Map<String, Double> featuresUtilizadas) {
        this.featuresUtilizadas = featuresUtilizadas;
    }

    public String getDataHoraAnalise() {
        return dataHoraAnalise;
    }

    public void setDataHoraAnalise(String dataHoraAnalise) {
        this.dataHoraAnalise = dataHoraAnalise;
    }
}
