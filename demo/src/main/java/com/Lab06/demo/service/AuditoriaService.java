package com.Lab06.demo.service;

import com.Lab06.demo.entity.Auditoria;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditoriaService {

    Auditoria registrar(
            Auditoria auditoria
    );

    List<Auditoria> listar();

    Page<Auditoria> listar(Pageable pageable, String usuario, String accion,
                           String resultado, java.time.LocalDate desde, java.time.LocalDate hasta);
}
