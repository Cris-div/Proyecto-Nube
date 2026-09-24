package com.Lab06.demo.controller;

import com.Lab06.demo.entity.Auditoria;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.security.rbac.RbacService;
import com.Lab06.demo.service.AuditoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final RbacService rbacService;

    public AuditoriaController(
            AuditoriaService auditoriaService,
            RbacService rbacService) {

        this.auditoriaService = auditoriaService;
        this.rbacService = rbacService;
    }

    @GetMapping
    public ResponseEntity<?> listar(Authentication authentication,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(name = "usuario", required = false) String filtroUsuario,
                                    @RequestParam(required = false) String accion,
                                    @RequestParam(required = false) String resultado,
                                    @RequestParam(required = false) LocalDate desde,
                                    @RequestParam(required = false) LocalDate hasta) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no autenticado");
        }

        boolean permitido =
                rbacService.tienePermiso(
                        usuario,
                        "Ver auditoría"
                );

        if (!permitido) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tiene permiso para ver la auditoría");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        var auditorias = auditoriaService.listar(
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "fecha")),
                filtroUsuario, accion, resultado, desde, hasta);

        return ResponseEntity.ok(auditorias);
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
