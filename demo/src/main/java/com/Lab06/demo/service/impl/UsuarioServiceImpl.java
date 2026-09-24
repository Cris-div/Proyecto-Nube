package com.Lab06.demo.service.impl;

import com.Lab06.demo.entity.Usuario;
import com.Lab06.demo.repository.UsuarioRepository;
import com.Lab06.demo.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        existente.setNombre(usuario.getNombre());
        existente.setCorreo(usuario.getCorreo());
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()) {
            existente.setPassword(usuario.getPassword());
        }
        existente.setRol(usuario.getRol());
        existente.setDepartamento(usuario.getDepartamento());
        existente.setNivelSeguridad(usuario.getNivelSeguridad());
        existente.setPais(usuario.getPais());
        existente.setTipoContrato(usuario.getTipoContrato());
        existente.setEstado(usuario.getEstado());

        return usuarioRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {

        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        usuarioRepository.deleteById(id);
    }
}
