package com.proyecto.api.repository;

import com.proyecto.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findByEstado(String estado);

    List<Usuario> findByRolIdRol(int idRol);
}
