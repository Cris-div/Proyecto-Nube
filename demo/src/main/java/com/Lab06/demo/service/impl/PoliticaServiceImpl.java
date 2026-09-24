package com.Lab06.demo.service.impl;

import com.Lab06.demo.entity.Politica;
import com.Lab06.demo.repository.PoliticaRepository;
import com.Lab06.demo.service.PoliticaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoliticaServiceImpl implements PoliticaService {

    private final PoliticaRepository politicaRepository;

    public PoliticaServiceImpl(PoliticaRepository politicaRepository) {
        this.politicaRepository = politicaRepository;
    }

    @Override
    public List<Politica> listar() {
        return politicaRepository.findAll();
    }

    @Override
    public List<Politica> listarActivas() {
        return politicaRepository.findByActivaTrue();
    }

    @Override
    public Optional<Politica> buscarPorId(Long id) {
        return politicaRepository.findById(id);
    }

    @Override
    public Politica guardar(Politica politica) {
        return politicaRepository.save(politica);
    }

    @Override
    public Politica actualizar(Long id, Politica politica) {

        Politica existente = politicaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Política no encontrada"));

        existente.setNombre(politica.getNombre());
        existente.setDescripcion(politica.getDescripcion());
        existente.setEfecto(politica.getEfecto());
        existente.setActiva(politica.getActiva());

        return politicaRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {

        if (!politicaRepository.existsById(id)) {
            throw new RuntimeException("Política no encontrada");
        }

        politicaRepository.deleteById(id);
    }
}