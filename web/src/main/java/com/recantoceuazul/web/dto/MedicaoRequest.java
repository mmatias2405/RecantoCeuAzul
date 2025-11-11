package com.recantoceuazul.web.dto;

public class MedicaoRequest {

    // Volume de água consumido durante o período medido (em litros ou unidade definida pela aplicação).
    private Float volumeAgua;

    // Identificador da residência à qual a medição está associada.
    private Integer residenciaId;

    // Identificador do ator (usuário) responsável pelo registro da medição.
    private Integer atorId;


    // Retorna o volume de água medido.
    public Float getVolumeAgua() {
        return volumeAgua;
    }

    // Define o volume de água medido.
    public void setVolumeAgua(Float volumeAgua) {
        this.volumeAgua = volumeAgua;
    }

    // Retorna o ID da residência associada à medição.
    public Integer getResidenciaId() {
        return residenciaId;
    }

    // Define o ID da residência associada à medição.    
    public void setResidenciaId(Integer residenciaId) {
        this.residenciaId = residenciaId;
    }

    // Retorna o ID do ator responsável pela medição.    
    public Integer getAtorId() {
        return atorId;
    }

    // Define o ID do ator responsável pela medição.    
    public void setAtorId(Integer atorId) {
        this.atorId = atorId;
    }
}
