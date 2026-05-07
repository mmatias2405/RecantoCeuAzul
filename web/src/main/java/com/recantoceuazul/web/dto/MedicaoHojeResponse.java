package com.recantoceuazul.web.dto;

public class MedicaoHojeResponse {
    private float consumo;
    private String hora;

    public float getConsumo() {
        return consumo;
    }

    public void setConsumo(float consumo) {
        this.consumo = consumo;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
