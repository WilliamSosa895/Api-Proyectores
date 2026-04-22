package com.proyecto.api.service;

import com.proyecto.api.model.EventoSistema;
import com.proyecto.api.model.TipoEvento;
import com.proyecto.api.repository.EventoSistemaRepository;
import com.proyecto.api.repository.TipoEventoRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventoSistemaService {

    private static final Logger log = LoggerFactory.getLogger(EventoSistemaService.class);

    private final EventoSistemaRepository eventoRepo;
    private final TipoEventoRepository    tipoEventoRepo;

    // Cache del catálogo tipo_de_evento — 5 filas fijas
    private final Map<String, TipoEvento> tipoCache = new HashMap<>();

    public EventoSistemaService(EventoSistemaRepository eventoRepo,
                                 TipoEventoRepository tipoEventoRepo) {
        this.eventoRepo     = eventoRepo;
        this.tipoEventoRepo = tipoEventoRepo;
    }

    @PostConstruct
    public void init() {
        tipoEventoRepo.findAll().forEach(t -> tipoCache.put(t.getDescripcion(), t));
        log.info("[EventoService] Catálogo tipo_de_evento cargado: {} entradas", tipoCache.size());
    }

    // ------------------------------------------------------------------ registrar
    // Llamado por MqttSubscriberService en cada mensaje recibido.
    // tipo puede ser: lectura_sensor, estado_actualizado, error

    public void registrar(String tipo, Integer idAula, String topico, String payload) {
        try {
            TipoEvento tipoEvento = tipoCache.get(tipo);
            if (tipoEvento == null) {
                log.warn("[EventoService] Tipo de evento desconocido: {}", tipo);
                return;
            }

            EventoSistema evento = new EventoSistema();
            evento.setIdAula(idAula);
            evento.setTipoEvento(tipoEvento);
            evento.setTopicoMqtt(topico);
            evento.setPayload(payload);
            evento.setTimestamp(Instant.now());

            eventoRepo.save(evento);
        } catch (Exception e) {
            log.error("[EventoService] Error registrando evento {}: {}", tipo, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ registrarComando
    // Llamado por MqttPublisherService cuando Spring Boot publica un comando

    public void registrarComando(Integer idAula, String topico, String payload) {
        registrar("comando_enviado", idAula, topico, payload);
    }

    // ------------------------------------------------------------------ registrarSolicitud
    // Llamado por SolicitudService al crear una solicitud nueva

    public void registrarSolicitud(Integer idAula, String payload) {
        registrar("solicitud_recibida", idAula, "sistema/solicitudes", payload);
    }

    // ------------------------------------------------------------------ obtenerPorAula

    public List<EventoSistema> obtenerPorAula(int idAula) {
        return eventoRepo.findTop100ByIdAulaOrderByTimestampDesc(idAula);
    }

    // ------------------------------------------------------------------ obtenerPorTipo

    public List<EventoSistema> obtenerPorTipo(String tipo) {
        return eventoRepo.findByTipoEventoDescripcionOrderByTimestampDesc(tipo);
    }
}
