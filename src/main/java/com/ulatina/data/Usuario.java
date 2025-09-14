package com.ulatina.data;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true)
    private String username;

    private String password;
    private String rol;
    private String tipo; // Agregado para compatibilidad con LoginController
    private String email;
    private String telefono;
    private int idCliente;
    private boolean activo;

    public Usuario() {}

    public Usuario(int id, String username, String password, String rol, int idCliente, boolean activo) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.tipo = rol; // Inicializar tipo igual a rol por defecto
        this.idCliente = idCliente;
        this.activo = activo;
    }

    // Getters y setters existentes
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) {
        this.rol = rol;
        this.tipo = rol; // Mantener sincronizados
    }

    // Métodos agregados para compatibilidad
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
