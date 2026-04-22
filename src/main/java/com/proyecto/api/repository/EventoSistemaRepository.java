package com.proyecto.api.repository;

import com.proyecto.api.model.EventoSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoSistemaRepository extends JpaRepository<EventoSistema, Integer> {

    // Últimos N eventos de un aula — para el panel de bitácora del admin
    List<EventoSistema> findTop100ByIdAulaOrderByTimestampDesc(int idAula);

    // Todos los eventos de un tipo específico
    List<EventoSistema> findByTipoEventoDescripcionOrderByTimestampDesc(String descripcion);
}
