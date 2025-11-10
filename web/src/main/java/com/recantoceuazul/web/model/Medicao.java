package com.recantoceuazul.web.model;
import java.time.LocalDateTime;

/**
 * Representa uma medição do volume de água em uma residência.
 * 
 * Cada registro de Medicao armazena o volume de água medido em um determinado momento,
 * o valor da variação (delta) em relação à medição anterior e está associado a um ator (quem registrou)
 * e a uma residência específica.
 */

public class Medicao {

    private Integer id; // Identificador único da medição (gerado automaticamente pelo banco de dados)
    private Float volumeAgua; // Volume total de água medido (em litros ou unidade definida pelo sistema)
    private Float delta; // Diferença de volume em relação à medição anterior
    private LocalDateTime dataMedicao; // Data e hora em que a medição foi realizada
    private Residencia residencia;
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