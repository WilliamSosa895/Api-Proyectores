package com.proyecto.api.model;

import jakarta.persistence.*;

/**
 * Entidad catalogo de tipos de dispositivo soportados por el sistema.
 */
@Entity
@Table(name = "tipos_dispositivo")
public class TipoDispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private int idTipo;

    @Column(name = "nombre_tipo", nullable = false)
    private String nombreTipo;

    /**
     * Obtiene el identificador del tipo de dispositivo.
     *
     * @return id del tipo
     */
    public int    getIdTipo()           { return idTipo; }

    /**
     * Define el identificador del tipo de dispositivo.
     *
     * @param v id del tipo
     */
    public void   setIdTipo(int v)      { this.idTipo = v; }

    /**
     * Obtiene el nombre logico del tipo de dispositivo.
     *
     * @return nombre del tipo
     */
    public String getNombreTipo()       { return nombreTipo; }

    /**
     * Define el nombre logico del tipo de dispositivo.
     *
     * @param v nombre del tipo
     */
    public void   setNombreTipo(String v){ this.nombreTipo = v; }
}
