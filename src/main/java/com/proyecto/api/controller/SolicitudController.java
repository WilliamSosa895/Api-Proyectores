package com.proyecto.api.controller;

import com.proyecto.api.security.JwtUtil;
import com.proyecto.api.service.SolicitudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para crear y consultar solicitudes de proyeccion.
 */
@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final JwtUtil jwtUtil;

    /**
     * Constructor con inyeccion del servicio de solicitudes.
     *
     * @param solicitudService servicio de orquestacion de solicitudes
     */
    public SolicitudController(SolicitudService solicitudService, JwtUtil jwtUtil) {
        this.solicitudService = solicitudService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registra una nueva solicitud y arranca su flujo de ejecucion asincrono.
     *
     * @param body cuerpo JSON con idAula e idUsuario
     * @return informacion inicial de la solicitud creada
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearSolicitud(
            @RequestBody Map<String, Integer> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        int idAula    = body.get("idAula");
        Integer idUsuarioBody = body.get("idUsuario");

        int idUsuario;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            idUsuario = jwtUtil.extraerIdUsuario(authHeader.substring(7));
        } else if (idUsuarioBody != null) {
            idUsuario = idUsuarioBody;
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "idUsuario es requerido si no se envia token"));
        }

        Map<String, Object> resultado = solicitudService.iniciarFlujoProyeccion(idAula, idUsuario);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Apaga el proyector de un aula.
     *
     * @param body cuerpo JSON con idAula e idUsuario
     * @return confirmacion de apagado
     */
    @PostMapping("/apagar")
    public ResponseEntity<Map<String, Object>> apagarProyector(
            @RequestBody Map<String, Integer> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        int idAula    = body.get("idAula");
        Integer idUsuarioBody = body.get("idUsuario");

        int idUsuario;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            idUsuario = jwtUtil.extraerIdUsuario(authHeader.substring(7));
        } else if (idUsuarioBody != null) {
            idUsuario = idUsuarioBody;
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "idUsuario es requerido si no se envia token"));
        }

        Map<String, Object> resultado = solicitudService.apagarProyector(idAula, idUsuario);
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

    /**
     * Consulta historial de solicitudes del usuario autenticado.
     *
     * @param authHeader encabezado Authorization con Bearer token
     * @return historial del usuario
     */
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<Map<String, Object>>> misSolicitudes(
            @RequestHeader("Authorization") String authHeader) {

        int idUsuario = jwtUtil.extraerIdUsuario(authHeader.substring(7));
        return ResponseEntity.ok(solicitudService.listarPorUsuario(idUsuario));
    }

    /**
     * Consulta historial de solicitudes por aula.
     *
     * @param idAula identificador de aula
     * @return historial del aula
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> porAula(@RequestParam int idAula) {
        return ResponseEntity.ok(solicitudService.listarPorAula(idAula));
    }
}
