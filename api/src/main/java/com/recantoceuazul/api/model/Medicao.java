package com.recantoceuazul.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa uma medição do volume de água em uma residência.
 * 
 * Cada registro de Medicao armazena o volume de água medido em um determinado momento,
 * o valor da variação (delta) em relação à medição anterior e está associado a um ator (quem registrou)
 * e a uma residência específica.
 */

@Entity
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Identificador único da medição (gerado automaticamente pelo banco de dados)

    @Column(name = "volume_agua")
    private Float volumeAgua; // Volume total de água medido (em litros ou unidade definida pelo sistema)

    private Float delta; // Diferença de volume em relação à medição anterior
    @Column(name = "data_medicao")
    private LocalDateTime dataMedicao; // Data e hora em que a medição foi realizada

 /**
 * Relação muitos-para-um entre Medicao e Residencia.
 * 
 * Cada medição pertence a uma única residência, mas uma residência pode ter várias medições registradas.
 * A chave estrangeira é armazenada na coluna "fk_Residencia_id".
 */

    @ManyToOne
    @JoinColumn(name = "fk_Residencia_id", nullable = false)
    private Residencia residencia;

/**
 * Relação muitos-para-um entre Medicao e Ator.
 * 
 * Indica qual ator (por exemplo, funcionário ou morador) realizou ou registrou a medição.
 * A chave estrangeira é armazenada na coluna "fk_Ator_id".
 */

    @ManyToOne
    @JoinColumn(name = "fk_Ator_id", nullable = false)
    private Ator ator;

     // Construtor padrão exigido pelo JPA
    public Medicao(){}

    // Construtor completo para inicialização manual
    public Medicao(Float volume_Agua, Float delta, LocalDateTime data_medicao, Residencia residencia, Ator ator) {
        this.volumeAgua = volume_Agua;
        this.delta = delta;
        this.dataMedicao = data_medicao;
        this.residencia = residencia;
        this.ator = ator;
    }

    // Getters e Setters (acessores e modificadores dos atributos)
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