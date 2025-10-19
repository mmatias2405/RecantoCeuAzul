package com.recantoceuazul.api.dto;

public class MedicaoRequest {
    
    private Float volumeAgua;
    private Integer residenciaId;
    private Integer atorId;

    // Getters e Setters
    public Float getVolumeAgua() {
        return volumeAgua;
    }

    public void setVolumeAgua(Float volumeAgua) {
        this.volumeAgua = volumeAgua;
    }

    public Integer getResidenciaId() {
        return residenciaId;
    }

    public void setResidenciaId(Integer residenciaId) {
        this.residenciaId = residenciaId;
    }

    public Integer getAtorId() {
        return atorId;
    }

    public void setAtorId(Integer atorId) {
        this.atorId = atorId;
    }
}
