package com.recantoceuazul.api.dto;

public interface ConsumoMensalProjection {
    
    // O nome do método get bate com a coluna da view ("MesAno")
    String getMesAno(); 
    
    // O nome do método get bate com a coluna da view ("MediaConsumo")
    Double getMediaConsumo(); 

}
