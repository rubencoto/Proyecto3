package com.ulatina.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa los límites de transacciones configurables por cuenta
 */
@Entity
@Table(name = "limite_transaccion")
public class LimiteTransaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, length = 20)
    private String numeroCuenta;

    @Column(name = "tipo_limite", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoLimite tipoLimite;

    @Column(name = "monto_limite", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoLimite;

    @Column(name = "periodo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PeriodoLimite periodo;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Constructor vacío
    public LimiteTransaccion() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros
    public LimiteTransaccion(String numeroCuenta, TipoLimite tipoLimite,
                           BigDecimal montoLimite, PeriodoLimite periodo) {
        this();
        this.numeroCuenta = numeroCuenta;
        this.tipoLimite = tipoLimite;
        this.montoLimite = montoLimite;
        this.periodo = periodo;
    }

    // Enums
    public enum TipoLimite {
        TRANSFERENCIA_NACIONAL,
        TRANSFERENCIA_INTERNACIONAL,
        RETIRO_ATM,
        COMPRA_TARJETA,
        PAGO_SERVICIOS
    }

    public enum PeriodoLimite {
        DIARIO,
        SEMANAL,
        MENSUAL
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

    public TipoLimite getTipoLimite() {
        return tipoLimite;
    }

    public void setTipoLimite(TipoLimite tipoLimite) {
        this.tipoLimite = tipoLimite;
    }

    public BigDecimal getMontoLimite() {
        return montoLimite;
    }

    public void setMontoLimite(BigDecimal montoLimite) {
        this.montoLimite = montoLimite;
    }

    public PeriodoLimite getPeriodo() {
        return periodo;
    }

    public void setPeriodo(PeriodoLimite periodo) {
        this.periodo = periodo;
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
