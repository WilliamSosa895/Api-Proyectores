package com.proyecto.api.controller;

import com.proyecto.api.model.Rol;
import com.proyecto.api.model.Usuario;
import com.proyecto.api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET /api/usuarios — lista todos los usuarios activos
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarActivos();
    }

    // GET /api/usuarios/todos — lista activos e inactivos (solo admin)
    @GetMapping("/todos")
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }

    // GET /api/usuarios/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtener(@PathVariable int id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/usuarios
    // body: { "nombre": "Juan Pérez", "idRol": 2 }
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Map<String, Object> body) {
        try {
            Usuario creado = usuarioService.crear(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/usuarios/{id}
    // body: { "nombre": "...", "idRol": 1, "estado": "inactivo" } — todos opcionales
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable int id,
            @RequestBody Map<String, Object> body) {
        try {
            Usuario actualizado = usuarioService.actualizar(id, body);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /api/usuarios/{id} — desactiva, no elimina físicamente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable int id) {
        usuarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/usuarios/roles — catálogo de roles disponibles
    @GetMapping("/roles")
    public List<Rol> listarRoles() {
        return usuarioService.listarRoles();
    }
}
