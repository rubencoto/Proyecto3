package com.ulatina.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaSwift {
    private int id;
    private String numeroCuentaOrigen;
    private String codigoSwiftOrigen;
    private String codigoSwiftDestino;
    private String numeroCuentaDestino;
    private String beneficiario;
    private String direccionBeneficiario;
    private String paisBeneficiario;
    private BigDecimal monto;
    private String moneda;
    private String concepto;
    private String estado; // PENDIENTE, ENVIADO, CONFIRMADO, RECHAZADO
    private String referenciaSwift;
    private String mensajeMT103;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaConfirmacion;
    private String codigoError;
    private String mensajeError;
    private BigDecimal comision;
    private String bancoIntermediario;
    private String codigoSwiftIntermediario;

    public TransferenciaSwift() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
        this.moneda = "USD"; // Por defecto USD para transferencias internacionales
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroCuentaOrigen() { return numeroCuentaOrigen; }
    public void setNumeroCuentaOrigen(String numeroCuentaOrigen) { this.numeroCuentaOrigen = numeroCuentaOrigen; }

    public String getCodigoSwiftOrigen() { return codigoSwiftOrigen; }
    public void setCodigoSwiftOrigen(String codigoSwiftOrigen) { this.codigoSwiftOrigen = codigoSwiftOrigen; }

    public String getCodigoSwiftDestino() { return codigoSwiftDestino; }
    public void setCodigoSwiftDestino(String codigoSwiftDestino) { this.codigoSwiftDestino = codigoSwiftDestino; }

    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public void setNumeroCuentaDestino(String numeroCuentaDestino) { this.numeroCuentaDestino = numeroCuentaDestino; }

    public String getBeneficiario() { return beneficiario; }
    public void setBeneficiario(String beneficiario) { this.beneficiario = beneficiario; }

    public String getDireccionBeneficiario() { return direccionBeneficiario; }
    public void setDireccionBeneficiario(String direccionBeneficiario) { this.direccionBeneficiario = direccionBeneficiario; }

    public String getPaisBeneficiario() { return paisBeneficiario; }
    public void setPaisBeneficiario(String paisBeneficiario) { this.paisBeneficiario = paisBeneficiario; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getReferenciaSwift() { return referenciaSwift; }
    public void setReferenciaSwift(String referenciaSwift) { this.referenciaSwift = referenciaSwift; }

    public String getMensajeMT103() { return mensajeMT103; }
    public void setMensajeMT103(String mensajeMT103) { this.mensajeMT103 = mensajeMT103; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; }
    public void setFechaConfirmacion(LocalDateTime fechaConfirmacion) { this.fechaConfirmacion = fechaConfirmacion; }

    public String getCodigoError() { return codigoError; }
    public void setCodigoError(String codigoError) { this.codigoError = codigoError; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }

    public BigDecimal getComision() { return comision; }
    public void setComision(BigDecimal comision) { this.comision = comision; }

    public String getBancoIntermediario() { return bancoIntermediario; }
    public void setBancoIntermediario(String bancoIntermediario) { this.bancoIntermediario = bancoIntermediario; }

    public String getCodigoSwiftIntermediario() { return codigoSwiftIntermediario; }
    public void setCodigoSwiftIntermediario(String codigoSwiftIntermediario) { this.codigoSwiftIntermediario = codigoSwiftIntermediario; }
}
