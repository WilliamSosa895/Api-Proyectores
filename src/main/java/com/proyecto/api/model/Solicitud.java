package com.proyecto.api.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entidad que representa una solicitud de preparacion de aula para proyeccion.
 */
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private int idSolicitud;

    @Column(name = "id_usuario", nullable = false)
    private int idUsuario;

    @Column(name = "id_aula", nullable = false)
    private int idAula;

    @Column(name = "fecha_solicitud", nullable = false)
    private Instant fechaSolicitud;

    @Column(name = "estado")
    private String estado;

    @Column(name = "detalle")
    private String detalle;

    /**
     * Obtiene el identificador de la solicitud.
     *
     * @return id de solicitud
     */
    public int     getIdSolicitud()              { return idSolicitud; }

    /**
     * Define el identificador de la solicitud.
     *
     * @param v id de solicitud
     */
    public void    setIdSolicitud(int v)         { this.idSolicitud = v; }

    /**
     * Obtiene el usuario que genero la solicitud.
     *
     * @return id de usuario
     */
    public int     getIdUsuario()                { return idUsuario; }

    /**
     * Define el usuario que genero la solicitud.
     *
     * @param v id de usuario
     */
    public void    setIdUsuario(int v)           { this.idUsuario = v; }

    /**
     * Obtiene el aula asociada a la solicitud.
     *
     * @return id del aula
     */
    public int     getIdAula()                   { return idAula; }

    /**
     * Define el aula asociada a la solicitud.
     *
     * @param v id del aula
     */
    public void    setIdAula(int v)              { this.idAula = v; }

    /**
     * Obtiene la fecha y hora de creacion de la solicitud.
     *
     * @return instante de solicitud
     */
    public Instant getFechaSolicitud()           { return fechaSolicitud; }

    /**
     * Define la fecha y hora de creacion de la solicitud.
     *
     * @param v instante de solicitud
     */
    public void    setFechaSolicitud(Instant v)  { this.fechaSolicitud = v; }

    /**
     * Obtiene el estado de ejecucion de la solicitud.
     *
     * @return estado actual
     */
    public String  getEstado()                   { return estado; }

    /**
     * Define el estado de ejecucion de la solicitud.
     *
     * @param v estado nuevo
     */
    public void    setEstado(String v)           { this.estado = v; }

    /**
     * Obtiene informacion adicional asociada a la solicitud.
     *
     * @return detalle textual
     */
    public String  getDetalle()                  { return detalle; }

    /**
     * Define informacion adicional asociada a la solicitud.
     *
     * @param v detalle textual
     */
    public void    setDetalle(String v)          { this.detalle = v; }
}
