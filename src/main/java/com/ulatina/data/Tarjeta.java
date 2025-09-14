/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tarjeta {
    private int id;
    private String numeroTarjeta; // PAN - Enmascarado para cumplimiento PCI DSS
    private String numeroTarjetaHash; // Hash del PAN para búsquedas seguras
    private String tipo; // DEBITO, CREDITO
    private String estado; // ACTIVA, BLOQUEADA, VENCIDA, CANCELADA
    private String estadoActivacion; // EMITIDA, ACTIVADA, PENDIENTE_ACTIVACION
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private LocalDate fechaActivacion;
    private LocalDateTime fechaUltimoBloqueo;
    private String motivoBloqueo;
    private int idCuenta; // Cuenta asociada
    private int idCliente; // Cliente propietario

    // Límites de transacciones
    private BigDecimal limiteRetiroDiario;
    private BigDecimal limiteCompraDiaria;
    private BigDecimal limiteTransferenciaDiaria;
    private BigDecimal limiteCredito; // Solo para tarjetas de crédito
    private BigDecimal saldoDisponibleCredito; // Solo para tarjetas de crédito

    // Configuración de seguridad y alertas
    private boolean alertasActivas;
    private boolean transaccionesInternacionalesHabilitadas;
    private boolean comprasOnlineHabilitadas;
    private boolean contactlessHabilitado;
    private int intentosFallidosConsecutivos;
    private LocalDateTime fechaUltimaTransaccion;

    // Campos para auditoría y cumplimiento
    private String tokenPago; // Token para pagos seguros (PCI DSS)
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaModificacion;
    private String creadoPor;
    private String modificadoPor;

    // CVV encriptado (nunca almacenar en texto plano)
    private String cvvHash;

    public Tarjeta() {
        this.estado = "ACTIVA";
        this.estadoActivacion = "EMITIDA";
        this.alertasActivas = true;
        this.transaccionesInternacionalesHabilitadas = false;
        this.comprasOnlineHabilitadas = true;
        this.contactlessHabilitado = true;
        this.intentosFallidosConsecutivos = 0;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Tarjeta(int id, String numeroTarjeta, String tipo, int idCuenta, int idCliente) {
        this();
        this.id = id;
        this.numeroTarjeta = enmascararNumeroTarjeta(numeroTarjeta);
        this.numeroTarjetaHash = generarHash(numeroTarjeta);
        this.tipo = tipo;
        this.idCuenta = idCuenta;
        this.idCliente = idCliente;
        this.fechaEmision = LocalDate.now();
        this.fechaVencimiento = LocalDate.now().plusYears(3);

        // Límites por defecto según tipo
        if ("DEBITO".equals(tipo)) {
            this.limiteRetiroDiario = new BigDecimal("500000"); // 500,000 CRC
            this.limiteCompraDiaria = new BigDecimal("1000000"); // 1,000,000 CRC
            this.limiteTransferenciaDiaria = new BigDecimal("2000000"); // 2,000,000 CRC
        } else if ("CREDITO".equals(tipo)) {
            this.limiteCredito = new BigDecimal("1000000"); // 1,000,000 CRC
            this.saldoDisponibleCredito = this.limiteCredito;
            this.limiteRetiroDiario = new BigDecimal("200000"); // 200,000 CRC
            this.limiteCompraDiaria = new BigDecimal("800000"); // 800,000 CRC
            this.limiteTransferenciaDiaria = new BigDecimal("500000"); // 500,000 CRC
        }
    }

    // Método para enmascarar número de tarjeta (PCI DSS)
    private String enmascararNumeroTarjeta(String numeroCompleto) {
        if (numeroCompleto == null || numeroCompleto.length() < 8) {
            return "****-****";
        }
        String primeros = numeroCompleto.substring(0, 4);
        String ultimos = numeroCompleto.substring(numeroCompleto.length() - 4);
        return primeros + "-****-****-" + ultimos;
    }

    // Método para generar hash seguro del PAN
    private String generarHash(String numeroCompleto) {
        // En implementación real, usar algoritmo seguro como SHA-256 con salt
        return String.valueOf(numeroCompleto.hashCode());
    }

    // Métodos para gestión de estados
    public boolean activar() {
        if ("EMITIDA".equals(this.estadoActivacion) || "PENDIENTE_ACTIVACION".equals(this.estadoActivacion)) {
            this.estadoActivacion = "ACTIVADA";
            this.fechaActivacion = LocalDate.now();
            this.fechaUltimaModificacion = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public boolean bloquear(String motivo) {
        if ("ACTIVA".equals(this.estado)) {
            this.estado = "BLOQUEADA";
            this.motivoBloqueo = motivo;
            this.fechaUltimoBloqueo = LocalDateTime.now();
            this.fechaUltimaModificacion = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public boolean desbloquear() {
        if ("BLOQUEADA".equals(this.estado)) {
            this.estado = "ACTIVA";
            this.motivoBloqueo = null;
            this.fechaUltimoBloqueo = null;
            this.intentosFallidosConsecutivos = 0;
            this.fechaUltimaModificacion = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public boolean estaActiva() {
        return "ACTIVA".equals(this.estado) && "ACTIVADA".equals(this.estadoActivacion)
               && LocalDate.now().isBefore(this.fechaVencimiento);
    }

    public boolean puedeTransaccionar(BigDecimal monto, String tipoTransaccion) {
        if (!estaActiva()) return false;

        BigDecimal limite = BigDecimal.ZERO;
        switch (tipoTransaccion) {
            case "RETIRO":
                limite = this.limiteRetiroDiario;
                break;
            case "COMPRA":
                limite = this.limiteCompraDiaria;
                break;
            case "TRANSFERENCIA":
                limite = this.limiteTransferenciaDiaria;
                break;
        }

        return monto.compareTo(limite) <= 0;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = enmascararNumeroTarjeta(numeroTarjeta);
        this.numeroTarjetaHash = generarHash(numeroTarjeta);
    }

    public String getNumeroTarjetaHash() { return numeroTarjetaHash; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {
        this.estado = estado;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public String getEstadoActivacion() { return estadoActivacion; }
    public void setEstadoActivacion(String estadoActivacion) { this.estadoActivacion = estadoActivacion; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDate getFechaActivacion() { return fechaActivacion; }
    public void setFechaActivacion(LocalDate fechaActivacion) { this.fechaActivacion = fechaActivacion; }

    public LocalDateTime getFechaUltimoBloqueo() { return fechaUltimoBloqueo; }
    public void setFechaUltimoBloqueo(LocalDateTime fechaUltimoBloqueo) { this.fechaUltimoBloqueo = fechaUltimoBloqueo; }

    public String getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public BigDecimal getLimiteRetiroDiario() { return limiteRetiroDiario; }
    public void setLimiteRetiroDiario(BigDecimal limiteRetiroDiario) {
        this.limiteRetiroDiario = limiteRetiroDiario;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public BigDecimal getLimiteCompraDiaria() { return limiteCompraDiaria; }
    public void setLimiteCompraDiaria(BigDecimal limiteCompraDiaria) {
        this.limiteCompraDiaria = limiteCompraDiaria;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public BigDecimal getLimiteTransferenciaDiaria() { return limiteTransferenciaDiaria; }
    public void setLimiteTransferenciaDiaria(BigDecimal limiteTransferenciaDiaria) {
        this.limiteTransferenciaDiaria = limiteTransferenciaDiaria;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public BigDecimal getLimiteCredito() { return limiteCredito; }
    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public BigDecimal getSaldoDisponibleCredito() { return saldoDisponibleCredito; }
    public void setSaldoDisponibleCredito(BigDecimal saldoDisponibleCredito) {
        this.saldoDisponibleCredito = saldoDisponibleCredito;
    }

    public boolean isAlertasActivas() { return alertasActivas; }
    public void setAlertasActivas(boolean alertasActivas) {
        this.alertasActivas = alertasActivas;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public boolean isTransaccionesInternacionalesHabilitadas() { return transaccionesInternacionalesHabilitadas; }
    public void setTransaccionesInternacionalesHabilitadas(boolean transaccionesInternacionalesHabilitadas) {
        this.transaccionesInternacionalesHabilitadas = transaccionesInternacionalesHabilitadas;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public boolean isComprasOnlineHabilitadas() { return comprasOnlineHabilitadas; }
    public void setComprasOnlineHabilitadas(boolean comprasOnlineHabilitadas) {
        this.comprasOnlineHabilitadas = comprasOnlineHabilitadas;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public boolean isContactlessHabilitado() { return contactlessHabilitado; }
    public void setContactlessHabilitado(boolean contactlessHabilitado) {
        this.contactlessHabilitado = contactlessHabilitado;
        this.fechaUltimaModificacion = LocalDateTime.now();
    }

    public int getIntentosFallidosConsecutivos() { return intentosFallidosConsecutivos; }
    public void setIntentosFallidosConsecutivos(int intentosFallidosConsecutivos) {
        this.intentosFallidosConsecutivos = intentosFallidosConsecutivos;
    }

    public LocalDateTime getFechaUltimaTransaccion() { return fechaUltimaTransaccion; }
    public void setFechaUltimaTransaccion(LocalDateTime fechaUltimaTransaccion) {
        this.fechaUltimaTransaccion = fechaUltimaTransaccion;
    }

    public String getTokenPago() { return tokenPago; }
    public void setTokenPago(String tokenPago) { this.tokenPago = tokenPago; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaUltimaModificacion() { return fechaUltimaModificacion; }
    public void setFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
    }

    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }

    public String getModificadoPor() { return modificadoPor; }
    public void setModificadoPor(String modificadoPor) { this.modificadoPor = modificadoPor; }

    public String getCvvHash() { return cvvHash; }
    public void setCvvHash(String cvvHash) { this.cvvHash = cvvHash; }
}
