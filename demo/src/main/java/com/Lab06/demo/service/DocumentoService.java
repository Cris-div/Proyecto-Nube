package com.Lab06.demo.service;

import com.Lab06.demo.entity.Documento;

import java.util.List;
import java.util.Optional;

public interface DocumentoService {

    List<Documento> listar();

    Optional<Documento> buscarPorId(Long id);

    Documento guardar(Documento documento);

    Documento actualizar(Long id, Documento documento);

    void eliminar(Long id);

    Documento aprobar(Long id);

    Documento cambiarEstado(Long id, String estado);
}
