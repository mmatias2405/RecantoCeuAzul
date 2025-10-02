package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Float volume_Agua;

    private Float delta;

    private LocalDateTime data_medicao;

    @ManyToOne
    @JoinColumn(name = "fk_Residencia_id", nullable = false)
    private Residencia residencia;

    @ManyToOne
    @JoinColumn(name = "fk_Ator_id", nullable = false)
    private Ator ator;

    public Medicao(Float volume_Agua, Float delta, LocalDateTime data_medicao, Residencia residencia, Ator ator) {
        this.volume_Agua = volume_Agua;
        this.delta = delta;
        this.data_medicao = data_medicao;
        this.residencia = residencia;
        this.ator = ator;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getVolume_Agua() {
        return volume_Agua;
    }

    public void setVolume_Agua(Float volume_Agua) {
        this.volume_Agua = volume_Agua;
    }

    public Float getDelta() {
        return delta;
    }

    public void setDelta(Float delta) {
        this.delta = delta;
    }

    public LocalDateTime getData_medicao() {
        return data_medicao;
    }

    public void setData_medicao(LocalDateTime data_medicao) {
        this.data_medicao = data_medicao;
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public void setResidencia(Residencia residencia) {
        this.residencia = residencia;
    }

    public Ator getAtor() {
        return ator;
    }

    public void setAtor(Ator ator) {
        this.ator = ator;
    }



    

}