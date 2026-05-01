package com.recantoceuazul.api.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
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

    public void processarMensagemMqtt(int residencia, double volumeLitros) {
        if (volumeLitros <= 0) return; // Não grava dados vazios para poupar disco

        Point point = Point.measurement("consumo_agua")
                .addTag("residencia", Integer.toString(residencia))
                .addField("volume_litros", volumeLitros)
                .time(Instant.now(), WritePrecision.MS);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(bucket, org, point);
    }
}