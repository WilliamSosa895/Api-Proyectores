package com.proyecto.api.repository;

import com.proyecto.api.model.AccionDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para el historico de acciones ejecutadas sobre dispositivos.
 */
@Repository
public interface AccionDispositivoRepository extends JpaRepository<AccionDispositivo, Integer> {
}
