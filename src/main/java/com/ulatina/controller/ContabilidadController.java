package com.ulatina.controller;

import com.ulatina.service.ServicioContabilidad;
import com.ulatina.data.AsientoContable;
import com.ulatina.data.DetalleAsiento;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Named
@SessionScoped
public class ContabilidadController implements Serializable {

    private final ServicioContabilidad servicioContabilidad = new ServicioContabilidad();

    // Filtros para consulta de asientos
    private String tipoTransaccionFiltro;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;

    // Resultados de consultas
    private List<AsientoContable> asientos = new ArrayList<>();
    private List<DetalleAsiento> detallesAsiento = new ArrayList<>();
    private AsientoContable asientoSeleccionado;

    // Variables para reportes
    private BigDecimal totalDebitos = BigDecimal.ZERO;
    private BigDecimal totalCreditos = BigDecimal.ZERO;
    private String mensaje;

    public void consultarAsientos() {
        try {
            asientos = servicioContabilidad.consultarAsientos(
                tipoTransaccionFiltro, fechaDesde, fechaHasta);

            // Calcular totales
            totalDebitos = asientos.stream()
                .map(AsientoContable::getTotalDebito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalCreditos = asientos.stream()
                .map(AsientoContable::getTotalCredito)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            mensaje = "Se encontraron " + asientos.size() + " asientos contables";

        } catch (Exception e) {
            mensaje = "Error consultando asientos: " + e.getMessage();
        }
    }

    public void verDetallesAsiento(AsientoContable asiento) {
        try {
            asientoSeleccionado = asiento;
            detallesAsiento = servicioContabilidad.consultarDetallesAsiento(asiento.getId());
            mensaje = "Detalles del asiento " + asiento.getNumeroAsiento();
        } catch (Exception e) {
            mensaje = "Error obteniendo detalles: " + e.getMessage();
        }
    }

    public void limpiarFiltros() {
        tipoTransaccionFiltro = null;
        fechaDesde = null;
        fechaHasta = null;
        asientos.clear();
        detallesAsiento.clear();
        asientoSeleccionado = null;
        totalDebitos = BigDecimal.ZERO;
        totalCreditos = BigDecimal.ZERO;
        mensaje = null;
    }

    public BigDecimal consultarSaldoCuentaContable(String codigoCuenta) {
        try {
            return servicioContabilidad.obtenerSaldoCuentaContable(codigoCuenta);
        } catch (Exception e) {
            mensaje = "Error consultando saldo: " + e.getMessage();
            return BigDecimal.ZERO;
        }
    }

    // Métodos para generar reportes básicos
    public void generarReporteAsientosPorTipo() {
        try {
            if (tipoTransaccionFiltro == null || tipoTransaccionFiltro.isEmpty()) {
                mensaje = "Debe seleccionar un tipo de transacción";
                return;
            }

            consultarAsientos();
            mensaje = "Reporte de asientos tipo: " + tipoTransaccionFiltro;

        } catch (Exception e) {
            mensaje = "Error generando reporte: " + e.getMessage();
        }
    }

    public boolean isAsientoBalanceado(AsientoContable asiento) {
        return asiento.getTotalDebito().compareTo(asiento.getTotalCredito()) == 0;
    }

    public String getEstadoAsiento(AsientoContable asiento) {
        return isAsientoBalanceado(asiento) ? "Balanceado" : "Desbalanceado";
    }

    public String getClasseEstado(AsientoContable asiento) {
        return isAsientoBalanceado(asiento) ? "text-success" : "text-danger";
    }

    // Getters y Setters
    public String getTipoTransaccionFiltro() { return tipoTransaccionFiltro; }
    public void setTipoTransaccionFiltro(String tipoTransaccionFiltro) {
        this.tipoTransaccionFiltro = tipoTransaccionFiltro;
    }

    public LocalDateTime getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDateTime fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDateTime getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDateTime fechaHasta) { this.fechaHasta = fechaHasta; }

    public List<AsientoContable> getAsientos() { return asientos; }
    public void setAsientos(List<AsientoContable> asientos) { this.asientos = asientos; }

    public List<DetalleAsiento> getDetallesAsiento() { return detallesAsiento; }
    public void setDetallesAsiento(List<DetalleAsiento> detallesAsiento) {
        this.detallesAsiento = detallesAsiento;
    }

    public AsientoContable getAsientoSeleccionado() { return asientoSeleccionado; }
    public void setAsientoSeleccionado(AsientoContable asientoSeleccionado) {
        this.asientoSeleccionado = asientoSeleccionado;
    }

    public BigDecimal getTotalDebitos() { return totalDebitos; }
    public BigDecimal getTotalCreditos() { return totalCreditos; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
