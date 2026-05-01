package com.recantoceuazul.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    @Autowired
    private InfluxDbService influxDbService;

    @Autowired
    private ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String payload = message.getPayload().toString();
        
        try {
            // Converte a string JSON recebida em um objeto navegável
            JsonNode jsonNode = objectMapper.readTree(payload);


            // Valida se os campos necessários existem no JSON antes de tentar ler
            if (jsonNode.has("residenciaId") && jsonNode.has("volume_litros")) {
                
                String residenciaId = jsonNode.get("residenciaId").asText();
                double volumeLitros = jsonNode.get("volume_litros").asDouble();

                // Dispara a gravação no banco de dados em série temporal
                influxDbService.processarMensagemMqtt(residenciaId, volumeLitros);
            } 
            else {
            
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}