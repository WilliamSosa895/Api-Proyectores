package com.proyecto.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configura WebSocket con STOMP para comunicacion en tiempo real con clientes.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registra el endpoint de handshake WebSocket.
     *
     * <p>El endpoint {@code /ws} acepta conexiones directas y fallback SockJS.</p>
     *
     * @param registry registro de endpoints STOMP
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Define el broker simple y el prefijo para destinos de aplicacion.
     *
     * @param registry configuracion del broker de mensajeria STOMP
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
