package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Captacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Float volumeAgua;

    private LocalDateTime hora;

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

    public LocalDateTime getHora() {
        return hora;
    }

    public void setHora(LocalDateTime hora) {
        this.hora = hora;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

}