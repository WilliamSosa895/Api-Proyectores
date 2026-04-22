package com.proyecto.api.repository;

import com.proyecto.api.model.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoEventoRepository extends JpaRepository<TipoEvento, Integer> {

    // Usado por EventoSistemaService para resolver el tipo por nombre
    Optional<TipoEvento> findByDescripcion(String descripcion);
}
