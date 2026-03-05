package com.recantoceuazul.api.service;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class MqttService {

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) {
        String payload = message.getPayload().toString();
        System.out.println("Mensagem recebida do MQTT: " + payload);
        
        // Aqui você coloca sua lógica de negócio (salvar no banco, etc)
    }
}
