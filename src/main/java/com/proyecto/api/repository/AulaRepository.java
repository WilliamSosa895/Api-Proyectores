package com.proyecto.api.repository;

import com.proyecto.api.model.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad {@link Aula}.
 */
@Repository
public interface AulaRepository extends JpaRepository<Aula, Integer> {
}
