package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "volume_agua")
    private Float volumeAgua;

    private Float delta;
    @Column(name = "data_medicao")
    private LocalDateTime dataMedicao;

    @ManyToOne
    @JoinColumn(name = "fk_Residencia_id", nullable = false)
    private Residencia residencia;

    @ManyToOne
    @JoinColumn(name = "fk_Ator_id", nullable = false)
    private Ator ator;
    public Medicao(){}

    public Medicao(Float volume_Agua, Float delta, LocalDateTime data_medicao, Residencia residencia, Ator ator) {
        this.volumeAgua = volume_Agua;
        this.delta = delta;
        this.dataMedicao = data_medicao;
        this.residencia = residencia;
        this.ator = ator;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getVolumeAgua() {
        return volumeAgua;
    }

    public void setVolumeAgua(Float volume_Agua) {
        this.volumeAgua = volume_Agua;
    }

    public Float getDelta() {
        return delta;
    }

    public void setDelta(Float delta) {
        this.delta = delta;
    }

    public LocalDateTime getDataMedicao() {
        return dataMedicao;
    }

    public void setDataMedicao(LocalDateTime data_medicao) {
        this.dataMedicao = data_medicao;
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