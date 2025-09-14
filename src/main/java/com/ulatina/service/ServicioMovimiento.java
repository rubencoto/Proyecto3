/*
 * To change this license header header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ulatina.service;

import com.ulatina.data.Movimiento;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

@ApplicationScoped
public class ServicioMovimiento extends Servicio {

    private static final Logger LOGGER = Logger.getLogger(ServicioMovimiento.class.getName());

    public Movimiento registrar(Movimiento movimiento) throws Exception {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser null");
        }

        validarMovimiento(movimiento);

        // Generar referencia si no existe
        if (movimiento.getReferencia() == null || movimiento.getReferencia().trim().isEmpty()) {
            movimiento.setReferencia(generarReferencia(movimiento.getTipoMovimiento()));
        }

        // Establecer fecha actual si no existe
        if (movimiento.getFechaMovimiento() == null) {
            movimiento.setFechaMovimiento(LocalDateTime.now());
        }

        String sql = "INSERT INTO movimiento (numero_cuenta, tipo_movimiento, monto, descripcion, " +
                    "fecha_movimiento, saldo_anterior, saldo_posterior, cuenta_destino, referencia, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, movimiento.getNumeroCuenta());
                ps.setString(2, movimiento.getTipoMovimiento());
                ps.setBigDecimal(3, movimiento.getMonto());
                ps.setString(4, movimiento.getDescripcion());
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(movimiento.getFechaMovimiento()));
                ps.setBigDecimal(6, movimiento.getSaldoAnterior());
                ps.setBigDecimal(7, movimiento.getSaldoPosterior());
                ps.setString(8, movimiento.getCuentaDestino());
                ps.setString(9, movimiento.getReferencia());
                ps.setString(10, movimiento.getEstado());

                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new SQLException("No se pudo insertar el movimiento");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setId(rs.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar movimiento: " + e.getMessage(), e);
            throw new Exception("Error registrando movimiento: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimiento;
    }

    public List<Movimiento> listar() throws Exception {
        return listarConLimite(null);
    }

    public List<Movimiento> listarConLimite(Integer limite) throws Exception {
        List<Movimiento> movimientos = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM movimiento ORDER BY fecha_movimiento DESC");
        if (limite != null && limite > 0) {
            sqlBuilder.append(" LIMIT ?");
        }

        String sql = sqlBuilder.toString();

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                if (limite != null && limite > 0) {
                    ps.setInt(1, limite);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar movimientos: " + e.getMessage(), e);
            throw new Exception("Error listando movimientos: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public List<Movimiento> obtenerPorCuenta(String numeroCuenta) throws Exception {
        return obtenerPorCuentaConLimite(numeroCuenta, null);
    }

    public List<Movimiento> obtenerPorCuentaConLimite(String numeroCuenta, Integer limite) throws Exception {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede ser null o vacío");
        }

        List<Movimiento> movimientos = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM movimiento WHERE numero_cuenta = ? ORDER BY fecha_movimiento DESC");
        if (limite != null && limite > 0) {
            sqlBuilder.append(" LIMIT ?");
        }

        String sql = sqlBuilder.toString();

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, numeroCuenta);
                if (limite != null && limite > 0) {
                    ps.setInt(2, limite);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener movimientos por cuenta: " + e.getMessage(), e);
            throw new Exception("Error obteniendo movimientos por cuenta: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public Movimiento obtenerPorId(Long id) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo");
        }

        Movimiento movimiento = null;
        String sql = "SELECT * FROM movimiento WHERE id = ?";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setLong(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        movimiento = mapearMovimiento(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener movimiento por ID: " + e.getMessage(), e);
            throw new Exception("Error obteniendo movimiento por ID: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimiento;
    }

    public List<Movimiento> obtenerPorTipo(String tipoMovimiento) throws Exception {
        if (tipoMovimiento == null || tipoMovimiento.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser null o vacío");
        }

        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimiento WHERE tipo_movimiento = ? ORDER BY fecha_movimiento DESC";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, tipoMovimiento);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener movimientos por tipo: " + e.getMessage(), e);
            throw new Exception("Error obteniendo movimientos por tipo: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public List<Movimiento> obtenerPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws Exception {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser null");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimiento WHERE fecha_movimiento BETWEEN ? AND ? ORDER BY fecha_movimiento DESC";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setTimestamp(1, java.sql.Timestamp.valueOf(fechaInicio));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(fechaFin));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener movimientos por fecha: " + e.getMessage(), e);
            throw new Exception("Error obteniendo movimientos por fecha: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public BigDecimal calcularSaldoPorCuenta(String numeroCuenta) throws Exception {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede ser null o vacío");
        }

        BigDecimal saldo = BigDecimal.ZERO;
        String sql = "SELECT saldo_posterior FROM movimiento WHERE numero_cuenta = ? " +
                    "ORDER BY fecha_movimiento DESC, id DESC LIMIT 1";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, numeroCuenta);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal saldoPosterior = rs.getBigDecimal("saldo_posterior");
                        if (saldoPosterior != null) {
                            saldo = saldoPosterior;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al calcular saldo: " + e.getMessage(), e);
            throw new Exception("Error calculando saldo: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return saldo;
    }

    public List<Movimiento> obtenerIngresos(String numeroCuenta, int limite) throws Exception {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede ser null o vacío");
        }

        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser un número positivo");
        }

        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimiento WHERE numero_cuenta = ? AND monto > 0 " +
                    "ORDER BY fecha_movimiento DESC LIMIT ?";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, numeroCuenta);
                ps.setInt(2, limite);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener ingresos: " + e.getMessage(), e);
            throw new Exception("Error obteniendo ingresos: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public List<Movimiento> obtenerGastos(String numeroCuenta, int limite) throws Exception {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede ser null o vacío");
        }

        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser un número positivo");
        }

        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimiento WHERE numero_cuenta = ? AND monto < 0 " +
                    "ORDER BY fecha_movimiento DESC LIMIT ?";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, numeroCuenta);
                ps.setInt(2, limite);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener gastos: " + e.getMessage(), e);
            throw new Exception("Error obteniendo gastos: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public List<Movimiento> obtenerMovimientosPorEstado(String estado) throws Exception {
        if (estado == null || estado.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado no puede ser null o vacío");
        }

        List<Movimiento> movimientos = new ArrayList<>();
        String sql = "SELECT * FROM movimiento WHERE estado = ? ORDER BY fecha_movimiento DESC";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, estado);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Movimiento movimiento = mapearMovimiento(rs);
                        movimientos.add(movimiento);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener movimientos por estado: " + e.getMessage(), e);
            throw new Exception("Error obteniendo movimientos por estado: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return movimientos;
    }

    public boolean actualizarEstadoMovimiento(Long id, String nuevoEstado) throws Exception {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un número positivo");
        }

        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo estado no puede ser null o vacío");
        }

        String sql = "UPDATE movimiento SET estado = ? WHERE id = ?";
        boolean actualizado = false;

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setLong(2, id);

                int filasAfectadas = ps.executeUpdate();
                actualizado = filasAfectadas > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estado del movimiento: " + e.getMessage(), e);
            throw new Exception("Error actualizando estado del movimiento: " + e.getMessage(), e);
        } finally {
            cerrarConexion();
        }

        return actualizado;
    }

    private void validarMovimiento(Movimiento movimiento) throws IllegalArgumentException {
        if (movimiento.getNumeroCuenta() == null || movimiento.getNumeroCuenta().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta es requerido");
        }

        if (movimiento.getTipoMovimiento() == null || movimiento.getTipoMovimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido");
        }

        if (movimiento.getMonto() == null) {
        // Validar que el monto no sea cero
        if (movimiento.getMonto().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("El monto no puede ser cero");
        }

            throw new IllegalArgumentException("El monto es requerido");
        }

        if (movimiento.getSaldoAnterior() == null) {
            throw new IllegalArgumentException("El saldo anterior es requerido");
        }

        if (movimiento.getSaldoPosterior() == null) {
        // Validar consistencia de saldos
        BigDecimal saldoCalculado = movimiento.getSaldoAnterior().add(movimiento.getMonto());
        if (saldoCalculado.compareTo(movimiento.getSaldoPosterior()) != 0) {
            throw new IllegalArgumentException("Los saldos no son consistentes: saldo anterior (" +
                movimiento.getSaldoAnterior() + ") + monto (" + movimiento.getMonto() +
                ") debe igual saldo posterior (" + movimiento.getSaldoPosterior() + ")");
        }

        // Validar que no se permitan saldos negativos en retiros si exceden el saldo disponible
        if (movimiento.getMonto().compareTo(BigDecimal.ZERO) < 0 &&
            movimiento.getSaldoPosterior().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El movimiento resultaría en saldo negativo");
        }

            throw new IllegalArgumentException("El saldo posterior es requerido");
        }

        if (movimiento.getEstado() == null || movimiento.getEstado().trim().isEmpty()) {
            movimiento.setEstado("COMPLETADO"); // Estado por defecto
        }
    }

    private String generarReferencia(String tipoMovimiento) {
        if (tipoMovimiento == null) {
            tipoMovimiento = "GENERAL";
        }

        String prefijo;
        switch (tipoMovimiento.toUpperCase()) {
            case "DEPOSITO":
                prefijo = "DEP";
                break;
            case "RETIRO":
                prefijo = "RET";
                break;
            case "TRANSFERENCIA":
                prefijo = "TRF";
                break;
            case "TRANSFERENCIA_SINPE":
                prefijo = "SNP";
                break;
            case "TRANSFERENCIA_SWIFT":
                prefijo = "SWF";
                break;
            default:
                prefijo = "MOV";
        }

        // Usar timestamp más un número aleatorio para mayor unicidad
        return prefijo + "-" + System.currentTimeMillis() + "-" +
               String.format("%04d", new Random().nextInt(10000));
    }

    private Movimiento mapearMovimiento(ResultSet rs) throws SQLException {
        Movimiento movimiento = new Movimiento();
        movimiento.setId(rs.getLong("id"));
        movimiento.setNumeroCuenta(rs.getString("numero_cuenta"));
        movimiento.setTipoMovimiento(rs.getString("tipo_movimiento"));
        movimiento.setMonto(rs.getBigDecimal("monto"));
        movimiento.setDescripcion(rs.getString("descripcion"));

        // Manejo seguro de timestamp que puede ser null
        java.sql.Timestamp timestamp = rs.getTimestamp("fecha_movimiento");
        if (timestamp != null) {
            movimiento.setFechaMovimiento(timestamp.toLocalDateTime());
        }

        movimiento.setSaldoAnterior(rs.getBigDecimal("saldo_anterior"));
        movimiento.setSaldoPosterior(rs.getBigDecimal("saldo_posterior"));
        movimiento.setCuentaDestino(rs.getString("cuenta_destino"));
        movimiento.setReferencia(rs.getString("referencia"));
        movimiento.setEstado(rs.getString("estado"));

        return movimiento;
    }
}
