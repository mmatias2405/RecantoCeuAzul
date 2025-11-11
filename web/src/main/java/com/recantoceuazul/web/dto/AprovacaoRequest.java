package com.recantoceuazul.web.dto;

import java.util.List;

public class AprovacaoRequest {
    private String papel;
    private List<Integer> residencias; 
    private int atorId;

    public int getAtorId() {
        return atorId;
    }
    public void setAtorId(int atorId) {
        this.atorId = atorId;
    }
    // Getters e Setters
    public String getPapel() {
        return papel;
    }
    public void setPapel(String papel) {
        this.papel = papel;
    }
    public List<Integer> getResidencias() {
        return residencias;
    }
    public void setResidencias(List<Integer> residencias) {
        this.residencias = residencias;
    }
}
