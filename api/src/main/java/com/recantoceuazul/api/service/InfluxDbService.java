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

    public void processarMensagemMqtt(String residenciaId, double volumeLitros) {
        if (volumeLitros <= 0) return; // Não grava dados vazios para poupar disco

        Point point = Point.measurement("consumo_agua")
                .addTag("residenciaId", residenciaId)
                .addField("volume_litros", volumeLitros)
                .time(Instant.now(), WritePrecision.MS);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(bucket, org, point);
    }

    public List<FluxTable> medicoesDoMesPassado(){
                // Query FLUX para somar todo o volume do último mês agrupado por residência
        String query = 
            "import \"date\"\n" +
            "import \"timezone\"\n" +
            "option location = timezone.location(name: \"America/Sao_Paulo\")\n" +
            "startOfThisMonth = date.truncate(t: now(), unit: 1mo)\n" +
            "startOfLastMonth = date.sub(d: 1mo, from: startOfThisMonth)\n" +
            "from(bucket: \"home\") \n" +
            "|> range(start: startOfLastMonth, stop: startOfThisMonth) \n" +
            "|> filter(fn: (r) => r[\"_measurement\"] == \"consumo_agua\") \n" +
            "|> filter(fn: (r) => r[\"_field\"] == \"volume_litros\") \n" +
            "|> group(columns: [\"residenciaId\"]) \n" +
            "|> sum()";

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
        
        return tables;
    }
    public Double getConsumoHoje(String residenciaId) {
        String query = String.format(
            "import \"date\"\n" +
            "import \"timezone\"\n" +
            "option location = timezone.location(name: \"America/Sao_Paulo\")\n" +
            "from(bucket: \"home\") \n" +
            "|> range(start: date.truncate(t: now(), unit: 1mo)) \n" +
            "|> filter(fn: (r) => r[\"_measurement\"] == \"consumo_agua\") \n" +
            "|> filter(fn: (r) => r[\"_field\"] == \"volume_litros\") \n" +
            "|> filter(fn: (r) => r[\"residenciaId\"] == \"%s\") \n" +
            "|> sum()", residenciaId
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0.0;
        }
        return (Double) tables.get(0).getRecords().get(0).getValue();
    }
}