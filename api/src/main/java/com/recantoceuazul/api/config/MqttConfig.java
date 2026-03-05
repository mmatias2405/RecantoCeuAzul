package com.recantoceuazul.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {

    // Injetando as propriedades
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.broker.username}")
    private String username;

    @Value("${mqtt.broker.password}")
    private String password;

    @Value("${mqtt.broker.client-id}")
    private String clientId;

    @Value("${mqtt.broker.topic}")
    private String defaultTopic;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        
        // Usando as variáveis injetadas em vez de strings fixas
        options.setServerURIs(new String[] { brokerUrl });
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        // Usando o clientId e o tópico injetados
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory(), defaultTopic);
        
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
}