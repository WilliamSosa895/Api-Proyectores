package com.proyecto.api.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion del cliente MQTT compartido por la aplicacion.
 *
 * <p>Define un unico bean {@link MqttClient} para publicar y suscribirse a
 * eventos del broker.</p>
 */
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    /**
     * Crea y conecta el cliente MQTT al iniciar la aplicacion.
     *
     * @return cliente MQTT conectado y listo para uso
     * @throws MqttException si no se puede crear o conectar el cliente
     */
    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, null);

        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setCleanSession(true);
        opts.setAutomaticReconnect(true);
        opts.setConnectionTimeout(10);
        opts.setKeepAliveInterval(30);

        client.connect(opts);
        return client;
    }
}
