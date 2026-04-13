package com.proyecto.api.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Publicador de eventos STOMP para notificar cambios en tiempo real al cliente web.
 */
@Service
public class WebSocketEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventPublisher.class);

    private final SimpMessagingTemplate stomp;

    /**
     * Constructor con inyeccion de plantilla STOMP para envio de mensajes.
     *
     * @param stomp plantilla de mensajeria de Spring
     */
    public WebSocketEventPublisher(SimpMessagingTemplate stomp) {
        this.stomp = stomp;
    }

    /**
     * Publica una lectura de lux hacia los clientes suscritos del aula.
     *
     * @param aulaId identificador logico de aula
     * @param payloadJson payload JSON de lectura lux
     */
    public void publicarLux(String aulaId, String payloadJson) {
        String destino = "/topic/aulas/" + aulaId + "/lux";
        stomp.convertAndSend(destino, payloadJson);
        log.debug("[WS] Lux publicado en {}", destino);
    }

    /**
     * Publica cambio de estado de actuadores hacia el topic del aula.
     *
     * @param aulaId identificador logico de aula
     * @param tipo tipo de dispositivo que cambio de estado
     * @param payloadJson payload JSON del estado del dispositivo
     */
    public void publicarEstadoDispositivo(String aulaId, String tipo, String payloadJson) {
        String destino = "/topic/aulas/" + aulaId + "/dispositivos";
        stomp.convertAndSend(destino, payloadJson);
        log.debug("[WS] Estado de {} publicado en {}", tipo, destino);
    }
}
