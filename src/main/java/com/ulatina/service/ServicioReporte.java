package com.ulatina.service;

import com.ulatina.data.Movimiento;
import java.sql.*;
import java.util.*;

public class ServicioReporte extends Servicio {

    public List<Movimiento> movimientosDeCuenta(int idCuenta) {
        List<Movimiento> movimientos = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectarBD();
            ps = getConexion().prepareStatement(
                "SELECT m.*, c.numeroCuenta FROM movimientos m " +
                "INNER JOIN cuentas c ON c.id = ? " +
                "WHERE m.numeroCuenta = c.numeroCuenta " +
                "ORDER BY m.fecha DESC LIMIT 100");
            ps.setInt(1, idCuenta);
            rs = ps.executeQuery();

            while (rs.next()) {
                Movimiento mov = new Movimiento();
                mov.setId(rs.getLong("id"));  // Cambiar getInt a getLong
                // Remover setIdCuenta ya que no existe en la clase Movimiento
                mov.setNumeroCuenta(rs.getString("numeroCuenta"));
                mov.setTipoMovimiento(rs.getString("tipo"));  // Cambiar setTipo a setTipoMovimiento
                mov.setMonto(rs.getBigDecimal("monto"));
                mov.setDescripcion(rs.getString("descripcion"));
                mov.setFechaMovimiento(rs.getTimestamp("fecha").toLocalDateTime());  // Cambiar setFecha a setFechaMovimiento
                mov.setReferencia(rs.getString("referencia"));
                movimientos.add(mov);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo movimientos: " + e.getMessage(), e);
        } finally {
            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);
            cerrarConexion();
        }

        return movimientos;
    }

    public List<Movimiento> movimientosPorNumeroCuenta(String numeroCuenta) {
        List<Movimiento> movimientos = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conectarBD();
            ps = getConexion().prepareStatement(
                "SELECT * FROM movimientos WHERE numeroCuenta = ? ORDER BY fecha DESC LIMIT 100");
            ps.setString(1, numeroCuenta);
            rs = ps.executeQuery();

            while (rs.next()) {
                Movimiento mov = new Movimiento();
                mov.setId(rs.getLong("id"));  // Cambiar getInt a getLong
                mov.setNumeroCuenta(rs.getString("numeroCuenta"));
                mov.setTipoMovimiento(rs.getString("tipo"));  // Cambiar setTipo a setTipoMovimiento
                mov.setMonto(rs.getBigDecimal("monto"));
                mov.setDescripcion(rs.getString("descripcion"));
                mov.setFechaMovimiento(rs.getTimestamp("fecha").toLocalDateTime());  // Cambiar setFecha a setFechaMovimiento
                mov.setReferencia(rs.getString("referencia"));
                movimientos.add(mov);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo movimientos: " + e.getMessage(), e);
        } finally {
            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);
            cerrarConexion();
        }

        return movimientos;
    }
}
