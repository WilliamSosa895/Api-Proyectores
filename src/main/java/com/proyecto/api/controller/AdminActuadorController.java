package com.proyecto.api.controller;

import com.proyecto.api.mqtt.MqttPublisherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

/**
 * Control manual de actuadores para el administrador.
 */
@RestController
@RequestMapping("/api/admin/aulas/{idAula}/actuadores")
@CrossOrigin(origins = "*")
public class AdminActuadorController {

    private final MqttPublisherService mqttPublisherService;

    public AdminActuadorController(MqttPublisherService mqttPublisherService) {
        this.mqttPublisherService = mqttPublisherService;
    }

    @PostMapping("/{tipo}")
    public ResponseEntity<Map<String, Object>> controlarActuador(
            @PathVariable int idAula,
            @PathVariable String tipo,
            @RequestBody Map<String, String> body) {

        String aulaId = "aula-" + idAula;
        String normalizedTipo = tipo.toLowerCase(Locale.ROOT);
        String action = body.getOrDefault("action", "").toUpperCase(Locale.ROOT);

        switch (normalizedTipo) {
            case "light" -> {
                if ("TURN_ON".equals(action)) mqttPublisherService.encenderLuces(aulaId);
                else if ("TURN_OFF".equals(action)) mqttPublisherService.apagarLuces(aulaId);
                else return ResponseEntity.badRequest().body(Map.of("error", "Accion invalida para luces"));
            }
            case "blind" -> {
                if ("OPEN".equals(action)) mqttPublisherService.subirPersianas(aulaId);
                else if ("CLOSE".equals(action)) mqttPublisherService.bajarPersianas(aulaId);
                else return ResponseEntity.badRequest().body(Map.of("error", "Accion invalida para persianas"));
            }
            case "screen" -> {
                if ("DEPLOY".equals(action)) mqttPublisherService.bajarTelon(aulaId);
                else if ("RETRACT".equals(action)) mqttPublisherService.recogerTelon(aulaId);
                else return ResponseEntity.badRequest().body(Map.of("error", "Accion invalida para telon"));
            }
            case "projector" -> {
                if ("TURN_ON".equals(action)) mqttPublisherService.encenderProyector(aulaId);
                else if ("TURN_OFF".equals(action)) mqttPublisherService.apagarProyector(aulaId);
                else return ResponseEntity.badRequest().body(Map.of("error", "Accion invalida para proyector"));
            }
            case "monitor" -> {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "El monitor no se controla manualmente desde el admin"
                ));
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Tipo de actuador invalido"));
            }
        }

        return ResponseEntity.ok(Map.of(
            "mensaje", "Comando enviado",
            "idAula", idAula,
            "aulaId", aulaId,
            "tipo", normalizedTipo,
            "action", action
        ));
    }
}