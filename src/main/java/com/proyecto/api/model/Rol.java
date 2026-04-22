package com.proyecto.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private int idRol;

    @Column(name = "nombre_rol", nullable = false, unique = true)
    private String nombreRol;

    public int    getIdRol()             { return idRol; }
    public void   setIdRol(int v)        { this.idRol = v; }

    public String getNombreRol()         { return nombreRol; }
    public void   setNombreRol(String v) { this.nombreRol = v; }
}
