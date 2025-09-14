package com.ulatina.data;

import java.math.BigDecimal;

public class CuentaContable {
    private int id;
    private String codigo;      // 1.1.1.001
    private String nombre;      // "Efectivo en Caja"
    private String tipo;        // ACTIVO, PASIVO, PATRIMONIO, INGRESO, GASTO
    private String naturaleza;  // DEUDORA, ACREEDORA
    private BigDecimal saldo;
    private boolean activa;

    public CuentaContable() {
        this.saldo = BigDecimal.ZERO;
        this.activa = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNaturaleza() { return naturaleza; }
    public void setNaturaleza(String naturaleza) { this.naturaleza = naturaleza; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}
