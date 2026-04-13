package com.proyecto.api.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad que almacena snapshots historicos de iluminacion por aula.
 */
@Entity
@Table(name = "lecturas_lux")
public class LecturaLux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lectura")
    private int idLectura;

    @Column(name = "id_aula", nullable = false)
    private int idAula;

    @Column(name = "valor_lux", nullable = false)
    private int valorLux;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_causa", nullable = false)
    private CausaLux causa;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /**
     * Obtiene el identificador de la lectura.
     *
     * @return id de lectura
     */
    public int     getIdLectura()          { return idLectura; }

    /**
     * Define el identificador de la lectura.
     *
     * @param v id de lectura
     */
    public void    setIdLectura(int v)     { this.idLectura = v; }

    /**
     * Obtiene el aula asociada a la lectura.
     *
     * @return id de aula
     */
    public int     getIdAula()             { return idAula; }

    /**
     * Define el aula asociada a la lectura.
     *
     * @param v id de aula
     */
    public void    setIdAula(int v)        { this.idAula = v; }

    /**
     * Obtiene el valor de iluminacion en lux.
     *
     * @return valor de lux
     */
    public int     getValorLux()           { return valorLux; }

    /**
     * Define el valor de iluminacion en lux.
     *
     * @param v valor de lux
     */
    public void    setValorLux(int v)      { this.valorLux = v; }

    /**
     * Obtiene la causa que origino el cambio de lux.
     *
     * @return causa del snapshot
     */
    public CausaLux getCausa()            { return causa; }

    /**
     * Define la causa que origino el cambio de lux.
     *
     * @param v causa del snapshot
     */
    public void    setCausa(CausaLux v)   { this.causa = v; }

    /**
     * Obtiene la fecha y hora del snapshot.
     *
     * @return instante de lectura
     */
    public Instant getTimestamp()          { return timestamp; }

    /**
     * Define la fecha y hora del snapshot.
     *
     * @param v instante de lectura
     */
    public void    setTimestamp(Instant v) { this.timestamp = v; }
}
