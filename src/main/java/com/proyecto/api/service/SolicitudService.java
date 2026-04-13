package com.proyecto.api.service;

import com.proyecto.api.model.Solicitud;
import com.proyecto.api.mqtt.MqttPublisherService;
import com.proyecto.api.repository.LecturaLuxRepository;
import com.proyecto.api.repository.SolicitudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de orquestacion del flujo automatico de proyeccion en aula.
 */
@Service
public class SolicitudService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudService.class);

    // Umbral de lux óptimo para proyección
    private static final int LUX_OPTIMO = 100;

    // Tiempo de espera para que el emulador ejecute la acción y el sensor
    // publique el nuevo valor de lux (processingDelay + transición completa)
    private static final int ESPERA_ACCION_MS = 4000;

    private final SolicitudRepository    solicitudRepo;
    private final LecturaLuxRepository   luxRepo;
    private final MqttPublisherService   mqtt;

    /**
     * Constructor con inyeccion de repositorios y publicador MQTT.
     *
     * @param solicitudRepo repositorio de solicitudes
     * @param luxRepo repositorio de lecturas de iluminacion
     * @param mqtt servicio publicador de comandos MQTT
     */
    public SolicitudService(SolicitudRepository solicitudRepo,
                             LecturaLuxRepository luxRepo,
                             MqttPublisherService mqtt) {
        this.solicitudRepo = solicitudRepo;
        this.luxRepo       = luxRepo;
        this.mqtt          = mqtt;
    }

    /**
     * Crea la solicitud en base de datos e inicia su flujo asincrono.
     *
     * @param idAula aula donde se solicita preparar la proyeccion
     * @param idUsuario usuario que realiza la solicitud
     * @return respuesta resumida para el cliente HTTP
     */
    public Map<String, Object> iniciarFlujoProyeccion(int idAula, int idUsuario) {
        Solicitud solicitud = new Solicitud();
        solicitud.setIdAula(idAula);
        solicitud.setIdUsuario(idUsuario);
        solicitud.setFechaSolicitud(Instant.now());
        solicitud.setEstado("PROCESANDO");
        solicitudRepo.save(solicitud);

        log.info("[Solicitud] Nueva solicitud #{} — aula={} usuario={}",
                solicitud.getIdSolicitud(), idAula, idUsuario);

        String aulaId = "aula-" + idAula;
        ejecutarFlujo(solicitud.getIdSolicitud(), aulaId);

        return Map.of(
            "idSolicitud", solicitud.getIdSolicitud(),
            "estado",      "PROCESANDO",
            "mensaje",     "Solicitud recibida. Ajustando condiciones del aula..."
        );
    }

    /**
     * Ejecuta el flujo automatico de preparacion del aula para proyeccion.
     *
     * <p>El metodo evalua lux actual y, si es necesario, apaga luces y baja
     * persianas antes de bajar telon y encender proyector.</p>
     *
     * @param idSolicitud solicitud a actualizar durante la ejecucion
     * @param aulaId identificador logico de aula con formato aula-N
     */
    @Async
    public void ejecutarFlujo(int idSolicitud, String aulaId) {
        log.info("[Flujo #{}] Iniciando para {}", idSolicitud, aulaId);

        try {
            int idAula = extraerIdAula(aulaId);

            int luxActual = obtenerLuxActual(idAula);
            log.info("[Flujo #{}] Lux actual: {}", idSolicitud, luxActual);

            if (luxActual > LUX_OPTIMO) {
                log.info("[Flujo #{}] Lux > {} → apagando luces", idSolicitud, LUX_OPTIMO);
                mqtt.apagarLuces(aulaId);

                Thread.sleep(ESPERA_ACCION_MS);
                luxActual = obtenerLuxActual(idAula);
                log.info("[Flujo #{}] Lux tras apagar luces: {}", idSolicitud, luxActual);
            }

            if (luxActual > LUX_OPTIMO) {
                log.info("[Flujo #{}] Lux > {} → bajando persianas", idSolicitud, LUX_OPTIMO);
                mqtt.bajarPersianas(aulaId);

                Thread.sleep(ESPERA_ACCION_MS);
                luxActual = obtenerLuxActual(idAula);
                log.info("[Flujo #{}] Lux tras bajar persianas: {}", idSolicitud, luxActual);
            }

            if (luxActual > LUX_OPTIMO) {
                log.warn("[Flujo #{}] Lux={} todavía > {} — procediendo de todas formas",
                        idSolicitud, luxActual, LUX_OPTIMO);
            }

            log.info("[Flujo #{}] Bajando telón", idSolicitud);
            mqtt.bajarTelon(aulaId);
            Thread.sleep(1000);

            log.info("[Flujo #{}] Encendiendo proyector", idSolicitud);
            mqtt.encenderProyector(aulaId);

            actualizarEstadoSolicitud(idSolicitud, "COMPLETADA");
            log.info("[Flujo #{}] Proyección lista en {}", idSolicitud, aulaId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            actualizarEstadoSolicitud(idSolicitud, "ERROR");
            log.error("[Flujo #{}] Flujo interrumpido", idSolicitud);
        } catch (Exception e) {
            actualizarEstadoSolicitud(idSolicitud, "ERROR");
            log.error("[Flujo #{}] Error en flujo: {}", idSolicitud, e.getMessage());
        }
    }

    /**
     * Consulta el estado actual de una solicitud.
     *
     * @param idSolicitud identificador de solicitud
     * @return mapa con datos de estado o mensaje de error
     */
    public Map<String, Object> obtenerEstado(int idSolicitud) {
        Optional<Solicitud> opt = solicitudRepo.findById(idSolicitud);
        if (opt.isEmpty()) {
            return Map.of("error", "Solicitud no encontrada");
        }
        Solicitud s = opt.get();
        return Map.of(
            "idSolicitud",    s.getIdSolicitud(),
            "estado",         s.getEstado(),
            "fechaSolicitud", s.getFechaSolicitud().toString()
        );
    }

    /**
     * Obtiene la lectura de lux mas reciente para un aula.
     *
     * @param idAula identificador numerico de aula
     * @return valor de lux actual o un valor alto por defecto
     */
    private int obtenerLuxActual(int idAula) {
        return luxRepo.findTopByIdAulaOrderByTimestampDesc(idAula)
                .map(l -> l.getValorLux())
                .orElse(200);
    }

    /**
     * Actualiza el estado de una solicitud si existe en base de datos.
     *
     * @param idSolicitud identificador de solicitud
     * @param estado nuevo estado a persistir
     */
    private void actualizarEstadoSolicitud(int idSolicitud, String estado) {
        solicitudRepo.findById(idSolicitud).ifPresent(s -> {
            s.setEstado(estado);
            solicitudRepo.save(s);
        });
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
