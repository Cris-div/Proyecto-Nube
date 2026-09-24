package com.Lab06.demo.service.impl;

import com.Lab06.demo.entity.Auditoria;
import com.Lab06.demo.repository.AuditoriaRepository;
import com.Lab06.demo.service.AuditoriaService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Auditoria registrar(Auditoria auditoria) {

        if (auditoria.getFecha() == null) {
            auditoria.setFecha(LocalDateTime.now());
        }

        return auditoriaRepository.save(auditoria);
    }

    @Override
    public List<Auditoria> listar() {
        return auditoriaRepository.findAll();
    }

    @Override
    public Page<Auditoria> listar(Pageable pageable, String usuario, String accion,
                                  String resultado, java.time.LocalDate desde, java.time.LocalDate hasta) {
        Specification<Auditoria> spec = (root, query, cb) -> cb.conjunction();
        if (usuario != null && !usuario.isBlank()) {
            String pattern = "%" + usuario.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                var user = root.join("usuario", jakarta.persistence.criteria.JoinType.LEFT);
                return cb.or(cb.like(cb.lower(user.get("nombre")), pattern),
                        cb.like(cb.lower(user.get("correo")), pattern));
            });
        }
        if (accion != null && !accion.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("accion")), accion.trim().toLowerCase()));
        }
        if (resultado != null && !resultado.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("resultado")), resultado.trim().toLowerCase()));
        }
        if (desde != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), desde.atStartOfDay()));
        }
        if (hasta != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("fecha"), hasta.plusDays(1).atStartOfDay()));
        }
        return auditoriaRepository.findAll(spec, pageable);
    }
}
