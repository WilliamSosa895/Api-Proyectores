package com.proyecto.api.controller;

import com.proyecto.api.model.LecturaLux;
import com.proyecto.api.service.LuxService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<LecturaLux> historial(
            @PathVariable int idAula,
            @RequestParam(defaultValue = "100") int limite) {
        return luxService.obtenerHistorial(idAula, limite);
    }
}
