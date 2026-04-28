package com.proyecto.api.service;

import com.proyecto.api.model.Rol;
import com.proyecto.api.model.Usuario;
import com.proyecto.api.repository.RolRepository;
import com.proyecto.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepo;
    private final RolRepository     rolRepo;
    private final PasswordEncoder   passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepo,
                          RolRepository rolRepo,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepo     = usuarioRepo;
        this.rolRepo         = rolRepo;
        this.passwordEncoder = passwordEncoder;
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
    // body esperado: { "nombre": "Juan", "password": "Clave123!", "idRol": 2 }

    public Usuario crear(Map<String, Object> body) {
        String nombre   = (String) body.get("nombre");
        String password = (String) body.get("password");
        int    idRol    = (int) body.get("idRol");

        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (password == null || password.length() < 6) {
            throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
        }

        if (usuarioRepo.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("Ya existe un usuario con ese nombre");
        }

        Rol rol = rolRepo.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + idRol));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        usuario.setEstado("activo");
        usuario.setPasswordHash(passwordEncoder.encode(password));

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
            String nuevoNombre = (String) body.get("nombre");
            usuarioRepo.findByNombre(nuevoNombre).ifPresent(u -> {
                if (u.getIdUsuario() != idUsuario) {
                    throw new RuntimeException("El nombre ya esta en uso");
                }
            });
            usuario.setNombre(nuevoNombre);
        }
        if (body.containsKey("password")) {
            String nuevaPassword = (String) body.get("password");
            if (nuevaPassword == null || nuevaPassword.length() < 6) {
                throw new RuntimeException("La contrasena debe tener al menos 6 caracteres");
            }
            usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
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
