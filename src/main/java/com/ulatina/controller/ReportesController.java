/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.controller;
import com.ulatina.data.Movimiento; import com.ulatina.service.Servicio; import com.ulatina.service.ServicioReporte;
import jakarta.enterprise.context.SessionScoped; import jakarta.inject.Named; import java.io.Serializable; import java.util.*;

@Named @SessionScoped
public class ReportesController implements Serializable {
    Servicio servicio=new Servicio(){};
    ServicioReporte sr=new ServicioReporte();
    private List<Movimiento> movimientos=new ArrayList<>();
    public void cargarMovimientos(int idCuenta){ movimientos=sr.movimientosDeCuenta(idCuenta); servicio.redireccionar("/reportesCliente.xhtml"); }
    public List<Movimiento> getMovimientos(){return movimientos;}
}