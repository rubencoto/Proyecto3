package com.ulatina.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa las alertas generadas por el sistema
 */
@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, length = 20)
    private String numeroCuenta;

    @Column(name = "tipo_alerta", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoAlerta tipoAlerta;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "monto_transaccion", precision = 15, scale = 2)
    private BigDecimal montoTransaccion;

    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private EstadoAlerta estado;

    @Column(name = "prioridad", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private PrioridadAlerta prioridad;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "email_enviado")
    private Boolean emailEnviado = false;

    @Column(name = "sms_enviado")
    private Boolean smsEnviado = false;

    @Column(name = "metadatos", columnDefinition = "TEXT")
    private String metadatos; // JSON con información adicional

    // Constructor vacío
    public Alerta() {
        this.fechaGeneracion = LocalDateTime.now();
        this.estado = EstadoAlerta.PENDIENTE;
    }

    // Constructor con parámetros
    public Alerta(String numeroCuenta, TipoAlerta tipoAlerta, String mensaje,
                  PrioridadAlerta prioridad) {
        this();
        this.numeroCuenta = numeroCuenta;
        this.tipoAlerta = tipoAlerta;
        this.mensaje = mensaje;
        this.prioridad = prioridad;
    }

    // Enums
    public enum TipoAlerta {
        LIMITE_EXCEDIDO,
        LIMITE_PROXIMO,
        TRANSACCION_SOSPECHOSA,
        CUENTA_BLOQUEADA,
        INTENTO_ACCESO_FALLIDO,
        CAMBIO_CONFIGURACION
    }

    public enum EstadoAlerta {
        PENDIENTE,
        PROCESANDO,
        ENVIADA,
        FALLIDA,
        CANCELADA
    }

    public enum PrioridadAlerta {
        BAJA,
        MEDIA,
        ALTA,
        CRITICA
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

    public TipoAlerta getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(TipoAlerta tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public BigDecimal getMontoTransaccion() {
        return montoTransaccion;
    }

    public void setMontoTransaccion(BigDecimal montoTransaccion) {
        this.montoTransaccion = montoTransaccion;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlerta estado) {
        this.estado = estado;
    }

    public PrioridadAlerta getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(PrioridadAlerta prioridad) {
        this.prioridad = prioridad;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public Boolean getEmailEnviado() {
        return emailEnviado;
    }

    public void setEmailEnviado(Boolean emailEnviado) {
        this.emailEnviado = emailEnviado;
    }

    public Boolean getSmsEnviado() {
        return smsEnviado;
    }

    public void setSmsEnviado(Boolean smsEnviado) {
        this.smsEnviado = smsEnviado;
    }

    public String getMetadatos() {
        return metadatos;
    }

    public void setMetadatos(String metadatos) {
        this.metadatos = metadatos;
    }
}
