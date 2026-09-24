package com.Lab06.demo.dto;

public class LoginResponse {

    private String token;
    private String tipo;
    private Long usuarioId;
    private String nombre;
    private String correo;
    private String rol;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            String tipo,
            Long usuarioId,
            String nombre,
            String correo,
            String rol) {

        this.token = token;
        this.tipo = tipo;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}