package com.proyecto.api.service;

import com.proyecto.api.model.Rol;
import com.proyecto.api.model.Usuario;
import com.proyecto.api.repository.RolRepository;
import com.proyecto.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepo;
    private final RolRepository     rolRepo;

    public UsuarioService(UsuarioRepository usuarioRepo, RolRepository rolRepo) {
        this.usuarioRepo = usuarioRepo;
        this.rolRepo     = rolRepo;
    }

    // ------------------------------------------------------------------ listar

    public List<Usuario> listarTodos() {
        return usuarioRepo.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepo.findByEstado("activo");
    }

    public Optional<Usuario> buscarPorId(int idUsuario) {
        return usuarioRepo.findById(idUsuario);
    }

    // ------------------------------------------------------------------ crear
    // body esperado: { "nombre": "Juan", "idRol": 2 }

    public Usuario crear(Map<String, Object> body) {
        String nombre = (String) body.get("nombre");
        int    idRol  = (int)    body.get("idRol");

        Rol rol = rolRepo.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + idRol));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        usuario.setEstado("activo");

        Usuario guardado = usuarioRepo.save(usuario);
        log.info("[UsuarioService] Usuario creado: id={} nombre={} rol={}",
                guardado.getIdUsuario(), guardado.getNombre(), rol.getNombreRol());
        return guardado;
    }

    // ------------------------------------------------------------------ actualizar
    // Permite cambiar nombre, rol o estado

    public Usuario actualizar(int idUsuario, Map<String, Object> body) {
        Usuario usuario = usuarioRepo.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));

        if (body.containsKey("nombre")) {
            usuario.setNombre((String) body.get("nombre"));
        }
        if (body.containsKey("idRol")) {
            int idRol = (int) body.get("idRol");
            Rol rol = rolRepo.findById(idRol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + idRol));
            usuario.setRol(rol);
        }
        if (body.containsKey("estado")) {
            String estado = (String) body.get("estado");
            if (!estado.equals("activo") && !estado.equals("inactivo")) {
                throw new RuntimeException("Estado inválido: " + estado);
            }
            usuario.setEstado(estado);
        }

        Usuario actualizado = usuarioRepo.save(usuario);
        log.info("[UsuarioService] Usuario actualizado: id={}", idUsuario);
        return actualizado;
    }

    // ------------------------------------------------------------------ desactivar
    // No se elimina físicamente — se marca como inactivo

    public void desactivar(int idUsuario) {
        usuarioRepo.findById(idUsuario).ifPresent(u -> {
            u.setEstado("inactivo");
            usuarioRepo.save(u);
            log.info("[UsuarioService] Usuario desactivado: id={}", idUsuario);
        });
    }

    // ------------------------------------------------------------------ roles disponibles

    public List<Rol> listarRoles() {
        return rolRepo.findAll();
    }
}
