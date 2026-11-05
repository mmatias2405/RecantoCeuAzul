package com.recantoceuazul.api.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.recantoceuazul.api.dto.LeituraTemporalDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Recupera as últimas leituras diárias consolidadas de demanda hídrica (volume_litros) agrupadas por residência,
     * agregando pacotes subdiários enviados pelo ESP32 (ex.: a cada 10s) para compor o vetor (Z_D_Med, Z_D_Max, Z_D_Std).
     */
    public Map<String, List<Double>> buscarUltimasLeiturasCondominio(int limitePorResidencia) {
        String bucketAlvo = (bucket != null && !bucket.isBlank()) ? bucket : "home";
        String query = String.format(
            "import \"timezone\"\n" +
            "option location = timezone.location(name: \"America/Sao_Paulo\")\n" +
            "from(bucket: \"%s\")\n" +
            "  |> range(start: -365d)\n" +
            "  |> filter(fn: (r) => r[\"_measurement\"] == \"consumo_agua\")\n" +
            "  |> filter(fn: (r) => r[\"_field\"] == \"volume_litros\")\n" +
            "  |> group(columns: [\"residenciaId\"])\n" +
            "  |> aggregateWindow(every: 1d, fn: sum, createEmpty: false)\n" +
            "  |> sort(columns: [\"_time\"])\n" +
            "  |> tail(n: %d)",
            bucketAlvo,
            limitePorResidencia
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
        Map<String, List<Double>> leiturasPorResidencia = new LinkedHashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object resKey = record.getValueByKey("residenciaId");
                Object valorObj = record.getValue();
                if (resKey != null && valorObj instanceof Number valorNum) {
                    String resId = resKey.toString();
                    leiturasPorResidencia
                            .computeIfAbsent(resId, k -> new ArrayList<>())
                            .add(valorNum.doubleValue());
                }
            }
        }

        return leiturasPorResidencia;
    }

    /**
     * Recupera as últimas leituras diárias (preservando o primeiro timestamp real do dia + soma de volume_litros)
     * agrupadas por residência, compatibilizando telemetria granular do ESP32 (pulsos de 10s) com o cálculo das features
     * do modelo e o rastreamento do horário exato de início de anomalia.
     */
    public Map<String, List<LeituraTemporalDTO>> buscarUltimasLeiturasTemporaisCondominio(int limitePorResidencia) {
        String bucketAlvo = (bucket != null && !bucket.isBlank()) ? bucket : "home";
        String query = String.format(
            "import \"timezone\"\n" +
            "option location = timezone.location(name: \"America/Sao_Paulo\")\n" +
            "from(bucket: \"%s\")\n" +
            "  |> range(start: -365d)\n" +
            "  |> filter(fn: (r) => r[\"_measurement\"] == \"consumo_agua\")\n" +
            "  |> filter(fn: (r) => r[\"_field\"] == \"volume_litros\")\n" +
            "  |> group(columns: [\"residenciaId\"])\n" +
            "  |> window(every: 1d, createEmpty: false)\n" +
            "  |> reduce(\n" +
            "       identity: {volume_dia: 0.0, primeiro_ts: 0},\n" +
            "       fn: (r, accumulator) => ({\n" +
            "         volume_dia: accumulator.volume_dia + r._value,\n" +
            "         primeiro_ts: if accumulator.primeiro_ts == 0 or int(v: r._time) < accumulator.primeiro_ts then int(v: r._time) else accumulator.primeiro_ts\n" +
            "       })\n" +
            "     )\n" +
            "  |> map(fn: (r) => ({\n" +
            "       residenciaId: r.residenciaId,\n" +
            "       _time: time(v: r.primeiro_ts),\n" +
            "       _value: r.volume_dia\n" +
            "     }))\n" +
            "  |> group(columns: [\"residenciaId\"])\n" +
            "  |> sort(columns: [\"_time\"])\n" +
            "  |> tail(n: %d)",
            bucketAlvo,
            limitePorResidencia
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(query, org);
        Map<String, List<LeituraTemporalDTO>> leiturasPorResidencia = new LinkedHashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object resKey = record.getValueByKey("residenciaId");
                Object valorObj = record.getValue();
                Instant tempo = record.getTime();
                if (resKey != null && valorObj instanceof Number valorNum) {
                    String resId = resKey.toString();
                    leiturasPorResidencia
                            .computeIfAbsent(resId, k -> new ArrayList<>())
                            .add(new LeituraTemporalDTO(tempo, valorNum.doubleValue()));
                }
            }
        }

        return leiturasPorResidencia;
    }
}