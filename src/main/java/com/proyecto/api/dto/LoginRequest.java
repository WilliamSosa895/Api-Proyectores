package com.proyecto.api.dto;

public class LoginRequest {

    private String nombre;
    private String password;

    public String getNombre()           { return nombre; }
    public void   setNombre(String v)   { this.nombre = v; }

    public String getPassword()         { return password; }
    public void   setPassword(String v) { this.password = v; }
}
