package com.proyecto.api.controller;

import com.proyecto.api.model.LecturaLux;
import com.proyecto.api.service.LuxService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller que expone el historial de lecturas de lux.
 * Devuelve objetos JSON con campos `valorLux` y `timestamp` (epoch ms) para evitar ambigüedades.
 */

@RestController
@RequestMapping("/api/aulas/{idAula}/lux")
@CrossOrigin(origins = "*")
public class LuxController {

    private final LuxService luxService;

    public LuxController(LuxService luxService) {
        this.luxService = luxService;
    }

    // GET /api/aulas/1/lux?limite=100
    // React llama este endpoint al cargar el panel para obtener el historial
    // previo de la gráfica. Las actualizaciones en tiempo real llegan por WebSocket.
    @GetMapping
    public List<Map<String, Object>> historial(
            @PathVariable int idAula,
            @RequestParam(defaultValue = "100") int limite) {
        List<LecturaLux> rows = luxService.obtenerHistorial(idAula, limite);

        return rows.stream().map(r -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("valorLux", r.getValorLux());
            payload.put("timestamp", r.getTimestamp() != null ? r.getTimestamp().toEpochMilli() : 0L);
            return payload;
        }).collect(Collectors.toList());
    }
}
