package com.proyecto.api.dto;

public class LoginResponse {

    private final String token;
    private final int idUsuario;
    private final String nombre;
    private final String rol;
    private final long expiresIn;

    public LoginResponse(String token, int idUsuario, String nombre, String rol, long expiresIn) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.rol = rol;
        this.expiresIn = expiresIn;
    }

    public String getToken()     { return token; }
    public int getIdUsuario()    { return idUsuario; }
    public String getNombre()    { return nombre; }
    public String getRol()       { return rol; }
    public long getExpiresIn()   { return expiresIn; }
}
