package com.Lab06.demo.controller;

import com.Lab06.demo.entity.Politica;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.service.PoliticaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/politicas")
@CrossOrigin(origins = "http://localhost:4200")
public class PoliticaController {

    private final PoliticaService politicaService;

    public PoliticaController(
            PoliticaService politicaService) {

        this.politicaService = politicaService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            Authentication authentication) {

        Usuario usuario =
                obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!esAdministrador(usuario)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Solo el Administrador puede gestionar políticas");
        }

        return ResponseEntity.ok(
                politicaService.listar()
        );
    }

    @GetMapping("/activas")
    public ResponseEntity<?> listarActivas(
            Authentication authentication) {

        Usuario usuario =
                obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!esAdministrador(usuario)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Solo el Administrador puede gestionar políticas");
        }

        return ResponseEntity.ok(
                politicaService.listarActivas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario =
                obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!esAdministrador(usuario)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Solo el Administrador puede gestionar políticas");
        }

        return politicaService
                .buscarPorId(id)
                .<ResponseEntity<?>>map(
                        ResponseEntity::ok
                )
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Politica politica,
            Authentication authentication) {

        Usuario usuario =
                obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!esAdministrador(usuario)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Solo el Administrador puede gestionar políticas");
        }

        Politica guardada =
                politicaService.guardar(politica);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Politica datos,
            Authentication authentication) {

        Usuario usuario =
                obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!esAdministrador(usuario)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Solo el Administrador puede gestionar políticas");
        }

        try {
            return ResponseEntity.ok(
                    politicaService.actualizar(id, datos)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario =
                obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!esAdministrador(usuario)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Solo el Administrador puede gestionar políticas");
        }

        politicaService.eliminar(id);

        return ResponseEntity.ok(
                "Política eliminada correctamente"
        );
    }

    private boolean esAdministrador(
            Usuario usuario) {

        return usuario.getRol() != null
                && "Administrador".equalsIgnoreCase(
                usuario.getRol().getNombre()
        );
    }

    private Usuario obtenerUsuario(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

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