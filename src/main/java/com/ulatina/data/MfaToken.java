package com.ulatina.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para gestionar tokens de autenticación MFA
 */
@Entity
@Table(name = "mfa_tokens")
public class MfaToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_usuario")
    private int idUsuario;

    private String token;

    @Column(name = "tipo_token")
    private String tipoToken; // SMS, EMAIL, TOTP, PUSH

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    private boolean utilizado;
    private String dispositivo;

    @Column(name = "ip_origen")
    private String ipOrigen;

    @Column(name = "intentos_validacion")
    private int intentosValidacion;

    private boolean activo;

    public MfaToken() {
        this.fechaGeneracion = LocalDateTime.now();
        this.fechaExpiracion = LocalDateTime.now().plusMinutes(5); // Válido por 5 minutos
        this.utilizado = false;
        this.intentosValidacion = 0;
        this.activo = true;
    }

    public MfaToken(int idUsuario, String tipoToken, String dispositivo, String ipOrigen) {
        this();
        this.idUsuario = idUsuario;
        this.tipoToken = tipoToken;
        this.dispositivo = dispositivo;
        this.ipOrigen = ipOrigen;
        this.token = generarToken();
    }

    private String generarToken() {
        // Generar token de 6 dígitos
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    public boolean esValido() {
        return activo && !utilizado && LocalDateTime.now().isBefore(fechaExpiracion)
               && intentosValidacion < 3;
    }

    public boolean validarToken(String tokenIngresado) {
        this.intentosValidacion++;

        if (!esValido()) {
            return false;
        }

        if (this.token.equals(tokenIngresado)) {
            this.utilizado = true;
            return true;
        }

        if (this.intentosValidacion >= 3) {
            this.activo = false;
        }

        return false;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipoToken() { return tipoToken; }
    public void setTipoToken(String tipoToken) { this.tipoToken = tipoToken; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }

    public boolean isUtilizado() { return utilizado; }
    public void setUtilizado(boolean utilizado) { this.utilizado = utilizado; }

    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    public int getIntentosValidacion() { return intentosValidacion; }
    public void setIntentosValidacion(int intentosValidacion) { this.intentosValidacion = intentosValidacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
