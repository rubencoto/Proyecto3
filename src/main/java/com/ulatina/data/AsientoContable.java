// src/main/java/com/ulatina/data/AsientoContable.java
package com.ulatina.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AsientoContable {
    private int id;
    private String numeroAsiento;
    private LocalDateTime fecha;
    private String concepto;
    private String tipoTransaccion;  // DEPOSITO, RETIRO, TRANSFERENCIA, etc.
    private String referenciaTransaccion;
    private BigDecimal totalDebito;
    private BigDecimal totalCredito;
    private boolean balanceado;

    // constructors, getters, setters
    public AsientoContable(){}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroAsiento() { return numeroAsiento; }
    public void setNumeroAsiento(String numeroAsiento) { this.numeroAsiento = numeroAsiento; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }

    public String getReferenciaTransaccion() { return referenciaTransaccion; }
    public void setReferenciaTransaccion(String referenciaTransaccion) { this.referenciaTransaccion = referenciaTransaccion; }

    public BigDecimal getTotalDebito() { return totalDebito; }
    public void setTotalDebito(BigDecimal totalDebito) { this.totalDebito = totalDebito; }

    public BigDecimal getTotalCredito() { return totalCredito; }
    public void setTotalCredito(BigDecimal totalCredito) { this.totalCredito = totalCredito; }

    public boolean isBalanceado() { return balanceado; }
    public void setBalanceado(boolean balanceado) { this.balanceado = balanceado; }
}
