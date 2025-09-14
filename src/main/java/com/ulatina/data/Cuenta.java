/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cuenta {
    private int id;
    private String numeroCuenta; // Cambiar nombre para consistencia
    private String tipo; // AHORRO, CORRIENTE
    private String tipoMoneda; // CRC, USD
    private BigDecimal saldo;
    private String estado; // ACTIVA, BLOQUEADA, CERRADA
    private int idCliente;
    private LocalDateTime fechaApertura;

    public Cuenta(){}

    public Cuenta(int id, String numeroCuenta, String tipo, String tipoMoneda, BigDecimal saldo, String estado, int idCliente){
        this.id = id;
        this.numeroCuenta = numeroCuenta;
        this.tipo = tipo;
        this.tipoMoneda = tipoMoneda;
        this.saldo = saldo;
        this.estado = estado;
        this.idCliente = idCliente;
        this.fechaApertura = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    // Mantener compatibilidad con código existente
    public String getNumero() { return numeroCuenta; }
    public void setNumero(String numero) { this.numeroCuenta = numero; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTipoMoneda() { return tipoMoneda; }
    public void setTipoMoneda(String tipoMoneda) { this.tipoMoneda = tipoMoneda; }

    // Mantener compatibilidad con código existente
    public String getMoneda() { return tipoMoneda; }
    public void setMoneda(String moneda) { this.tipoMoneda = moneda; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
}