package com.proyecto.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "estado", nullable = false)
    private String estado = "activo";

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    public int    getIdUsuario()          { return idUsuario; }
    public void   setIdUsuario(int v)     { this.idUsuario = v; }

    public String getNombre()             { return nombre; }
    public void   setNombre(String v)     { this.nombre = v; }

    public Rol    getRol()                { return rol; }
    public void   setRol(Rol v)          { this.rol = v; }

    public String getEstado()             { return estado; }
    public void   setEstado(String v)     { this.estado = v; }

    public String getPasswordHash()       { return passwordHash; }
    public void   setPasswordHash(String v) { this.passwordHash = v; }
}
