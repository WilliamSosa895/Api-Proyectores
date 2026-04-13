package com.proyecto.api.model;

import jakarta.persistence.*;

/**
 * Entidad que modela un dispositivo instalado en un aula.
 */
@Entity
@Table(name = "dispositivos")
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dispositivo")
    private int idDispositivo;

    @Column(name = "id_aula", nullable = false)
    private int idAula;

    /** Tipo funcional del dispositivo. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoDispositivo tipo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "estado_actual")
    private String estadoActual;

    /**
     * Obtiene el identificador del dispositivo.
     *
     * @return id del dispositivo
     */
    public int             getIdDispositivo()          { return idDispositivo; }

    /**
     * Define el identificador del dispositivo.
     *
     * @param v id del dispositivo
     */
    public void            setIdDispositivo(int v)     { this.idDispositivo = v; }

    /**
     * Obtiene el aula propietaria del dispositivo.
     *
     * @return id del aula
     */
    public int             getIdAula()                 { return idAula; }

    /**
     * Define el aula propietaria del dispositivo.
     *
     * @param v id del aula
     */
    public void            setIdAula(int v)            { this.idAula = v; }

    /**
     * Obtiene el tipo de dispositivo asociado.
     *
     * @return tipo de dispositivo
     */
    public TipoDispositivo getTipo()                   { return tipo; }

    /**
     * Define el tipo de dispositivo asociado.
     *
     * @param v tipo de dispositivo
     */
    public void            setTipo(TipoDispositivo v)  { this.tipo = v; }

    /**
     * Obtiene el nombre de negocio del dispositivo.
     *
     * @return nombre del dispositivo
     */
    public String          getNombre()                 { return nombre; }

    /**
     * Define el nombre de negocio del dispositivo.
     *
     * @param v nombre del dispositivo
     */
    public void            setNombre(String v)         { this.nombre = v; }

    /**
     * Obtiene el estado actual del dispositivo.
     *
     * @return estado actual
     */
    public String          getEstadoActual()           { return estadoActual; }

    /**
     * Define el estado actual del dispositivo.
     *
     * @param v estado nuevo
     */
    public void            setEstadoActual(String v)   { this.estadoActual = v; }
}
