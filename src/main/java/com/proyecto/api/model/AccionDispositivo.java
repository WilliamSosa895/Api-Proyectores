package com.proyecto.api.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad de auditoria para acciones ejecutadas sobre dispositivos.
 */
@Entity
@Table(name = "acciones_dispositivo")
public class AccionDispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_accion")
    private int idAccion;

    @Column(name = "id_dispositivo", nullable = false)
    private int idDispositivo;

    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Column(name = "estado_anterior")
    private String estadoAnterior;

    @Column(name = "estado_nuevo")
    private String estadoNuevo;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "ejecutor")
    private String ejecutor;

    /**
     * Obtiene el identificador unico de la accion.
     *
     * @return id de accion
     */
    public int     getIdAccion()                { return idAccion; }

    /**
     * Define el identificador unico de la accion.
     *
     * @param v id de accion
     */
    public void    setIdAccion(int v)           { this.idAccion = v; }

    /**
     * Obtiene el dispositivo afectado por la accion.
     *
     * @return id del dispositivo
     */
    public int     getIdDispositivo()           { return idDispositivo; }

    /**
     * Define el dispositivo afectado por la accion.
     *
     * @param v id del dispositivo
     */
    public void    setIdDispositivo(int v)      { this.idDispositivo = v; }

    /**
     * Obtiene la solicitud origen, si existe asociacion.
     *
     * @return id de solicitud o null
     */
    public Integer getIdSolicitud()             { return idSolicitud; }

    /**
     * Define la solicitud origen asociada a la accion.
     *
     * @param v id de solicitud o null
     */
    public void    setIdSolicitud(Integer v)    { this.idSolicitud = v; }

    /**
     * Obtiene el nombre de la accion ejecutada.
     *
     * @return accion realizada
     */
    public String  getAccion()                  { return accion; }

    /**
     * Define el nombre de la accion ejecutada.
     *
     * @param v accion realizada
     */
    public void    setAccion(String v)          { this.accion = v; }

    /**
     * Obtiene el estado anterior del dispositivo.
     *
     * @return estado anterior
     */
    public String  getEstadoAnterior()          { return estadoAnterior; }

    /**
     * Define el estado anterior del dispositivo.
     *
     * @param v estado anterior
     */
    public void    setEstadoAnterior(String v)  { this.estadoAnterior = v; }

    /**
     * Obtiene el nuevo estado del dispositivo.
     *
     * @return estado nuevo
     */
    public String  getEstadoNuevo()             { return estadoNuevo; }

    /**
     * Define el nuevo estado del dispositivo.
     *
     * @param v estado nuevo
     */
    public void    setEstadoNuevo(String v)     { this.estadoNuevo = v; }

    /**
     * Obtiene el instante en que se registro la accion.
     *
     * @return timestamp de la accion
     */
    public Instant getTimestamp()               { return timestamp; }

    /**
     * Define el instante en que se registro la accion.
     *
     * @param v timestamp de la accion
     */
    public void    setTimestamp(Instant v)      { this.timestamp = v; }

    /**
     * Obtiene el actor que ejecuto la accion.
     *
     * @return ejecutor de la accion
     */
    public String  getEjecutor()                { return ejecutor; }

    /**
     * Define el actor que ejecuto la accion.
     *
     * @param v ejecutor de la accion
     */
    public void    setEjecutor(String v)        { this.ejecutor = v; }
}
