package com.recantoceuazul.api.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

public class AnaliseVazamentoResponseDTO {

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

    public AnaliseVazamentoResponseDTO() {
        this.dataHoraAnalise = agoraFormatado();
        this.featuresUtilizadas = Collections.emptyMap();
    }

    public static String agoraFormatado() {
        return LocalDateTime.now(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public static AnaliseVazamentoResponseDTO dadosInsuficientes(Integer residenciaId, int qtdLeituras) {
        AnaliseVazamentoResponseDTO dto = new AnaliseVazamentoResponseDTO();
        dto.setResidenciaId(residenciaId);
        dto.setAnaliseDisponivel(false);
        dto.setVazamentoDetectado(false);
        dto.setStatus("Dados insuficientes para análise");
        dto.setProbabilidadeVazamento(0.0);
        dto.setNivelRisco("Indisponível");
        dto.setQuantidadeLeituras(qtdLeituras);
        dto.setMensagemOrientativa(
                "Ainda não há leituras recentes suficientes dos sensores para realizar o diagnóstico preditivo desta residência. "
                        + "O monitoramento inteligente será ativado automaticamente assim que novas medições forem registradas."
        );
        return dto;
    }

    public static AnaliseVazamentoResponseDTO servicoIndisponivel(Integer residenciaId, String detalhe) {
        AnaliseVazamentoResponseDTO dto = new AnaliseVazamentoResponseDTO();
        dto.setResidenciaId(residenciaId);
        dto.setAnaliseDisponivel(false);
        dto.setVazamentoDetectado(false);
        dto.setStatus("Diagnóstico temporariamente indisponível");
        dto.setProbabilidadeVazamento(0.0);
        dto.setNivelRisco("Indisponível");
        dto.setQuantidadeLeituras(0);
        dto.setMensagemOrientativa(detalhe);
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
