package com.recantoceuazul.api.model;
import jakarta.persistence.*;

@Entity
public class Possuir {
    @ManyToOne
    @JoinColumn(name = "fk_Ator_id", nullable = false)
    private Ator ator;

    @ManyToOne
    @JoinColumn(name = "fk_Residencia_id", nullable = false)
    private Residencia residencia;


    public Possuir() {
    }

    public Possuir(Ator ator, Residencia residencia) {
        this.ator = ator;
        this.residencia = residencia;
    }

    public Ator getAtor() {
        return ator;
    }

    public void setAtor(Ator ator) {
        this.ator = ator;
    }

    public Residencia getResidencia() {
        return residencia;
    }

    public void setResidencia(Residencia residencia) {
        this.residencia = residencia;
    }
    
}
