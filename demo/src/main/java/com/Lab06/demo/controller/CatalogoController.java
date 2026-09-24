package com.Lab06.demo.controller;

import com.Lab06.demo.entity.Departamento;
import com.Lab06.demo.entity.Rol;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.DepartamentoRepository;
import com.Lab06.demo.repository.RolRepository;
import com.Lab06.demo.security.rbac.RbacService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final DepartamentoRepository departamentoRepository;
    private final RolRepository rolRepository;
    private final RbacService rbacService;

    public CatalogoController(DepartamentoRepository departamentoRepository,
                              RolRepository rolRepository,
                              RbacService rbacService) {
        this.departamentoRepository = departamentoRepository;
        this.rolRepository = rolRepository;
        this.rbacService = rbacService;
    }

    @GetMapping("/departamentos")
    public ResponseEntity<List<Departamento>> departamentos() {
        return ResponseEntity.ok(departamentoRepository.findAll());
    }

    @GetMapping("/roles")
    public ResponseEntity<?> roles(Authentication authentication) {
        Usuario usuario = authentication != null
                && authentication.getPrincipal() instanceof Usuario principal
                ? principal : null;
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        if (!rbacService.tienePermiso(usuario, "Asignar roles")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tiene permiso para asignar roles");
        }
        return ResponseEntity.ok(rolRepository.findAll());
    }
}
