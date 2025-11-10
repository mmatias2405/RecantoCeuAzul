package com.recantoceuazul.web.model;

public class Media {
    private String MesAno;
    private float MediaConsumo;
    public Media() {
    }

    public Media(String mesAno, float mediaConsumo) {
        MesAno = mesAno;
        MediaConsumo = mediaConsumo;
    } 

    public String getMesAno() {
        return MesAno;
    }

    public void setMesAno(String mesAno) {
        MesAno = mesAno;
    }

    public float getMediaConsumo() {
        return MediaConsumo;
    }

    public void setMediaConsumo(float mediaConsumo) {
        MediaConsumo = mediaConsumo;
    }


}
