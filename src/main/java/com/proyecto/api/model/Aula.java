package com.proyecto.api.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un aula fisica dentro del sistema.
 */
@Entity
@Table(name = "aulas")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aula")
    private int idAula;

    @Column(name = "ubicacion")
    private String ubicacion;

    @Column(name = "estado")
    private String estado;

    /**
     * Obtiene el identificador unico del aula.
     *
     * @return id del aula
     */
    public int    getIdAula()           { return idAula; }

    /**
     * Define el identificador unico del aula.
     *
     * @param v id del aula
     */
    public void   setIdAula(int v)      { this.idAula = v; }

    /**
     * Obtiene la ubicacion fisica del aula.
     *
     * @return ubicacion del aula
     */
    public String getUbicacion()        { return ubicacion; }

    /**
     * Define la ubicacion fisica del aula.
     *
     * @param v ubicacion del aula
     */
    public void   setUbicacion(String v){ this.ubicacion = v; }

    /**
     * Obtiene el estado general del aula.
     *
     * @return estado actual
     */
    public String getEstado()           { return estado; }

    /**
     * Define el estado general del aula.
     *
     * @param v nuevo estado
     */
    public void   setEstado(String v)   { this.estado = v; }
}
