package com.proyecto.api.controller;

import com.proyecto.api.dto.LoginRequest;
import com.proyecto.api.dto.LoginResponse;
import com.proyecto.api.model.Usuario;
import com.proyecto.api.repository.UsuarioRepository;
import com.proyecto.api.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepo;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UsuarioRepository usuarioRepo) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepo = usuarioRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getNombre(),
                    request.getPassword()
                )
            );

            Usuario usuario = usuarioRepo.findByNombre(request.getNombre()).orElseThrow();

            String token = jwtUtil.generarToken(
                    usuario.getIdUsuario(),
                    usuario.getNombre(),
                    usuario.getRol().getNombreRol()
            );

            log.info("[Auth] Login exitoso: usuario={} rol={}",
                    usuario.getNombre(), usuario.getRol().getNombreRol());

            return ResponseEntity.ok(new LoginResponse(
                    token,
                    usuario.getIdUsuario(),
                    usuario.getNombre(),
                    usuario.getRol().getNombreRol(),
                    jwtUtil.getExpirationMs()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nombre o contrasena incorrectos"));
        } catch (Exception e) {
            log.error("[Auth] Error en login: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            int idUsuario = jwtUtil.extraerIdUsuario(token);
            String nombre = jwtUtil.extraerNombre(token);
            String rol = jwtUtil.extraerRol(token);

            return ResponseEntity.ok(Map.of(
                "idUsuario", idUsuario,
                "nombre", nombre,
                "rol", rol
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalido"));
        }
    }
}
