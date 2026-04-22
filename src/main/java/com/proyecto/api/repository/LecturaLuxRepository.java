package com.proyecto.api.repository;

import com.proyecto.api.model.LecturaLux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para lecturas de iluminacion (lux) por aula.
 */
@Repository
public interface LecturaLuxRepository extends JpaRepository<LecturaLux, Integer> {

    /**
     * Obtiene la lectura mas reciente de lux para un aula.
     *
     * @param idAula identificador de aula
     * @return ultima lectura encontrada
     */
    Optional<LecturaLux> findTopByIdAulaOrderByTimestampDesc(int idAula);

    /**
     * Obtiene las ultimas 100 lecturas ordenadas desde la mas reciente.
     *
     * @param idAula identificador de aula
     * @return historial corto de lecturas de lux
     */
    List<LecturaLux> findTop100ByIdAulaOrderByTimestampDesc(int idAula);

    @Query(value = """
        SELECT * FROM (
            SELECT * FROM lecturas_lux
            WHERE id_aula = :idAula
            ORDER BY timestamp DESC
            LIMIT :limite
        ) sub
        ORDER BY timestamp ASC
        """, nativeQuery = true)
    List<LecturaLux> findTopNByIdAulaOrderByTimestampDesc(
            @Param("idAula") int idAula,
            @Param("limite") int limite
    );
}
