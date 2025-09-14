/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.controller;
import com.ulatina.data.Prestamo;
import com.ulatina.data.Cuota;
import com.ulatina.service.Servicio;
import com.ulatina.service.ServicioPrestamo;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Named @SessionScoped
public class PrestamosController implements Serializable {
    Servicio servicio = new Servicio(){};
    ServicioPrestamo sp = new ServicioPrestamo();
    private List<Prestamo> prestamos = new ArrayList<>();
    private List<Cuota> cuotasPrestamo = new ArrayList<>();
    private Prestamo nuevoPrestamo = new Prestamo();
    private int prestamoSeleccionado = 0;
    private String mensaje = "";
    private boolean mostrarSolicitud = false;
    private boolean mostrarAmortizacion = false;
    
    public void cargar(int idCliente) {
        prestamos = sp.listarPorCliente(idCliente);
        servicio.redireccionar("/prestamos.xhtml");
    }
    
    public void inicializarSolicitud() {
        nuevoPrestamo = new Prestamo();
        nuevoPrestamo.setFechaInicio(LocalDate.now());
        mostrarSolicitud = true;
        mensaje = "";
    }
    
    public void solicitarPrestamo() {
        try {
            if (nuevoPrestamo.getMonto() == null || nuevoPrestamo.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
                mensaje = "El monto debe ser mayor a cero";
                return;
            }
            if (nuevoPrestamo.getPlazoMeses() <= 0 || nuevoPrestamo.getPlazoMeses() > 360) {
                mensaje = "El plazo debe estar entre 1 y 360 meses";
                return;
            }
            if (nuevoPrestamo.getTasaAnual() == null || nuevoPrestamo.getTasaAnual().compareTo(BigDecimal.ZERO) <= 0) {
                mensaje = "La tasa de interés debe ser mayor a cero";
                return;
            }
            
            // Obtener ID del cliente desde la sesión
            Integer idCliente = (Integer) servicio.obtenerSesion("idUsuario");
            if (idCliente == null) {
                mensaje = "Error: No se pudo obtener la información del cliente";
                return;
            }
            
            nuevoPrestamo.setIdCliente(idCliente);
            nuevoPrestamo.setEstado("ACTIVO");
            
            sp.crear(nuevoPrestamo);
            
            // Recargar la lista de préstamos
            prestamos = sp.listarPorCliente(idCliente);
            
            mensaje = "Préstamo creado exitosamente";
            mostrarSolicitud = false;
            nuevoPrestamo = new Prestamo();
            
        } catch (Exception e) {
            mensaje = "Error al crear el préstamo: " + e.getMessage();
        }
    }
    
    public void verAmortizacion(int idPrestamo) {
        try {
            prestamoSeleccionado = idPrestamo;
            cuotasPrestamo = sp.obtenerCuotas(idPrestamo);
            mostrarAmortizacion = true;
        } catch (Exception e) {
            mensaje = "Error al cargar la tabla de amortización: " + e.getMessage();
        }
    }
    
    public void pagarCuota(int idCuota) {
        try {
            sp.pagarCuota(idCuota);
            // Recargar cuotas
            cuotasPrestamo = sp.obtenerCuotas(prestamoSeleccionado);
            mensaje = "Cuota pagada exitosamente";
        } catch (Exception e) {
            mensaje = "Error al pagar la cuota: " + e.getMessage();
        }
    }
    
    public void cerrarAmortizacion() {
        mostrarAmortizacion = false;
        cuotasPrestamo.clear();
        prestamoSeleccionado = 0;
    }
    
    public void cancelarSolicitud() {
        mostrarSolicitud = false;
        nuevoPrestamo = new Prestamo();
        mensaje = "";
    }
    
    // Getters y Setters
    public List<Prestamo> getPrestamos() { return prestamos; }
    public List<Cuota> getCuotasPrestamo() { return cuotasPrestamo; }
    public Prestamo getNuevoPrestamo() { return nuevoPrestamo; }
    public void setNuevoPrestamo(Prestamo nuevoPrestamo) { this.nuevoPrestamo = nuevoPrestamo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public boolean isMostrarSolicitud() { return mostrarSolicitud; }
    public boolean isMostrarAmortizacion() { return mostrarAmortizacion; }
    public int getPrestamoSeleccionado() { return prestamoSeleccionado; }
}
