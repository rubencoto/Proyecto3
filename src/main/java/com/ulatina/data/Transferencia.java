package com.ulatina.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transferencia {
    private int id;
    private String numeroCuentaOrigen;
    private String numeroCuentaDestino;
    private BigDecimal monto;
    private String descripcion;
    private String tipo; // INTERNA, SINPE
    private String estado; // PENDIENTE, COMPLETADA, FALLIDA
    private String referencia;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProceso;

    public Transferencia() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroCuentaOrigen() { return numeroCuentaOrigen; }
    public void setNumeroCuentaOrigen(String numeroCuentaOrigen) { this.numeroCuentaOrigen = numeroCuentaOrigen; }

    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public void setNumeroCuentaDestino(String numeroCuentaDestino) { this.numeroCuentaDestino = numeroCuentaDestino; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaProceso() { return fechaProceso; }
    public void setFechaProceso(LocalDateTime fechaProceso) { this.fechaProceso = fechaProceso; }
}
