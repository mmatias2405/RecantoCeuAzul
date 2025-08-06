package com.recantoceuazul.web.model;

import java.time.LocalDateTime;

public class Captacao {
    public Captacao() {}
    
    public Captacao(Float volumeAgua, LocalDateTime hora) {
        this.volumeAgua = volumeAgua;
        this.hora = hora;
    }

    private Integer id;

    private Float volumeAgua;

    private LocalDateTime hora;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getVolumeAgua() {
        return volumeAgua;
    }

    public void setVolumeAgua(Float volumeAgua) {
        this.volumeAgua = volumeAgua;
    }

    public LocalDateTime getHora() {
        return hora;
    }

    public void setHora(LocalDateTime hora) {
        this.hora = hora;
    }
    
}
