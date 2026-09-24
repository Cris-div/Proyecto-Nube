package com.Lab06.demo.service;

import com.Lab06.demo.entity.Politica;

import java.util.List;
import java.util.Optional;

public interface PoliticaService {

    List<Politica> listar();

    List<Politica> listarActivas();

    Optional<Politica> buscarPorId(Long id);

    Politica guardar(Politica politica);

    Politica actualizar(Long id, Politica politica);

    void eliminar(Long id);
}