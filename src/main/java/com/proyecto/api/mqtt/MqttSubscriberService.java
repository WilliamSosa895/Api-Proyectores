package com.proyecto.api.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.api.service.DispositivoService;
import com.proyecto.api.service.EventoSistemaService;
import com.proyecto.api.service.LuxService;
import com.proyecto.api.websocket.WebSocketEventPublisher;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio suscriptor de topics MQTT de estado de sensores y actuadores.
 */
@Service
public class MqttSubscriberService {

    private static final Logger log = LoggerFactory.getLogger(MqttSubscriberService.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final MqttClient              mqttClient;
    private final LuxService              luxService;
    private final DispositivoService      dispositivoService;
    private final EventoSistemaService    eventoService;
    private final WebSocketEventPublisher wsPublisher;

    @Value("${mqtt.topic.subscribe}")
    private String subscribePattern; // "aulas/+/+/state"

    /**
     * Constructor con inyeccion de cliente MQTT y servicios de procesamiento.
     *
     * @param mqttClient cliente MQTT compartido
     * @param luxService servicio para persistir lecturas de lux
     * @param dispositivoService servicio para actualizar estados de actuadores
     * @param wsPublisher publicador WebSocket para notificaciones en tiempo real
     */
    public MqttSubscriberService(MqttClient mqttClient,
                                  LuxService luxService,
                                  DispositivoService dispositivoService,
                                  EventoSistemaService eventoService,
                                  WebSocketEventPublisher wsPublisher) {
        this.mqttClient         = mqttClient;
        this.luxService         = luxService;
        this.dispositivoService = dispositivoService;
        this.eventoService      = eventoService;
        this.wsPublisher        = wsPublisher;
    }

    /**
     * Realiza la suscripcion inicial al patron configurado de estados MQTT.
     */
    @PostConstruct
    public void init() {
        try {
            mqttClient.subscribe(subscribePattern, 1, this::onMessageArrived);
            log.info("[MQTT-Sub] Suscrito a: {}", subscribePattern);
        } catch (MqttException e) {
            log.error("[MQTT-Sub] Error al suscribirse: {}", e.getMessage());
            throw new RuntimeException("No se pudo suscribir a EMQX", e);
        }
    }

    /**
     * Maneja cada mensaje recibido, enruta por tipo y publica eventos WebSocket.
     *
     * @param topic topic MQTT de origen
     * @param message mensaje MQTT con payload en JSON
     */
    private void onMessageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.debug("[MQTT-Sub] {} → {}", topic, payload);

        try {
            String[] parts = topic.split("/");
            if (parts.length < 4) {
                log.warn("[MQTT-Sub] Topic con formato inesperado: {}", topic);
                return;
            }

            String aulaId = parts[1]; // "aula-1"
            String tipo   = parts[2]; // "lux_sensor", "light", "blind", etc.
            int idAula    = extraerIdAula(aulaId);

            JsonNode node = JSON.readTree(payload);

            if ("lux_sensor".equals(tipo)) {
                luxService.procesarSnapshot(aulaId, node);
                eventoService.registrar("lectura_sensor", idAula, topic, payload);
                wsPublisher.publicarLux(aulaId, payload);

            } else {
                dispositivoService.actualizarEstado(aulaId, tipo, node);
                eventoService.registrar("estado_actualizado", idAula, topic, payload);
                wsPublisher.publicarEstadoDispositivo(aulaId, tipo, payload);
            }

        } catch (Exception e) {
            log.error("[MQTT-Sub] Error procesando mensaje de {}: {}", topic, e.getMessage());
        }
    }

    private int extraerIdAula(String aulaId) {
        String[] parts = aulaId.split("-");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
