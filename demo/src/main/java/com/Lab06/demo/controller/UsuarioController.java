package com.Lab06.demo.controller;

import com.Lab06.demo.entity.Rol;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.RolRepository;
import com.Lab06.demo.security.rbac.RbacService;
import com.Lab06.demo.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RbacService rbacService;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(
            UsuarioService usuarioService,
            RbacService rbacService,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioService = usuarioService;
        this.rbacService = rbacService;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // LISTAR USUARIOS
    // =========================

    @GetMapping
    public ResponseEntity<?> listar(
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!tienePermiso(usuario, "Gestionar usuarios")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para gestionar usuarios");
        }

        return ResponseEntity.ok(usuarioService.listar());
    }

    // =========================
    // BUSCAR USUARIO
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!tienePermiso(usuario, "Gestionar usuarios")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para gestionar usuarios");
        }

        return usuarioService.buscarPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    // =========================
    // CREAR USUARIO
    // =========================

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestBody Usuario nuevoUsuario,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!tienePermiso(usuario, "Gestionar usuarios")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para gestionar usuarios");
        }

        if (nuevoUsuario.getPassword() == null || nuevoUsuario.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("La contraseña es obligatoria");
        }
        nuevoUsuario.setPassword(passwordEncoder.encode(nuevoUsuario.getPassword()));
        Usuario guardado = usuarioService.guardar(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(guardado);
    }

    // =========================
    // ACTUALIZAR USUARIO
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @RequestBody Usuario datos,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!tienePermiso(usuario, "Gestionar usuarios")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para gestionar usuarios");
        }

        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            datos.setPassword(passwordEncoder.encode(datos.getPassword()));
        }

        Usuario existente = usuarioService.buscarPorId(id).orElse(null);
        if (existente != null && datos.getRol() != null
                && !datos.getRol().getId().equals(existente.getRol().getId())
                && !rbacService.tienePermiso(usuario, "Asignar roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para asignar roles");
        }

        try {
            Usuario actualizado =
                    usuarioService.actualizar(id, datos);

            return ResponseEntity.ok(actualizado);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================
    // ELIMINAR USUARIO
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        if (!tienePermiso(usuario, "Gestionar usuarios")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para gestionar usuarios");
        }

        usuarioService.eliminar(id);

        return ResponseEntity.ok(
                "Usuario eliminado correctamente"
        );
    }

    // =========================
    // ASIGNAR ROL
    // =========================

    @PutMapping("/{id}/rol/{rolId}")
    public ResponseEntity<?> asignarRol(
            @PathVariable Long id,
            @PathVariable Long rolId,
            Authentication authentication) {

        Usuario usuarioActual =
                obtenerUsuario(authentication);

        if (usuarioActual == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        // Solo un usuario con el permiso "Asignar roles"
        // puede cambiar el rol de otro usuario.
        if (!rbacService.tienePermiso(
                usuarioActual,
                "Asignar roles")) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para asignar roles");
        }

        // Buscar usuario que recibirá el nuevo rol
        Usuario usuario =
                usuarioService.buscarPorId(id)
                        .orElse(null);

        if (usuario == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        // Buscar el rol
        Rol rol =
                rolRepository.findById(rolId)
                        .orElse(null);

        if (rol == null) {
            return ResponseEntity
                    .badRequest()
                    .body("El rol no existe");
        }

        // Asignar el rol
        usuario.setRol(rol);

        // Guardar cambios
        Usuario actualizado =
                usuarioService.guardar(usuario);

        return ResponseEntity.ok(actualizado);
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private boolean tienePermiso(
            Usuario usuario,
            String permiso) {

        return rbacService.tienePermiso(
                usuario,
                permiso
        );
    }

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
