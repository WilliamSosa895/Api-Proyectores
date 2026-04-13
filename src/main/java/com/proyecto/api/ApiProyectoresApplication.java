package com.proyecto.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada de la aplicacion Spring Boot.
 *
 * <p>Habilita la ejecucion asincrona para que el flujo de solicitudes
 * de proyeccion pueda ejecutarse en segundo plano sin bloquear el hilo HTTP.</p>
 */
@SpringBootApplication
@EnableAsync
public class ApiProyectoresApplication {

    /**
     * Arranca el contexto de Spring e inicializa todos los beans de la API.
     *
     * @param args argumentos de linea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiProyectoresApplication.class, args);
    }
}
