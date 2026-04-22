package com.proyecto.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_de_evento")
public class TipoEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_evento")
    private int idTipoEvento;

    // Valores: solicitud_recibida, comando_enviado, estado_actualizado,
    //          lectura_sensor, error
    @Column(name = "descripcion", nullable = false, unique = true)
    private String descripcion;

    public int    getIdTipoEvento()          { return idTipoEvento; }
    public void   setIdTipoEvento(int v)     { this.idTipoEvento = v; }

    public String getDescripcion()           { return descripcion; }
    public void   setDescripcion(String v)   { this.descripcion = v; }
}
