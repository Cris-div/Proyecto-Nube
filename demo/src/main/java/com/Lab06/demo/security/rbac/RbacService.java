package com.Lab06.demo.security.rbac;

import com.Lab06.demo.entity.RolPermiso;
import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.RolPermisoRepository;
import org.springframework.stereotype.Service;

@Service
public class RbacService {

    private final RolPermisoRepository rolPermisoRepository;

    public RbacService(RolPermisoRepository rolPermisoRepository) {
        this.rolPermisoRepository = rolPermisoRepository;
    }

    public boolean tienePermiso(Usuario usuario, String nombrePermiso) {

        if (usuario == null) {
            return false;
        }

        if (usuario.getRol() == null) {
            return false;
        }

        for (RolPermiso rolPermiso : rolPermisoRepository.findAll()) {

            if (rolPermiso.getRol().getId()
                    .equals(usuario.getRol().getId())) {

                if (rolPermiso.getPermiso().getNombre()
                        .equalsIgnoreCase(nombrePermiso)) {

                    return true;
                }
            }
        }

        return false;
    }
}