package com.Lab06.demo.controller;

import com.Lab06.demo.entity.Auditoria;
import com.Lab06.demo.entity.Documento;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.security.AuthorizationResult;
import com.Lab06.demo.security.AuthorizationService;
import com.Lab06.demo.service.AuditoriaService;
import com.Lab06.demo.service.DocumentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final AuthorizationService authorizationService;
    private final AuditoriaService auditoriaService;

    public DocumentoController(
            DocumentoService documentoService,
            AuthorizationService authorizationService,
            AuditoriaService auditoriaService) {

        this.documentoService = documentoService;
        this.authorizationService = authorizationService;
        this.auditoriaService = auditoriaService;
    }

    // =========================================================
    // LISTAR DOCUMENTOS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> listar(
            Authentication authentication,
            @RequestHeader(name = "X-Device-Type", defaultValue = "PERSONAL") String deviceType) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        /*
         * Primero comprobamos RBAC.
         *
         * Todos los roles tienen el permiso
         * "Consultar documento", según la matriz RBAC.
         */
        AuthorizationResult permisoRBAC =
                authorizationService.autorizar(
                        usuario,
                        "Consultar documento",
                        null,
                        LocalTime.now(),
                        isCorporateDevice(deviceType)
                );

        if (!permisoRBAC.isPermitido()) {

            registrarAuditoria(
                    usuario,
                    "DOCUMENTOS",
                    "CONSULTAR",
                    permisoRBAC
            );

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(permisoRBAC.getMotivo());
        }

        /*
         * Obtenemos todos los documentos.
         */
        List<Documento> todos =
                documentoService.listar();

        /*
         * Solo devolvemos los documentos que
         * también cumplen las políticas ABAC.
         */
        List<Documento> permitidos =
                todos.stream()
                        .filter(documento -> {

                            AuthorizationResult resultado =
                                    authorizationService.autorizar(
                                            usuario,
                                            "Consultar documento",
                                            documento,
                                            LocalTime.now(),
                                            isCorporateDevice(deviceType)
                                    );

                            registrarAuditoria(
                                    usuario,
                                    "DOCUMENTO "
                                            + documento.getId(),
                                    "CONSULTAR",
                                    resultado
                            );

                            return resultado.isPermitido();
                        })
                        .toList();

        return ResponseEntity.ok(permitidos);
    }

    // =========================================================
    // BUSCAR DOCUMENTO POR ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(name = "X-Device-Type", defaultValue = "PERSONAL") String deviceType) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        Documento documento =
                documentoService.buscarPorId(id)
                        .orElse(null);

        if (documento == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        AuthorizationResult resultado =
                authorizationService.autorizar(
                        usuario,
                        "Consultar documento",
                        documento,
                        LocalTime.now(),
                        isCorporateDevice(deviceType)
                );

        registrarAuditoria(
                usuario,
                "DOCUMENTO " + id,
                "CONSULTAR",
                resultado
        );

        if (!resultado.isPermitido()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(resultado.getMotivo());
        }

        return ResponseEntity.ok(documento);
    }

    // =========================================================
    // CREAR DOCUMENTO
    // =========================================================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Documento documento,
            Authentication authentication,
            @RequestHeader(name = "X-Device-Type", defaultValue = "PERSONAL") String deviceType) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        /*
         * El propietario del documento será
         * automáticamente el usuario autenticado.
         */
        documento.setPropietario(usuario);

        AuthorizationResult resultado =
                authorizationService.autorizar(
                        usuario,
                        "Crear documento",
                        documento,
                        LocalTime.now(),
                        isCorporateDevice(deviceType)
                );

        registrarAuditoria(
                usuario,
                "DOCUMENTOS",
                "CREAR",
                resultado
        );

        if (!resultado.isPermitido()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(resultado.getMotivo());
        }

        Documento guardado =
                documentoService.guardar(documento);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    // =========================================================
    // MODIFICAR DOCUMENTO
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Documento datos,
            Authentication authentication,
            @RequestHeader(name = "X-Device-Type", defaultValue = "PERSONAL") String deviceType) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        Documento documento =
                documentoService.buscarPorId(id)
                        .orElse(null);

        if (documento == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // Autorizar los atributos que se van a guardar, no solo el estado
        // anterior. Así no se puede subir el nivel por encima del clearance.
        Documento objetivo = new Documento();
        objetivo.setTitulo(datos.getTitulo() != null
                ? datos.getTitulo() : documento.getTitulo());
        objetivo.setDescripcion(datos.getDescripcion());
        objetivo.setPropietario(documento.getPropietario());
        objetivo.setDepartamento(datos.getDepartamento() != null
                ? datos.getDepartamento() : documento.getDepartamento());
        objetivo.setNivelConfidencialidad(
                datos.getNivelConfidencialidad() != null
                        ? datos.getNivelConfidencialidad()
                        : documento.getNivelConfidencialidad());
        // El estado solo cambia por el flujo de aprobación/publicación.
        objetivo.setEstado(documento.getEstado());
        objetivo.setPais(datos.getPais() != null
                ? datos.getPais() : documento.getPais());
        objetivo.setFechaCreacion(documento.getFechaCreacion());

        AuthorizationResult resultado =
                authorizationService.autorizarModificacion(
                        usuario,
                        documento,
                        objetivo,
                        LocalTime.now(),
                        isCorporateDevice(deviceType)
                );

        registrarAuditoria(
                usuario,
                "DOCUMENTO " + id,
                "MODIFICAR",
                resultado
        );

        if (!resultado.isPermitido()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(resultado.getMotivo());
        }

        Documento actualizado =
                documentoService.actualizar(
                        id,
                        objetivo
                );

        return ResponseEntity.ok(actualizado);
    }

    // =========================================================
    // ELIMINAR DOCUMENTO
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(name = "X-Device-Type", defaultValue = "PERSONAL") String deviceType) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        Documento documento =
                documentoService.buscarPorId(id)
                        .orElse(null);

        if (documento == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        AuthorizationResult resultado =
                authorizationService.autorizar(
                        usuario,
                        "Eliminar documento",
                        documento,
                        LocalTime.now(),
                        isCorporateDevice(deviceType)
                );

        registrarAuditoria(
                usuario,
                "DOCUMENTO " + id,
                "ELIMINAR",
                resultado
        );

        if (!resultado.isPermitido()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(resultado.getMotivo());
        }

        documentoService.eliminar(id);

        return ResponseEntity.ok(
                "Documento eliminado correctamente"
        );
    }

    // =========================================================
    // APROBAR DOCUMENTO
    // =========================================================

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(name = "X-Device-Type", defaultValue = "PERSONAL") String deviceType) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        Documento documento =
                documentoService.buscarPorId(id)
                        .orElse(null);

        if (documento == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        AuthorizationResult resultado =
                authorizationService.autorizar(
                        usuario,
                        "Aprobar documento",
                        documento,
                        LocalTime.now(),
                        isCorporateDevice(deviceType)
                );

        registrarAuditoria(
                usuario,
                "DOCUMENTO " + id,
                "APROBAR",
                resultado
        );

        if (!resultado.isPermitido()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(resultado.getMotivo());
        }

        Documento aprobado =
                documentoService.aprobar(id);

        return ResponseEntity.ok(aprobado);
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<?> publicar(
            @PathVariable Long id,
            Authentication authentication) {
        return cambiarEstadoAdministrativo(
                id, authentication, "APROBADO", "PUBLICADO", "PUBLICAR", true);
    }

    @PostMapping("/{id}/retirar-publicacion")
    public ResponseEntity<?> retirarPublicacion(
            @PathVariable Long id,
            Authentication authentication) {
        return cambiarEstadoAdministrativo(
                id, authentication, "PUBLICADO", "APROBADO", "RETIRAR_PUBLICACION", false);
    }

    @PostMapping("/{id}/reabrir")
    public ResponseEntity<?> reabrir(
            @PathVariable Long id,
            Authentication authentication) {
        return cambiarEstadoAdministrativo(
                id, authentication, "APROBADO", "PENDIENTE", "REABRIR", false);
    }

    private ResponseEntity<?> cambiarEstadoAdministrativo(
            Long id,
            Authentication authentication,
            String estadoRequerido,
            String estadoNuevo,
            String accion,
            boolean requiereNivelPublico) {

        Usuario usuario = obtenerUsuario(authentication);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        Documento documento = documentoService.buscarPorId(id).orElse(null);
        if (documento == null) {
            return ResponseEntity.notFound().build();
        }

        AuthorizationResult resultado = authorizationService.autorizar(
                usuario,
                "Gestionar estado documento",
                null,
                LocalTime.now(),
                false
        );

        if (!resultado.isPermitido()) {
            registrarAuditoria(usuario, "DOCUMENTO " + id, accion, resultado);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(resultado.getMotivo());
        }

        if (!estadoRequerido.equalsIgnoreCase(documento.getEstado())) {
            AuthorizationResult transicionInvalida = new AuthorizationResult(
                    false,
                    "Transición inválida: el documento debe estar en estado " + estadoRequerido
            );
            registrarAuditoria(usuario, "DOCUMENTO " + id, accion, transicionInvalida);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(transicionInvalida.getMotivo());
        }

        if (requiereNivelPublico &&
                !Integer.valueOf(1).equals(documento.getNivelConfidencialidad())) {
            AuthorizationResult nivelInvalido = new AuthorizationResult(
                    false,
                    "Solo se pueden publicar documentos de nivel 1"
            );
            registrarAuditoria(usuario, "DOCUMENTO " + id, accion, nivelInvalido);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(nivelInvalido.getMotivo());
        }

        registrarAuditoria(usuario, "DOCUMENTO " + id, accion, resultado);
        return ResponseEntity.ok(documentoService.cambiarEstado(id, estadoNuevo));
    }

    // =========================================================
    // REGISTRAR AUDITORÍA
    // =========================================================

    private void registrarAuditoria(
            Usuario usuario,
            String recurso,
            String accion,
            AuthorizationResult resultado) {

        Auditoria auditoria =
                new Auditoria(
                        usuario,
                        recurso,
                        accion,
                        LocalDateTime.now(),
                        resultado.isPermitido()
                                ? "PERMITIDO"
                                : "DENEGADO",
                        resultado.getMotivo()
                );

        auditoriaService.registrar(auditoria);
    }

    private boolean isCorporateDevice(String deviceType) {
        return "CORPORATIVO".equalsIgnoreCase(deviceType);
    }

    // =========================================================
    // OBTENER USUARIO AUTENTICADO
    // =========================================================

    private Usuario obtenerUsuario(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return null;
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }

        return null;
    }
}
