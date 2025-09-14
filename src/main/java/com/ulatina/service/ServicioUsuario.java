package com.ulatina.service;

import com.ulatina.data.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de usuarios
 */
@ApplicationScoped
public class ServicioUsuario {

    // Simulación de base de datos en memoria
    private Map<String, Usuario> usuarios = new HashMap<>();

    public ServicioUsuario() {
        // Inicializar usuarios de prueba
        Usuario admin = new Usuario();
        admin.setId(1);
        admin.setUsername("admin");
        admin.setPassword("admin123"); // En producción usar hash
        admin.setRol("ADMIN");
        admin.setEmail("admin@banco.com");
        admin.setTelefono("8888-8888");
        usuarios.put("admin", admin);

        Usuario cliente = new Usuario();
        cliente.setId(2);
        cliente.setUsername("cliente1");
        cliente.setPassword("cliente123");
        cliente.setRol("CLIENTE");
        cliente.setEmail("cliente@email.com");
        cliente.setTelefono("8777-7777");
        usuarios.put("cliente1", cliente);
    }

    /**
     * Obtiene un usuario por nombre de usuario
     */
    public Usuario obtenerPorUsername(String username) {
        return usuarios.get(username);
    }

    /**
     * Obtiene un usuario por ID
     */
    public Usuario obtenerPorId(int id) {
        return usuarios.values().stream()
            .filter(u -> u.getId() == id)
            .findFirst()
            .orElse(null);
    }

    /**
     * Crear nuevo usuario
     */
    public void crear(Usuario usuario) {
        usuarios.put(usuario.getUsername(), usuario);
    }

    /**
     * Actualizar usuario existente
     */
    public void actualizar(Usuario usuario) {
        usuarios.put(usuario.getUsername(), usuario);
    }

    /**
     * Listar todos los usuarios
     */
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }
}
