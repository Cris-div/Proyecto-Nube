package com.Lab06.demo.security.rbac;

import com.Lab06.demo.entity.Permiso;
import com.Lab06.demo.entity.Rol;
import com.Lab06.demo.entity.RolPermiso;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.RolPermisoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RbacServiceTest {

    @Test
    void auditPermissionIsGrantedToAdministratorAndAuditorButNotManager() {
        Rol administrator = role(1L, "Administrador");
        Rol manager = role(2L, "Gerente");
        Rol auditor = role(3L, "Auditor");
        Permiso viewAudit = new Permiso("Ver auditoría");

        RolPermisoRepository repository = mock(RolPermisoRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new RolPermiso(administrator, viewAudit),
                new RolPermiso(auditor, viewAudit)
        ));
        RbacService service = new RbacService(repository);

        assertTrue(service.tienePermiso(user(administrator), "Ver auditoría"));
        assertTrue(service.tienePermiso(user(auditor), "Ver auditoría"));
        assertFalse(service.tienePermiso(user(manager), "Ver auditoría"));
    }

    private Rol role(Long id, String name) {
        Rol role = new Rol(name);
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    private Usuario user(Rol role) {
        Usuario user = new Usuario();
        user.setRol(role);
        return user;
    }
}
