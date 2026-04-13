package com.proyecto.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.api.model.AccionDispositivo;
import com.proyecto.api.model.Dispositivo;
import com.proyecto.api.repository.AccionDispositivoRepository;
import com.proyecto.api.repository.DispositivoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Servicio de sincronizacion de estados de actuadores en base de datos.
 */
@Service
public class DispositivoService {

    private static final Logger log = LoggerFactory.getLogger(DispositivoService.class);

    private final DispositivoRepository       dispositivoRepo;
    private final AccionDispositivoRepository accionRepo;

    /**
     * Constructor con inyeccion de dependencias de dispositivos y auditoria.
     *
     * @param dispositivoRepo repositorio de dispositivos
     * @param accionRepo repositorio de acciones ejecutadas
     */
    public DispositivoService(DispositivoRepository dispositivoRepo,
                               AccionDispositivoRepository accionRepo) {
        this.dispositivoRepo = dispositivoRepo;
        this.accionRepo      = accionRepo;
    }

    /**
     * Actualiza el estado de un dispositivo y registra la accion ejecutada.
     *
     * @param aulaId identificador logico de aula
     * @param tipo tipo de actuador reportado en el topic MQTT
     * @param node payload JSON del estado recibido
     */
    public void actualizarEstado(String aulaId, String tipo, JsonNode node) {
        try {
            int idAula = extraerIdAula(aulaId);
            Optional<Dispositivo> optDispositivo =
                    dispositivoRepo.findByIdAulaAndTipoNombre(idAula, tipo);

            if (optDispositivo.isEmpty()) {
                log.warn("[DispositivoService] Dispositivo no encontrado: aula={} tipo={}", aulaId, tipo);
                return;
            }

            Dispositivo dispositivo = optDispositivo.get();
            String estadoAnterior   = dispositivo.getEstadoActual();
            String estadoNuevo      = node.has("state") ? node.get("state").asText() : "UNKNOWN";

            dispositivo.setEstadoActual(estadoNuevo);
            dispositivoRepo.save(dispositivo);

            if (node.has("action")) {
                AccionDispositivo accion = new AccionDispositivo();
                accion.setIdDispositivo(dispositivo.getIdDispositivo());
                accion.setAccion(node.get("action").asText());
                accion.setEstadoAnterior(estadoAnterior);
                accion.setEstadoNuevo(estadoNuevo);
                accion.setEjecutor(node.has("executor") ? node.get("executor").asText() : "EMULADOR");
                accion.setTimestamp(Instant.now());
                accionRepo.save(accion);
            }

            log.debug("[DispositivoService] {} → {} (aula={})", tipo, estadoNuevo, aulaId);

        } catch (Exception e) {
            log.error("[DispositivoService] Error actualizando {}/{}: {}", aulaId, tipo, e.getMessage());
        }
    }

    /**
     * Convierte aula-N al identificador numerico de aula usado en BD.
     *
     * @param aulaId identificador logico de aula
     * @return id numerico de aula
     */
    private int extraerIdAula(String aulaId) {
        String[] parts = aulaId.split("-");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
