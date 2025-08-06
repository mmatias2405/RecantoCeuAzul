package com.recantoceuazul.web.model;

import java.time.LocalDateTime;

public class Abastecimento {
    public Abastecimento() {
    }
    public Abastecimento(Float volumeAgua, LocalDateTime horaInicio, LocalDateTime horaFim, Setor setor,
            Administrador administrador) {
        this.volumeAgua = volumeAgua;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.setor = setor;
        this.administrador = administrador;
    }

    private Integer id;

    private Float volumeAgua;

    private LocalDateTime horaInicio;

    private LocalDateTime horaFim;

    private Setor setor;
    
    private Administrador administrador;

    

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

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalDateTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalDateTime horaFim) {
        this.horaFim = horaFim;
    }

    public Setor getSetor() {
        return setor;
    }

    public void setSetor(Setor setor) {
        this.setor = setor;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }
}
