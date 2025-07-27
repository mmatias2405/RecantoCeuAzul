package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Abastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Float volumeAgua;

    private LocalDateTime horaInicio;

    private LocalDateTime horaFim;

    @ManyToOne
    @JoinColumn(name = "fk_Setor_id", nullable = false)
    private Setor setor;

    @ManyToOne
    @JoinColumn(name = "fk_Administrador_id", nullable = false)
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