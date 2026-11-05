package com.recantoceuazul.api.dto;

import java.util.Map;

public class InferenciaMlResponseDTO {

    private boolean vazamentoDetectado;
    private int classePredita;
    private double probabilidadeVazamento;
    private String nivelRisco;
    private Map<String, Double> featuresUtilizadas;

    public InferenciaMlResponseDTO() {
    }

    public boolean isVazamentoDetectado() {
        return vazamentoDetectado;
    }

    public void setVazamentoDetectado(boolean vazamentoDetectado) {
        this.vazamentoDetectado = vazamentoDetectado;
    }

    public int getClassePredita() {
        return classePredita;
    }

    public void setClassePredita(int classePredita) {
        this.classePredita = classePredita;
    }

    public double getProbabilidadeVazamento() {
        return probabilidadeVazamento;
    }

    public void setProbabilidadeVazamento(double probabilidadeVazamento) {
        this.probabilidadeVazamento = probabilidadeVazamento;
    }

    public String getNivelRisco() {
        return nivelRisco;
    }

    public void setNivelRisco(String nivelRisco) {
        this.nivelRisco = nivelRisco;
    }

    public Map<String, Double> getFeaturesUtilizadas() {
        return featuresUtilizadas;
    }

    public void setFeaturesUtilizadas(Map<String, Double> featuresUtilizadas) {
        this.featuresUtilizadas = featuresUtilizadas;
    }
}
