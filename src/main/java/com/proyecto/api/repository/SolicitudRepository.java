package com.proyecto.api.repository;

import com.proyecto.api.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA de solicitudes de proyeccion.
 */
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    /**
     * Recupera solicitudes de un aula ordenadas de mas reciente a mas antigua.
     *
     * @param idAula identificador de aula
     * @return historial de solicitudes del aula
     */
    List<Solicitud> findByIdAulaOrderByFechaSolicitudDesc(int idAula);
}
