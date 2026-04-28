package com.proyecto.api.security;

import com.proyecto.api.model.Usuario;
import com.proyecto.api.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String nombre) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepo.findByNombre(nombre)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + nombre));

        if (!"activo".equals(usuario.getEstado())) {
            throw new UsernameNotFoundException("Usuario inactivo: " + nombre);
        }

        String authority = "ROLE_" + usuario.getRol().getNombreRol().replace(" ", "_");

        return new User(
                usuario.getNombre(),
                usuario.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
