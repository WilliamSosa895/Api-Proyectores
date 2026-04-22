package com.proyecto.api.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.api.service.EventoSistemaService;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio que publica comandos MQTT hacia los emuladores de dispositivos.
 */
@Service
public class MqttPublisherService {

    private static final Logger log = LoggerFactory.getLogger(MqttPublisherService.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final MqttClient mqttClient;
    private final EventoSistemaService eventoService;

    /**
     * Constructor con inyeccion del cliente MQTT compartido.
     *
     * @param mqttClient cliente MQTT configurado por Spring
     */
    public MqttPublisherService(MqttClient mqttClient,
                                EventoSistemaService eventoService) {
        this.mqttClient = mqttClient;
        this.eventoService = eventoService;
    }

    /**
     * Publica un comando MQTT para un dispositivo especifico de un aula.
     *
     * @param aulaId identificador logico de aula
     * @param tipo tipo de dispositivo destino
     * @param action accion a ejecutar
     */
    public void publicarComando(String aulaId, String tipo, String action) {
        String topic   = "aulas/" + aulaId + "/" + tipo + "/cmd";
        String payload;
        try {
            payload = JSON.writeValueAsString(Map.of("action", action));
        } catch (Exception e) {
            payload = "{\"action\":\"" + action + "\"}";
        }

        try {
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(1);
            msg.setRetained(false);
            mqttClient.publish(topic, msg);
            eventoService.registrarComando(extraerIdAula(aulaId), topic, payload);
            log.info("[MQTT-Pub] {} → {}", topic, payload);
        } catch (MqttException e) {
            log.error("[MQTT-Pub] Error publicando en {}: {}", topic, e.getMessage());
            throw new RuntimeException("Error al enviar comando MQTT", e);
        }
    }

    private int extraerIdAula(String aulaId) {
        String[] parts = aulaId.split("-");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    /**
     * Solicita apagar las luces del aula.
     *
     * @param aulaId identificador logico de aula
     */
    public void apagarLuces(String aulaId) {
        publicarComando(aulaId, "light", "TURN_OFF");
    }

    /**
     * Solicita encender las luces del aula.
     *
     * @param aulaId identificador logico de aula
     */
    public void encenderLuces(String aulaId) {
        publicarComando(aulaId, "light", "TURN_ON");
    }

    /**
     * Solicita cerrar las persianas del aula.
     *
     * @param aulaId identificador logico de aula
     */
    public void bajarPersianas(String aulaId) {
        publicarComando(aulaId, "blind", "CLOSE");
    }

    /**
     * Solicita abrir las persianas del aula.
     *
     * @param aulaId identificador logico de aula
     */
    public void subirPersianas(String aulaId) {
        publicarComando(aulaId, "blind", "OPEN");
    }

    /**
     * Solicita desplegar el telon de proyeccion.
     *
     * @param aulaId identificador logico de aula
     */
    public void bajarTelon(String aulaId) {
        publicarComando(aulaId, "screen", "DEPLOY");
    }

    /**
     * Solicita retraer el telon de proyeccion.
     *
     * @param aulaId identificador logico de aula
     */
    public void recogerTelon(String aulaId) {
        publicarComando(aulaId, "screen", "RETRACT");
    }

    /**
     * Solicita encender el proyector.
     *
     * @param aulaId identificador logico de aula
     */
    public void encenderProyector(String aulaId) {
        publicarComando(aulaId, "projector", "TURN_ON");
    }

    /**
     * Solicita apagar el proyector.
     *
     * @param aulaId identificador logico de aula
     */
    public void apagarProyector(String aulaId) {
        publicarComando(aulaId, "projector", "TURN_OFF");
    }

    /**
     * Solicita apagar el monitor del aula.
     *
     * @param aulaId identificador logico de aula
     */
    public void apagarMonitor(String aulaId) {
        publicarComando(aulaId, "monitor", "TURN_OFF");
    }
}
