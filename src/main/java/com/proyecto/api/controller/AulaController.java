package com.proyecto.api.controller;

import com.proyecto.api.model.Aula;
import com.proyecto.api.repository.AulaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consultas de aulas.
 */
@RestController
@RequestMapping("/api/aulas")
@CrossOrigin(origins = "*")
public class AulaController {

    private final AulaRepository aulaRepo;

    /**
     * Constructor con inyeccion del repositorio de aulas.
     *
     * @param aulaRepo repositorio para consultas de aulas
     */
    public AulaController(AulaRepository aulaRepo) {
        this.aulaRepo = aulaRepo;
    }

    /**
     * Obtiene todas las aulas registradas en el sistema.
     *
     * @return lista completa de aulas
     */
    @GetMapping
    public List<Aula> listar() {
        return aulaRepo.findAll();
    }

    /**
     * Obtiene una sola aula por identificador.
     *
     * @param id identificador numerico del aula
     * @return aula encontrada
     */
    @GetMapping("/{id}")
    public Aula obtener(@PathVariable int id) {
        return aulaRepo.findById(id).orElseThrow();
    }
}
