package com.proyecto.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.api.model.CausaLux;
import com.proyecto.api.model.LecturaLux;
import com.proyecto.api.repository.CausaLuxRepository;
import com.proyecto.api.repository.LecturaLuxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio encargado de transformar snapshots MQTT de lux en registros de base de datos.
 */
@Service
public class LuxService {

    private static final Logger log = LoggerFactory.getLogger(LuxService.class);
    private static final String CAUSA_POR_DEFECTO = "STABLE";

    private final LecturaLuxRepository lecturaLuxRepo;
    private final CausaLuxRepository   causaLuxRepo;

    /**
     * Constructor con inyeccion del repositorio de lecturas lux.
     *
     * @param lecturaLuxRepo repositorio de persistencia de lecturas
     * @param causaLuxRepo repositorio del catalogo de causas de lux
     */
    public LuxService(LecturaLuxRepository lecturaLuxRepo,
                      CausaLuxRepository causaLuxRepo) {
        this.lecturaLuxRepo = lecturaLuxRepo;
        this.causaLuxRepo   = causaLuxRepo;
    }

    /**
     * Procesa un payload de lux recibido por MQTT y lo persiste como lectura historica.
     *
     * @param aulaId identificador logico de aula con formato aula-N
     * @param node payload JSON recibido del emulador de lux
     */
    public void procesarSnapshot(String aulaId, JsonNode node) {
        try {
            int idAula = extraerIdAula(aulaId);

            LecturaLux lectura = new LecturaLux();
            lectura.setIdAula(idAula);
            lectura.setValorLux(node.get("luxValue").asInt());
                String nombreCausa = node.has("cause") ? node.get("cause").asText() : CAUSA_POR_DEFECTO;
                lectura.setCausa(obtenerCausa(nombreCausa));
            lectura.setTimestamp(Instant.now());

            lecturaLuxRepo.save(lectura);
            log.debug("[LuxService] Lux persistido: aula={} lux={} causa={}",
                    aulaId, lectura.getValorLux(), lectura.getCausa().getNombre());

        } catch (Exception e) {
            log.error("[LuxService] Error al persistir snapshot de {}: {}", aulaId, e.getMessage());
        }
    }

    /**
     * Convierte un identificador logico aula-N al id numerico almacenado en BD.
     *
     * @param aulaId identificador logico recibido por MQTT
     * @return id numerico de aula
     */
    private int extraerIdAula(String aulaId) {
        String[] parts = aulaId.split("-");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    /**
     * Resuelve la causa por nombre y hace fallback a STABLE si no existe.
     *
     * @param nombreCausa nombre de la causa recibido por MQTT
     * @return entidad de causa existente en catalogo
     */
    private CausaLux obtenerCausa(String nombreCausa) {
        return causaLuxRepo.findByNombre(nombreCausa)
                .or(() -> causaLuxRepo.findByNombre(CAUSA_POR_DEFECTO))
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la causa '" + nombreCausa + "' ni la causa por defecto '" + CAUSA_POR_DEFECTO + "'"));
    }
}
