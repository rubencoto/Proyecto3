package com.ulatina.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa la configuración de notificaciones por usuario
 */
@Entity
@Table(name = "configuracion_notificacion")
public class ConfiguracionNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, length = 20)
    private String numeroCuenta;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "notificacion_email", nullable = false)
    private Boolean notificacionEmail = true;

    @Column(name = "notificacion_sms", nullable = false)
    private Boolean notificacionSms = true;

    @Column(name = "alertas_limite", nullable = false)
    private Boolean alertasLimite = true;

    @Column(name = "alertas_transacciones", nullable = false)
    private Boolean alertasTransacciones = true;

    @Column(name = "alertas_seguridad", nullable = false)
    private Boolean alertasSeguridad = true;

    @Column(name = "umbral_alerta_porcentaje", nullable = false)
    private Integer umbralAlertaPorcentaje = 80; // Alerta cuando se alcance el 80% del límite

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Constructor vacío
    public ConfiguracionNotificacion() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public ConfiguracionNotificacion(String numeroCuenta, String email, String telefono) {
        this();
        this.numeroCuenta = numeroCuenta;
        this.email = email;
        this.telefono = telefono;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getNotificacionEmail() {
        return notificacionEmail;
    }

    public void setNotificacionEmail(Boolean notificacionEmail) {
        this.notificacionEmail = notificacionEmail;
    }

    public Boolean getNotificacionSms() {
        return notificacionSms;
    }

    public void setNotificacionSms(Boolean notificacionSms) {
        this.notificacionSms = notificacionSms;
    }

    public Boolean getAlertasLimite() {
        return alertasLimite;
    }

    public void setAlertasLimite(Boolean alertasLimite) {
        this.alertasLimite = alertasLimite;
    }

    public Boolean getAlertasTransacciones() {
        return alertasTransacciones;
    }

    public void setAlertasTransacciones(Boolean alertasTransacciones) {
        this.alertasTransacciones = alertasTransacciones;
    }

    public Boolean getAlertasSeguridad() {
        return alertasSeguridad;
    }

    public void setAlertasSeguridad(Boolean alertasSeguridad) {
        this.alertasSeguridad = alertasSeguridad;
    }

    public Integer getUmbralAlertaPorcentaje() {
        return umbralAlertaPorcentaje;
    }

    public void setUmbralAlertaPorcentaje(Integer umbralAlertaPorcentaje) {
        this.umbralAlertaPorcentaje = umbralAlertaPorcentaje;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
