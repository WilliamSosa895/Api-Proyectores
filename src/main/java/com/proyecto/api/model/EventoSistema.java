package com.proyecto.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "eventos_sistema")
public class EventoSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private int idEvento;

    // Nullable — algunos eventos son del sistema general, no de un aula
    @Column(name = "id_aula")
    private Integer idAula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_evento", nullable = false)
    private TipoEvento tipoEvento;

    @Column(name = "topico_mqtt", nullable = false)
    private String topicoMqtt;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    public int        getIdEvento()              { return idEvento; }
    public void       setIdEvento(int v)         { this.idEvento = v; }

    public Integer    getIdAula()                { return idAula; }
    public void       setIdAula(Integer v)       { this.idAula = v; }

    public TipoEvento getTipoEvento()            { return tipoEvento; }
    public void       setTipoEvento(TipoEvento v){ this.tipoEvento = v; }

    public String     getTopicoMqtt()            { return topicoMqtt; }
    public void       setTopicoMqtt(String v)    { this.topicoMqtt = v; }

    public String     getPayload()               { return payload; }
    public void       setPayload(String v)       { this.payload = v; }

    public Instant    getTimestamp()             { return timestamp; }
    public void       setTimestamp(Instant v)    { this.timestamp = v; }
}
