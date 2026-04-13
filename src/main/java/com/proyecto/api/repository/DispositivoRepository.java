package com.proyecto.api.repository;

import com.proyecto.api.model.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para acceso a dispositivos y consultas por aula/tipo.
 */
@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Integer> {

    /**
     * Recupera todos los dispositivos asociados a un aula.
     *
     * @param idAula identificador de aula
     * @return lista de dispositivos del aula
     */
    List<Dispositivo> findByIdAula(int idAula);

    /**
     * Busca un dispositivo por aula y por nombre de tipo logico.
     *
     * @param idAula identificador de aula
     * @param tipo nombre del tipo de dispositivo
     * @return dispositivo encontrado si existe
     */
    @Query("SELECT d FROM Dispositivo d JOIN d.tipo t " +
           "WHERE d.idAula = :idAula AND t.nombreTipo = :tipo")
    Optional<Dispositivo> findByIdAulaAndTipoNombre(@Param("idAula") int idAula,
                                                     @Param("tipo") String tipo);
}
