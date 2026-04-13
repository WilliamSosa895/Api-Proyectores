package com.proyecto.api.controller;

import com.proyecto.api.service.SolicitudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para crear y consultar solicitudes de proyeccion.
 */
@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    private final SolicitudService solicitudService;

    /**
     * Constructor con inyeccion del servicio de solicitudes.
     *
     * @param solicitudService servicio de orquestacion de solicitudes
     */
    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    /**
     * Registra una nueva solicitud y arranca su flujo de ejecucion asincrono.
     *
     * @param body cuerpo JSON con idAula e idUsuario
     * @return informacion inicial de la solicitud creada
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearSolicitud(
            @RequestBody Map<String, Integer> body) {

        int idAula    = body.get("idAula");
        int idUsuario = body.get("idUsuario");

        Map<String, Object> resultado = solicitudService.iniciarFlujoProyeccion(idAula, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Consulta el estado actual de una solicitud existente.
     *
     * @param id identificador de solicitud
     * @return estado y metadatos de la solicitud
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerSolicitud(@PathVariable int id) {
        Map<String, Object> estado = solicitudService.obtenerEstado(id);
        return ResponseEntity.ok(estado);
    }
}
