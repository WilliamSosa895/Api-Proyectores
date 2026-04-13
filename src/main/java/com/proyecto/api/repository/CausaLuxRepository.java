package com.proyecto.api.repository;

import com.proyecto.api.model.CausaLux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para catalogo de causas de iluminancia.
 */
@Repository
public interface CausaLuxRepository extends JpaRepository<CausaLux, Integer> {

    /**
     * Busca una causa por su nombre logico (INITIAL, LIGHTS_OFF, etc.).
     *
     * @param nombre nombre de la causa
     * @return causa encontrada
     */
    Optional<CausaLux> findByNombre(String nombre);
}