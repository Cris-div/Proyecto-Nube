package com.Lab06.demo.repository;

import com.Lab06.demo.entity.Politica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PoliticaRepository extends JpaRepository<Politica, Long> {

    Optional<Politica> findByNombre(String nombre);

    List<Politica> findByActivaTrue();
}