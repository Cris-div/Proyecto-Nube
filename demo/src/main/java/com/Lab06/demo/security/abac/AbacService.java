package com.Lab06.demo.security.abac;

import com.Lab06.demo.entity.Documento;
import com.Lab06.demo.entity.Politica;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.PoliticaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class AbacService {

    private final PoliticaRepository politicaRepository;

    public AbacService(PoliticaRepository politicaRepository) {
        this.politicaRepository = politicaRepository;
    }

    public AbacResult evaluar(
            Usuario usuario,
            Documento documento,
            String permiso,
            LocalTime hora,
            boolean dispositivoCorporativo) {

        // =========================
        // VALIDACIONES BÁSICAS
        // =========================

        if (usuario == null) {
            return new AbacResult(
                    false,
                    "Usuario no identificado"
            );
        }

        if (documento == null) {
            return new AbacResult(
                    false,
                    "Documento no identificado"
            );
        }

        // =========================
        // OBTENER POLÍTICAS ACTIVAS
        // =========================

        List<Politica> politicas =
                politicaRepository.findByActivaTrue();

        // =========================
        // EVALUAR POLÍTICAS
        // =========================

        for (Politica politica : politicas) {

            boolean cumple = evaluarPolitica(
                    politica.getCodigoRegla(),
                    usuario,
                    documento,
                    permiso,
                    hora,
                    dispositivoCorporativo
            );

            // Si la política es DENY y no se cumple,
            // se deniega el acceso.
            if (!cumple &&
                    "DENY".equalsIgnoreCase(
                            politica.getEfecto())) {

                return new AbacResult(
                        false,
                        "Política ABAC: "
                                + politica.getNombre()
                );
            }
        }

        // =========================
        // TODAS LAS POLÍTICAS OK
        // =========================

        return new AbacResult(
                true,
                "Todas las políticas ABAC fueron satisfechas"
        );
    }

    private boolean evaluarPolitica(
            String codigoRegla,
            Usuario usuario,
            Documento documento,
            String permiso,
            LocalTime hora,
            boolean dispositivoCorporativo) {

        switch (codigoRegla) {

            // =========================
            // POLÍTICA 1
            // USUARIO ACTIVO
            // =========================

            case "USUARIO_ACTIVO":

                return "ACTIVO".equalsIgnoreCase(
                        usuario.getEstado()
                );

            // =========================
            // POLÍTICA 2
            // DEPARTAMENTO
            // =========================

            case "DEPARTAMENTO_IGUAL":

                // Los documentos que pasan la política de Invitado son
                // públicos; su acceso no depende del departamento interno.
                if (esInvitado(usuario)) {
                    return true;
                }

                if (usuario.getDepartamento() == null ||
                        documento.getDepartamento() == null) {

                    return false;
                }

                return usuario
                        .getDepartamento()
                        .getId()
                        .equals(
                                documento
                                        .getDepartamento()
                                        .getId()
                        );

            // =========================
            // POLÍTICA 3
            // NIVEL DE SEGURIDAD
            // =========================

            case "NIVEL_SEGURIDAD":

                if (usuario.getNivelSeguridad() == null ||
                        documento.getNivelConfidencialidad() == null) {

                    return false;
                }

                return usuario.getNivelSeguridad()
                        >= documento.getNivelConfidencialidad();

            // =========================
            // POLÍTICA 4
            // PROPIETARIO PARA MODIFICAR
            // =========================

            case "PROPIETARIO_MODIFICAR":

                // Esta política solamente aplica
                // cuando la operación es modificar.
                if (!"Modificar documento"
                        .equalsIgnoreCase(permiso)) {

                    return true;
                }

                String rol = usuario.getRol() != null
                        ? usuario.getRol().getNombre()
                        : "";

                // Administrador y Gerente
                // tienen excepción de propietario.
                if ("Administrador"
                        .equalsIgnoreCase(rol)
                        ||
                        "Gerente"
                                .equalsIgnoreCase(rol)) {

                    return true;
                }

                if (documento.getPropietario() == null ||
                        usuario.getId() == null) {

                    return false;
                }

                return documento
                        .getPropietario()
                        .getId()
                        .equals(usuario.getId());

            // =========================
            // POLÍTICA 5
            // HORARIO CONFIDENCIAL
            // =========================

            case "HORARIO_CONFIDENCIAL":

                // Si el documento tiene nivel
                // menor que 4, no se restringe.
                if (documento
                        .getNivelConfidencialidad() == null ||
                        documento
                                .getNivelConfidencialidad() < 4) {

                    return true;
                }

                if (hora == null) {
                    return false;
                }

                LocalTime inicio =
                        LocalTime.of(8, 0);

                LocalTime fin =
                        LocalTime.of(18, 0);

                return !hora.isBefore(inicio)
                        &&
                        !hora.isAfter(fin);

            // =========================
            // POLÍTICA 6
            // MISMO PAÍS
            // =========================

            case "PAIS_IGUAL":

                // Los documentos públicos pueden consultarse desde fuera
                // de la organización o del país del propietario.
                if (esInvitado(usuario)) {
                    return true;
                }

                if (usuario.getPais() == null ||
                        documento.getPais() == null) {

                    return false;
                }

                return usuario
                        .getPais()
                        .equalsIgnoreCase(
                                documento.getPais()
                        );

            // =========================
            // POLÍTICA 7
            // DISPOSITIVO CORPORATIVO
            // =========================

            case "DISPOSITIVO_CORPORATIVO":

                // Solo aplica a documentos
                // de confidencialidad 4 o superior.
                if (documento
                        .getNivelConfidencialidad() == null ||
                        documento
                                .getNivelConfidencialidad() < 4) {

                    return true;
                }

                return dispositivoCorporativo;

            // =========================
            // POLÍTICA 8
            // INVITADO
            // =========================

            case "INVITADO_RESTRINGIDO":

                // Si no es invitado,
                // esta política no aplica.
                if (!esInvitado(usuario)) {

                    return true;
                }

                return "EXTERNO".equalsIgnoreCase(
                        usuario.getTipoContrato()
                )
                        &&
                        documento
                                .getNivelConfidencialidad() != null
                        &&
                        documento
                                .getNivelConfidencialidad() <= 1
                        &&
                        "PUBLICADO".equalsIgnoreCase(
                                documento.getEstado()
                        );

            // =========================
            // REGLA DESCONOCIDA
            // =========================

            default:

                return true;
        }
    }

    public AbacResult validarCambioClasificacion(
            Usuario usuario,
            Documento documentoActual,
            Documento documentoActualizado) {

        if (usuario == null || documentoActual == null ||
                documentoActualizado == null ||
                usuario.getNivelSeguridad() == null ||
                documentoActual.getNivelConfidencialidad() == null ||
                documentoActualizado.getNivelConfidencialidad() == null) {
            return new AbacResult(false,
                    "No se pudieron validar los niveles de confidencialidad");
        }

        int nivelActual = documentoActual.getNivelConfidencialidad();
        int nivelNuevo = documentoActualizado.getNivelConfidencialidad();

        if (nivelNuevo < 1 || nivelNuevo > 5) {
            return new AbacResult(false,
                    "La confidencialidad debe estar entre los niveles 1 y 5");
        }

        if (nivelNuevo > usuario.getNivelSeguridad()) {
            return new AbacResult(false,
                    "No puedes asignar una confidencialidad superior a tu nivel de seguridad");
        }

        if (nivelNuevo < nivelActual &&
                (usuario.getRol() == null ||
                        !"Administrador".equalsIgnoreCase(
                                usuario.getRol().getNombre()))) {
            return new AbacResult(false,
                    "Solo un Administrador puede reducir la confidencialidad de un documento");
        }

        return new AbacResult(true,
                "Cambio de confidencialidad permitido");
    }

    private boolean esInvitado(Usuario usuario) {
        return usuario != null && usuario.getRol() != null &&
                "Invitado".equalsIgnoreCase(usuario.getRol().getNombre());
    }
}
