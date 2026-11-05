package com.recantoceuazul.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InferenciaMlRequestDTO {

    private Integer residenciaId;

    @JsonProperty("Z_D_Med")
    private Double zDMed;

    @JsonProperty("Z_D_Max")
    private Double zDMax;

    @JsonProperty("Z_D_Std")
    private Double zDStd;

    private List<Double> leiturasLitros;

    public InferenciaMlRequestDTO() {
    }

    public InferenciaMlRequestDTO(Integer residenciaId, Double zDMed, Double zDMax, Double zDStd, List<Double> leiturasLitros) {
        this.residenciaId = residenciaId;
        this.zDMed = zDMed;
        this.zDMax = zDMax;
        this.zDStd = zDStd;
        this.leiturasLitros = leiturasLitros;
    }

    public Integer getResidenciaId() {
        return residenciaId;
    }

    public void setResidenciaId(Integer residenciaId) {
        this.residenciaId = residenciaId;
    }

    public Double getzDMed() {
        return zDMed;
    }

    public void setzDMed(Double zDMed) {
        this.zDMed = zDMed;
    }

    public Double getzDMax() {
        return zDMax;
    }

    public void setzDMax(Double zDMax) {
        this.zDMax = zDMax;
    }

    public Double getzDStd() {
        return zDStd;
    }

    public void setzDStd(Double zDStd) {
        this.zDStd = zDStd;
    }

    public List<Double> getLeiturasLitros() {
        return leiturasLitros;
    }

    public void setLeiturasLitros(List<Double> leiturasLitros) {
        this.leiturasLitros = leiturasLitros;
    }
}
