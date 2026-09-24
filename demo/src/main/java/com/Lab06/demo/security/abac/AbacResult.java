package com.Lab06.demo.security.abac;

public class AbacResult {

    private final boolean permitido;
    private final String motivo;

    public AbacResult(boolean permitido, String motivo) {
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