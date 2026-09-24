package com.Lab06.demo.repository;

import com.Lab06.demo.entity.Rol;
import com.Lab06.demo.entity.RolPermiso;
import com.Lab06.demo.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {

    Optional<RolPermiso> findByRolAndPermiso(
            Rol rol,
            Permiso permiso
    );

}
