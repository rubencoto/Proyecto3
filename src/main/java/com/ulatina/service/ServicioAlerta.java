package com.ulatina.service;

import com.ulatina.data.Alerta;
import com.ulatina.data.LimiteTransaccion;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ServicioAlerta extends Servicio {

    public List<Alerta> obtenerAlertasPorCuenta(String numeroCuenta) throws Exception {
        List<Alerta> alertas = new ArrayList<>();

        try {
            conectarBD();
            String sql = "SELECT * FROM alerta WHERE numero_cuenta = ? ORDER BY fecha_generacion DESC LIMIT 50";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, numeroCuenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Alerta alerta = mapearAlerta(rs);
                alertas.add(alerta);
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error obteniendo alertas: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return alertas;
    }

    public void generarAlertaLimiteExcedido(String numeroCuenta, LimiteTransaccion.TipoLimite tipo,
                                          BigDecimal montoTransaccion, BigDecimal montoLimite) throws Exception {
        String mensaje = String.format("Límite excedido para %s. Monto: %s, Límite: %s",
            tipo.toString(), montoTransaccion, montoLimite);

        crearAlerta(numeroCuenta, Alerta.TipoAlerta.LIMITE_EXCEDIDO, mensaje,
                   Alerta.PrioridadAlerta.ALTA, montoTransaccion);
    }

    public void generarAlertaProximidadLimite(String numeroCuenta, LimiteTransaccion.TipoLimite tipo,
                                            BigDecimal montoTransaccion, BigDecimal montoLimite,
                                            int umbralPorcentaje) throws Exception {
        String mensaje = String.format("Próximo al límite para %s (%d%%). Monto: %s, Límite: %s",
            tipo.toString(), umbralPorcentaje, montoTransaccion, montoLimite);

        crearAlerta(numeroCuenta, Alerta.TipoAlerta.LIMITE_PROXIMO, mensaje,
                   Alerta.PrioridadAlerta.MEDIA, montoTransaccion);
    }

    public void generarAlertaTransaccionSospechosa(String numeroCuenta, String descripcion,
                                                 BigDecimal monto) throws Exception {
        String mensaje = "Transacción sospechosa detectada: " + descripcion;

        crearAlerta(numeroCuenta, Alerta.TipoAlerta.TRANSACCION_SOSPECHOSA, mensaje,
                   Alerta.PrioridadAlerta.CRITICA, monto);
    }

    private void crearAlerta(String numeroCuenta, Alerta.TipoAlerta tipoAlerta, String mensaje,
                           Alerta.PrioridadAlerta prioridad, BigDecimal montoTransaccion) throws Exception {
        try {
            conectarBD();
            String sql = "INSERT INTO alerta (numero_cuenta, tipo_alerta, mensaje, monto_transaccion, " +
                        "estado, prioridad, fecha_generacion, email_enviado, sms_enviado) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, numeroCuenta);
            ps.setString(2, tipoAlerta.toString());
            ps.setString(3, mensaje);
            ps.setBigDecimal(4, montoTransaccion);
            ps.setString(5, Alerta.EstadoAlerta.PENDIENTE.toString());
            ps.setString(6, prioridad.toString());
            ps.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(8, false);
            ps.setBoolean(9, false);

            ps.executeUpdate();
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error creando alerta: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private Alerta mapearAlerta(ResultSet rs) throws Exception {
        Alerta alerta = new Alerta();
        alerta.setId(rs.getLong("id"));
        alerta.setNumeroCuenta(rs.getString("numero_cuenta"));
        alerta.setTipoAlerta(Alerta.TipoAlerta.valueOf(rs.getString("tipo_alerta")));
        alerta.setMensaje(rs.getString("mensaje"));
        alerta.setMontoTransaccion(rs.getBigDecimal("monto_transaccion"));
        alerta.setEstado(Alerta.EstadoAlerta.valueOf(rs.getString("estado")));
        alerta.setPrioridad(Alerta.PrioridadAlerta.valueOf(rs.getString("prioridad")));
        alerta.setFechaGeneracion(rs.getTimestamp("fecha_generacion").toLocalDateTime());
        if (rs.getTimestamp("fecha_procesamiento") != null) {
            alerta.setFechaProcesamiento(rs.getTimestamp("fecha_procesamiento").toLocalDateTime());
        }
        alerta.setEmailEnviado(rs.getBoolean("email_enviado"));
        alerta.setSmsEnviado(rs.getBoolean("sms_enviado"));
        alerta.setMetadatos(rs.getString("metadatos"));

        return alerta;
    }

    /**
     * Obtiene todas las alertas con estado PENDIENTE
     */
    public List<Alerta> obtenerAlertasPendientes() throws Exception {
        List<Alerta> alertas = new ArrayList<>();

        try {
            conectarBD();
            String sql = "SELECT * FROM alerta WHERE estado = ? ORDER BY fecha_generacion ASC";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, Alerta.EstadoAlerta.PENDIENTE.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Alerta alerta = mapearAlerta(rs);
                alertas.add(alerta);
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error obteniendo alertas pendientes: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return alertas;
    }

    /**
     * Procesa una alerta individual cambiando su estado a PROCESADA
     */
    public void procesarAlerta(Alerta alerta) throws Exception {
        try {
            conectarBD();
            String sql = "UPDATE alerta SET estado = ?, fecha_procesamiento = ? WHERE id = ?";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, Alerta.EstadoAlerta.PROCESADA.toString());
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, alerta.getId());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new Exception("No se encontró la alerta con ID: " + alerta.getId());
            }

            // Actualizar el objeto alerta en memoria
            alerta.setEstado(Alerta.EstadoAlerta.PROCESADA);
            alerta.setFechaProcesamiento(LocalDateTime.now());

            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error procesando alerta: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }
}
