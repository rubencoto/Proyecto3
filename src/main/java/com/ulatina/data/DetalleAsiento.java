package com.ulatina.data;

import java.math.BigDecimal;

public class DetalleAsiento {
    private int id;
    private int idAsiento;
    private int idCuentaContable;
    private BigDecimal debito;
    private BigDecimal credito;
    private String concepto;

    public DetalleAsiento() {
        this.debito = BigDecimal.ZERO;
        this.credito = BigDecimal.ZERO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdAsiento() { return idAsiento; }
    public void setIdAsiento(int idAsiento) { this.idAsiento = idAsiento; }

    public int getIdCuentaContable() { return idCuentaContable; }
    public void setIdCuentaContable(int idCuentaContable) { this.idCuentaContable = idCuentaContable; }

    public BigDecimal getDebito() { return debito; }
    public void setDebito(BigDecimal debito) { this.debito = debito; }

    public BigDecimal getCredito() { return credito; }
    public void setCredito(BigDecimal credito) { this.credito = credito; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
}
