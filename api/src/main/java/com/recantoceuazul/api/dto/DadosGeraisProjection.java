package com.recantoceuazul.api.dto;

public interface DadosGeraisProjection {
    // O nome do método get bate com a coluna da view ("MesAno")
    String getMesAno(); 
    // O nome do método get bate com a coluna da view ("ConsumoTotal")
    Double getConsumoTotal(); 
    // O nome do método get bate com a coluna da view ("MedicaoesRealizadas")
    String getMedicoesRealizadas(); 
    // O nome do método get bate com a coluna da view ("MediaConsumo")
    Double getMediaConsumo(); 
}
