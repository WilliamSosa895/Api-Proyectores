package com.proyecto.api.controller;

import com.proyecto.api.model.Dispositivo;
import com.proyecto.api.repository.DispositivoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consultar dispositivos por aula.
 */
@RestController
@RequestMapping("/api/aulas/{idAula}/dispositivos")
@CrossOrigin(origins = "*")
public class DispositivoController {

    private final DispositivoRepository dispositivoRepo;

    /**
     * Constructor con inyeccion del repositorio de dispositivos.
     *
     * @param dispositivoRepo repositorio para consultas de dispositivos
     */
    public DispositivoController(DispositivoRepository dispositivoRepo) {
        this.dispositivoRepo = dispositivoRepo;
    }

    /**
     * Lista los dispositivos del aula indicada en la URL.
     *
     * @param idAula identificador numerico del aula
     * @return dispositivos asociados al aula
     */
    @GetMapping
    public List<Dispositivo> listar(@PathVariable int idAula) {
        return dispositivoRepo.findByIdAula(idAula);
    }
}
