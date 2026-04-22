package com.proyecto.api.controller;

import com.proyecto.api.model.EventoSistema;
import com.proyecto.api.service.EventoSistemaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
public class EventoSistemaController {

    private final EventoSistemaService eventoService;

    public EventoSistemaController(EventoSistemaService eventoService) {
        this.eventoService = eventoService;
    }

    // GET /api/eventos?idAula=1
    // Panel admin: bitácora de los últimos 100 eventos de un aula
    @GetMapping
    public List<EventoSistema> porAula(@RequestParam int idAula) {
        return eventoService.obtenerPorAula(idAula);
    }

    // GET /api/eventos/tipo?descripcion=lectura_sensor
    // Filtra por tipo: solicitud_recibida, comando_enviado, estado_actualizado,
    //                  lectura_sensor, error
    @GetMapping("/tipo")
    public List<EventoSistema> porTipo(@RequestParam String descripcion) {
        return eventoService.obtenerPorTipo(descripcion);
    }
}
