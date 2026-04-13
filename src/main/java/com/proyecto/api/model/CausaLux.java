package com.proyecto.api.model;

import jakarta.persistence.*;

/**
 * Entidad catalogo de causas de cambio de iluminancia.
 */
@Entity
@Table(name = "causas_lux")
public class CausaLux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_causa")
    private int idCausa;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    public int getIdCausa() {
        return idCausa;
    }

    public void setIdCausa(int idCausa) {
        this.idCausa = idCausa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}