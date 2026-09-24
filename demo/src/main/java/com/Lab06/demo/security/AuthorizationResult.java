package com.Lab06.demo.security;

public class AuthorizationResult {

    private final boolean permitido;
    private final String motivo;

    public AuthorizationResult(
            boolean permitido,
            String motivo) {

        this.permitido = permitido;
        this.motivo = motivo;
    }

    public boolean isPermitido() {
        return permitido;
    }

    public String getMotivo() {
        return motivo;
    }
}