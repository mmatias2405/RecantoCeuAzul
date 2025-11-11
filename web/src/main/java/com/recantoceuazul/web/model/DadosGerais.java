package com.recantoceuazul.web.model;

public class DadosGerais {
    private String MesAno;
    private String MedicoesRealizadas;
    private float consumoTotal;
    private float MediaConsumo;
    public DadosGerais() {
    }
    public DadosGerais(String mesAno, String medicoesRealizadas, float consumoTotal, float mediaConsumo) {
        MesAno = mesAno;
        MedicoesRealizadas = medicoesRealizadas;
        this.consumoTotal = consumoTotal;
        MediaConsumo = mediaConsumo;
    }
    public String getMesAno() {
        return MesAno;
    }
    public void setMesAno(String mesAno) {
        MesAno = mesAno;
    }
    public String getMedicoesRealizadas() {
        return MedicoesRealizadas;
    }
    public void setMedicoesRealizadas(String medicoesRealizadas) {
        MedicoesRealizadas = medicoesRealizadas;
    }
    public float getConsumoTotal() {
        return consumoTotal;
    }
    public void setConsumoTotal(float consumoTotal) {
        this.consumoTotal = consumoTotal;
    }
    public float getMediaConsumo() {
        return MediaConsumo;
    }
    public void setMediaConsumo(float mediaConsumo) {
        MediaConsumo = mediaConsumo;
    }
}
