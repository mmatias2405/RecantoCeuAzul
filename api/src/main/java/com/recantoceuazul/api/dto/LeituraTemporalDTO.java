package com.recantoceuazul.api.dto;

import java.time.Instant;

public class LeituraTemporalDTO {

    private Instant timestamp;
    private double volumeLitros;

    public LeituraTemporalDTO() {
    }

    public LeituraTemporalDTO(Instant timestamp, double volumeLitros) {
        this.timestamp = timestamp;
        this.volumeLitros = volumeLitros;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getVolumeLitros() {
        return volumeLitros;
    }

    public void setVolumeLitros(double volumeLitros) {
        this.volumeLitros = volumeLitros;
    }
}
