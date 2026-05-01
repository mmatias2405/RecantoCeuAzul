package com.recantoceuazul.api.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class InfluxDbService {

    private final InfluxDBClient influxDBClient;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${influx.org}")
    private String org;

    public InfluxDbService(
            @Value("${influx.url}") String url,
            @Value("${influx.token}") String token) {
        this.influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray());
    }

    public void processarMensagemMqtt(int residenciaId, double volumeLitros) {
        if (volumeLitros <= 0) return; // Não grava dados vazios para poupar disco

        Point point = Point.measurement("consumo_agua")
                .addTag("residenciaId", Integer.toString(residenciaId))
                .addField("volume_litros", volumeLitros)
                .time(Instant.now(), WritePrecision.MS);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(bucket, org, point);
    }

    public List<FluxTable> medicoesDoMesPassado(){
                // Query FLUX para somar todo o volume do último mês agrupado por residência
        String query = String.format(
            "from(bucket: \"home\") " +
            "|> range(start: -1mo) " +
            "|> filter(fn: (r) => r[\"_measurement\"] == \"consumo_agua\") " +
            "|> filter(fn: (r) => r[\"_field\"] == \"volume_litros\") " +
            "|> group(columns: [\"residenciaId\"]) " +
            "|> sum()"
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
        
        return tables;
    }
}