package com.proyecto.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.api.model.CausaLux;
import com.proyecto.api.model.LecturaLux;
import com.proyecto.api.repository.CausaLuxRepository;
import com.proyecto.api.repository.LecturaLuxRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de transformar snapshots MQTT de lux en registros de base de datos.
 */
@Service
public class LuxService {

    private static final Logger log = LoggerFactory.getLogger(LuxService.class);
    private static final String CAUSA_POR_DEFECTO = "STABLE";

    private final LecturaLuxRepository lecturaLuxRepo;
    private final CausaLuxRepository   causaLuxRepo;
    private final Map<String, CausaLux> causaCache = new HashMap<>();

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

    @PostConstruct
    public void init() {
        causaLuxRepo.findAll().forEach(c -> causaCache.put(c.getNombre(), c));
        log.info("[LuxService] Catalogo causas_lux cargado: {} entradas", causaCache.size());
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
            String nombreCausa = node.has("cause") ? node.get("cause").asText() : CAUSA_POR_DEFECTO;
            CausaLux causa = obtenerCausa(nombreCausa);

            LecturaLux lectura = new LecturaLux();
            lectura.setIdAula(idAula);
            lectura.setValorLux(node.get("luxValue").asInt());
            lectura.setCausa(causa);
            lectura.setTimestamp(Instant.now());

            lecturaLuxRepo.save(lectura);
            log.debug("[LuxService] Lux persistido: aula={} lux={} causa={}",
                    aulaId, lectura.getValorLux(), lectura.getCausa().getNombre());

        } catch (Exception e) {
            log.error("[LuxService] Error al persistir snapshot de {}: {}", aulaId, e.getMessage());
        }
    }

    public List<LecturaLux> obtenerHistorial(int idAula, int limite) {
        return lecturaLuxRepo.findTopNByIdAulaOrderByTimestampDesc(idAula, limite);
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
        CausaLux causa = causaCache.get(nombreCausa);
        if (causa != null) {
            return causa;
        }

        CausaLux fallback = causaCache.get(CAUSA_POR_DEFECTO);
        if (fallback != null) {
            return fallback;
        }

        return causaLuxRepo.findByNombre(nombreCausa)
                .or(() -> causaLuxRepo.findByNombre(CAUSA_POR_DEFECTO))
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la causa '" + nombreCausa + "' ni la causa por defecto '" + CAUSA_POR_DEFECTO + "'"));
    }
}
