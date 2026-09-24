package com.Lab06.demo.service.impl;

import com.Lab06.demo.entity.Documento;
import com.Lab06.demo.repository.DocumentoRepository;
import com.Lab06.demo.service.DocumentoService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;

    public DocumentoServiceImpl(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    @Override
    public List<Documento> listar() {
        return documentoRepository.findAll();
    }

    @Override
    public Optional<Documento> buscarPorId(Long id) {
        return documentoRepository.findById(id);
    }

    @Override
    public Documento guardar(Documento documento) {

        if (documento.getFechaCreacion() == null) {
            documento.setFechaCreacion(LocalDateTime.now());
        }

        if (documento.getEstado() == null) {
            documento.setEstado("PENDIENTE");
        }

        return documentoRepository.save(documento);
    }

    @Override
    public Documento actualizar(Long id, Documento documento) {

        Documento existente = documentoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Documento no encontrado"));

        existente.setTitulo(documento.getTitulo());
        existente.setDescripcion(documento.getDescripcion());

        existente.setDepartamento(documento.getDepartamento());
        existente.setNivelConfidencialidad(
                documento.getNivelConfidencialidad()
        );
        existente.setPais(documento.getPais());

        return documentoRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {

        if (!documentoRepository.existsById(id)) {
            throw new RuntimeException("Documento no encontrado");
        }

        documentoRepository.deleteById(id);
    }

    @Override
    public Documento aprobar(Long id) {

        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Documento no encontrado"));

        documento.setEstado(
                Integer.valueOf(1).equals(
                        documento.getNivelConfidencialidad())
                        ? "PUBLICADO"
                        : "APROBADO"
        );

        return documentoRepository.save(documento);
    }

    @Override
    public Documento cambiarEstado(Long id, String estado) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        documento.setEstado(estado);
        return documentoRepository.save(documento);
    }
}
