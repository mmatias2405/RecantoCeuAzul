package com.recantoceuazul.api.dto;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MedicaoHojeResponse {
    private float consumo;
    private String hora;

    public MedicaoHojeResponse(float consumo) {
        this.consumo = consumo;
        this.hora = LocalTime.now(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

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
