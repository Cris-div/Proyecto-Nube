package com.Lab06.demo.security;

import com.Lab06.demo.entity.Documento;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.security.abac.AbacResult;
import com.Lab06.demo.security.abac.AbacService;
import com.Lab06.demo.security.rbac.RbacService;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class AuthorizationService {

    private final RbacService rbacService;
    private final AbacService abacService;

    public AuthorizationService(
            RbacService rbacService,
            AbacService abacService) {

        this.rbacService = rbacService;
        this.abacService = abacService;
    }

    public AuthorizationResult autorizar(
            Usuario usuario,
            String permiso,
            Documento documento,
            LocalTime hora,
            boolean dispositivoCorporativo) {

        // =========================
        // PASO 1 - RBAC
        // =========================

        boolean tienePermiso =
                rbacService.tienePermiso(
                        usuario,
                        permiso
                );

        if (!tienePermiso) {

            return new AuthorizationResult(
                    false,
                    "RBAC: el usuario no tiene el permiso requerido"
            );
        }

        // =========================
        // PASO 2 - ABAC
        // =========================

        if (documento == null) {

            return new AuthorizationResult(
                    true,
                    "RBAC permitido"
            );
        }

        AbacResult resultadoAbac =
                abacService.evaluar(
                        usuario,
                        documento,
                        permiso,
                        hora,
                        dispositivoCorporativo
                );

        if (!resultadoAbac.isPermitido()) {

            return new AuthorizationResult(
                    false,
                    "ABAC: "
                            + resultadoAbac.getMotivo()
            );
        }

        // =========================
        // AUTORIZADO
        // =========================

        return new AuthorizationResult(
                true,
                "RBAC y ABAC permitidos"
        );
    }

    public AuthorizationResult autorizarModificacion(
            Usuario usuario,
            Documento documentoActual,
            Documento documentoActualizado,
            LocalTime hora,
            boolean dispositivoCorporativo) {

        // Comprueba el departamento original antes de evaluar los nuevos
        // atributos para impedir que se reasigne un documento ajeno y luego
        // se modifique desde el departamento del Administrador.
        if (esAdministrador(usuario)
                && !mismoDepartamento(usuario, documentoActual)) {
            return new AuthorizationResult(
                    false,
                    "ABAC: El Administrador solo puede modificar documentos de su departamento"
            );
        }

        AuthorizationResult resultado = autorizar(
                usuario,
                "Modificar documento",
                documentoActualizado,
                hora,
                dispositivoCorporativo
        );

        if (!resultado.isPermitido()) {
            return resultado;
        }

        AbacResult clasificacion = abacService.validarCambioClasificacion(
                usuario,
                documentoActual,
                documentoActualizado
        );

        return new AuthorizationResult(
                clasificacion.isPermitido(),
                clasificacion.isPermitido()
                        ? "Modificación permitida"
                        : "ABAC: " + clasificacion.getMotivo()
        );
    }

    private boolean esAdministrador(Usuario usuario) {
        return usuario != null
                && usuario.getRol() != null
                && "Administrador".equalsIgnoreCase(
                        usuario.getRol().getNombre());
    }

    private boolean mismoDepartamento(Usuario usuario, Documento documento) {
        return usuario.getDepartamento() != null
                && documento != null
                && documento.getDepartamento() != null
                && usuario.getDepartamento().getId() != null
                && usuario.getDepartamento().getId().equals(
                        documento.getDepartamento().getId());
    }
}
