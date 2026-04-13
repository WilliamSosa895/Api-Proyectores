package com.proyecto.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.api.model.LecturaLux;
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

    private final LecturaLuxRepository lecturaLuxRepo;

    /**
     * Constructor con inyeccion del repositorio de lecturas lux.
     *
     * @param lecturaLuxRepo repositorio de persistencia de lecturas
     */
    public LuxService(LecturaLuxRepository lecturaLuxRepo) {
        this.lecturaLuxRepo = lecturaLuxRepo;
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
            lectura.setCausa(node.has("cause") ? node.get("cause").asText() : "STABLE");
            lectura.setTimestamp(Instant.now());

            lecturaLuxRepo.save(lectura);
            log.debug("[LuxService] Lux persistido: aula={} lux={} causa={}",
                    aulaId, lectura.getValorLux(), lectura.getCausa());

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
}
