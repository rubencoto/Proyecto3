package com.ulatina.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaSinpe {
    private int id;
    private String numeroReferencia;
    private String cuentaOrigen;
    private String cuentaDestino;
    private String bancoDestino;
    private String beneficiario;
    private BigDecimal monto;
    private String concepto;
    private String estado; // PROCESANDO, COMPLETADA, FALLIDA, RECHAZADA
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaProceso;
    private String codigoError;
    private String comprobante;
    private int tiempoRespuesta; // en segundos

    public TransferenciaSinpe() {}

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroReferencia() { return numeroReferencia; }
    public void setNumeroReferencia(String numeroReferencia) { this.numeroReferencia = numeroReferencia; }

    public String getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(String cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }

    public String getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(String cuentaDestino) { this.cuentaDestino = cuentaDestino; }

    public String getBancoDestino() { return bancoDestino; }
    public void setBancoDestino(String bancoDestino) { this.bancoDestino = bancoDestino; }

    public String getBeneficiario() { return beneficiario; }
    public void setBeneficiario(String beneficiario) { this.beneficiario = beneficiario; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaProceso() { return fechaProceso; }
    public void setFechaProceso(LocalDateTime fechaProceso) { this.fechaProceso = fechaProceso; }

    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }

    public String getComprobante() { return comprobante; }
    public void setComprobante(String comprobante) { this.comprobante = comprobante; }

    public int getTiempoRespuesta() { return tiempoRespuesta; }
    public void setTiempoRespuesta(int tiempoRespuesta) { this.tiempoRespuesta = tiempoRespuesta; }

    // Métodos de compatibilidad para ServicioSinpe
    public String getReferencia() { return numeroReferencia; }
    public void setReferencia(String referencia) { this.numeroReferencia = referencia; }

    public String getDescripcion() { return concepto; }
    public void setDescripcion(String descripcion) { this.concepto = descripcion; }

    public LocalDateTime getFecha() { return fechaCreacion; }
    public void setFecha(LocalDateTime fecha) { this.fechaCreacion = fecha; }
}
