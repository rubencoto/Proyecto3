package com.ulatina.service;

import com.ulatina.data.AsientoContable;
import com.ulatina.data.DetalleAsiento;
import com.ulatina.data.CuentaContable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicioContabilidad extends Servicio {

    public List<AsientoContable> consultarAsientos(String tipoTransaccion,
                                                  LocalDateTime fechaDesde,
                                                  LocalDateTime fechaHasta) throws Exception {
        List<AsientoContable> asientos = new ArrayList<>();

        try {
            conectarBD();
            StringBuilder sql = new StringBuilder("SELECT * FROM asiento_contable WHERE 1=1");

            if (tipoTransaccion != null && !tipoTransaccion.isEmpty()) {
                sql.append(" AND tipo_transaccion = ?");
            }
            if (fechaDesde != null) {
                sql.append(" AND fecha >= ?");
            }
            if (fechaHasta != null) {
                sql.append(" AND fecha <= ?");
            }
            sql.append(" ORDER BY fecha DESC, id DESC");

            PreparedStatement ps = conexion.prepareStatement(sql.toString());
            int paramIndex = 1;

            if (tipoTransaccion != null && !tipoTransaccion.isEmpty()) {
                ps.setString(paramIndex++, tipoTransaccion);
            }
            if (fechaDesde != null) {
                ps.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(fechaDesde));
            }
            if (fechaHasta != null) {
                ps.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(fechaHasta));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AsientoContable asiento = mapearAsiento(rs);
                asientos.add(asiento);
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error consultando asientos: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return asientos;
    }

    public List<DetalleAsiento> consultarDetallesAsiento(int idAsiento) throws Exception {
        List<DetalleAsiento> detalles = new ArrayList<>();

        try {
            conectarBD();
            String sql = "SELECT d.*, c.codigo as codigo_cuenta, c.nombre as nombre_cuenta " +
                        "FROM detalle_asiento d " +
                        "LEFT JOIN cuenta_contable c ON d.id_cuenta_contable = c.id " +
                        "WHERE d.id_asiento = ? ORDER BY d.id";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idAsiento);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetalleAsiento detalle = mapearDetalle(rs);
                detalles.add(detalle);
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error consultando detalles del asiento: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return detalles;
    }

    public BigDecimal obtenerSaldoCuentaContable(String codigoCuenta) throws Exception {
        BigDecimal saldo = BigDecimal.ZERO;

        try {
            conectarBD();
            String sql = "SELECT saldo FROM cuenta_contable WHERE codigo = ? AND activa = true";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, codigoCuenta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                saldo = rs.getBigDecimal("saldo");
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error obteniendo saldo de cuenta contable: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return saldo;
    }

    public AsientoContable crearAsientoContable(String concepto, String tipoTransaccion,
                                              String referenciaTransaccion) throws Exception {
        AsientoContable asiento = new AsientoContable();

        try {
            conectarBD();
            String sql = "INSERT INTO asiento_contable (numero_asiento, fecha, concepto, " +
                        "tipo_transaccion, referencia_transaccion, total_debito, total_credito, balanceado) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            String numeroAsiento = generarNumeroAsiento();

            PreparedStatement ps = conexion.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, numeroAsiento);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, concepto);
            ps.setString(4, tipoTransaccion);
            ps.setString(5, referenciaTransaccion);
            ps.setBigDecimal(6, BigDecimal.ZERO);
            ps.setBigDecimal(7, BigDecimal.ZERO);
            ps.setBoolean(8, false);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                asiento.setId(rs.getInt(1));
            }

            asiento.setNumeroAsiento(numeroAsiento);
            asiento.setFecha(LocalDateTime.now());
            asiento.setConcepto(concepto);
            asiento.setTipoTransaccion(tipoTransaccion);
            asiento.setReferenciaTransaccion(referenciaTransaccion);
            asiento.setTotalDebito(BigDecimal.ZERO);
            asiento.setTotalCredito(BigDecimal.ZERO);
            asiento.setBalanceado(false);

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error creando asiento contable: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return asiento;
    }

    public void agregarDetalle(int idAsiento, int idCuentaContable,
                              BigDecimal debito, BigDecimal credito, String concepto) throws Exception {
        try {
            conectarBD();
            String sql = "INSERT INTO detalle_asiento (id_asiento, id_cuenta_contable, " +
                        "debito, credito, concepto) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idAsiento);
            ps.setInt(2, idCuentaContable);
            ps.setBigDecimal(3, debito != null ? debito : BigDecimal.ZERO);
            ps.setBigDecimal(4, credito != null ? credito : BigDecimal.ZERO);
            ps.setString(5, concepto);

            ps.executeUpdate();
            cerrarPreparedStatement(ps);

            // Actualizar totales del asiento
            actualizarTotalesAsiento(idAsiento);

        } catch (Exception e) {
            throw new Exception("Error agregando detalle al asiento: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private void actualizarTotalesAsiento(int idAsiento) throws Exception {
        try {
            String sql = "UPDATE asiento_contable SET " +
                        "total_debito = (SELECT COALESCE(SUM(debito), 0) FROM detalle_asiento WHERE id_asiento = ?), " +
                        "total_credito = (SELECT COALESCE(SUM(credito), 0) FROM detalle_asiento WHERE id_asiento = ?), " +
                        "balanceado = ((SELECT COALESCE(SUM(debito), 0) FROM detalle_asiento WHERE id_asiento = ?) = " +
                        "             (SELECT COALESCE(SUM(credito), 0) FROM detalle_asiento WHERE id_asiento = ?)) " +
                        "WHERE id = ?";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, idAsiento);
            ps.setInt(2, idAsiento);
            ps.setInt(3, idAsiento);
            ps.setInt(4, idAsiento);
            ps.setInt(5, idAsiento);

            ps.executeUpdate();
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error actualizando totales del asiento: " + e.getMessage());
        }
    }

    private String generarNumeroAsiento() {
        return "ASI-" + System.currentTimeMillis();
    }

    private AsientoContable mapearAsiento(ResultSet rs) throws Exception {
        AsientoContable asiento = new AsientoContable();
        asiento.setId(rs.getInt("id"));
        asiento.setNumeroAsiento(rs.getString("numero_asiento"));
        asiento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        asiento.setConcepto(rs.getString("concepto"));
        asiento.setTipoTransaccion(rs.getString("tipo_transaccion"));
        asiento.setReferenciaTransaccion(rs.getString("referencia_transaccion"));
        asiento.setTotalDebito(rs.getBigDecimal("total_debito"));
        asiento.setTotalCredito(rs.getBigDecimal("total_credito"));
        asiento.setBalanceado(rs.getBoolean("balanceado"));

        return asiento;
    }

    private DetalleAsiento mapearDetalle(ResultSet rs) throws Exception {
        DetalleAsiento detalle = new DetalleAsiento();
        detalle.setId(rs.getInt("id"));
        detalle.setIdAsiento(rs.getInt("id_asiento"));
        detalle.setIdCuentaContable(rs.getInt("id_cuenta_contable"));
        detalle.setDebito(rs.getBigDecimal("debito"));
        detalle.setCredito(rs.getBigDecimal("credito"));
        detalle.setConcepto(rs.getString("concepto"));

        return detalle;
    }
}
