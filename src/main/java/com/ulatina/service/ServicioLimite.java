package com.ulatina.service;

import com.ulatina.data.LimiteTransaccion;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ServicioLimite extends Servicio {

    public List<LimiteTransaccion> obtenerLimitesPorCuenta(String numeroCuenta) throws Exception {
        List<LimiteTransaccion> limites = new ArrayList<>();

        try {
            conectarBD();
            String sql = "SELECT * FROM limite_transaccion WHERE numero_cuenta = ? AND activo = true ORDER BY id DESC";
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, numeroCuenta);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LimiteTransaccion limite = mapearLimite(rs);
                limites.add(limite);
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error obteniendo límites: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return limites;
    }

    public void crearLimite(String numeroCuenta, LimiteTransaccion.TipoLimite tipoLimite,
                           BigDecimal montoLimite, LimiteTransaccion.PeriodoLimite periodo) throws Exception {
        try {
            conectarBD();
            String sql = "INSERT INTO limite_transaccion (numero_cuenta, tipo_limite, monto_limite, " +
                        "periodo, activo, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, numeroCuenta);
            ps.setString(2, tipoLimite.toString());
            ps.setBigDecimal(3, montoLimite);
            ps.setString(4, periodo.toString());
            ps.setBoolean(5, true);
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error creando límite: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    public void cambiarEstadoLimite(Long id, boolean nuevoEstado) throws Exception {
        try {
            conectarBD();
            String sql = "UPDATE limite_transaccion SET activo = ?, fecha_modificacion = ? WHERE id = ?";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setBoolean(1, nuevoEstado);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);

            ps.executeUpdate();
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error cambiando estado del límite: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    public boolean verificarLimite(String numeroCuenta, LimiteTransaccion.TipoLimite tipo, BigDecimal monto) throws Exception {
        LimiteTransaccion limite = buscarLimitePorTipo(numeroCuenta, tipo);

        if (limite == null || !limite.getActivo()) {
            return true; // Sin límite configurado, permitir transacción
        }

        return monto.compareTo(limite.getMontoLimite()) <= 0;
    }

    public LimiteTransaccion buscarLimitePorTipo(String numeroCuenta, LimiteTransaccion.TipoLimite tipo) throws Exception {
        LimiteTransaccion limite = null;

        try {
            conectarBD();
            String sql = "SELECT * FROM limite_transaccion WHERE numero_cuenta = ? AND tipo_limite = ? AND activo = true LIMIT 1";

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, numeroCuenta);
            ps.setString(2, tipo.toString());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                limite = mapearLimite(rs);
            }

            cerrarResultSet(rs);
            cerrarPreparedStatement(ps);

        } catch (Exception e) {
            throw new Exception("Error buscando límite: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return limite;
    }

    public boolean estaProximoAlLimite(String numeroCuenta, LimiteTransaccion.TipoLimite tipo,
                                     BigDecimal monto, int umbralPorcentaje) throws Exception {
        LimiteTransaccion limite = buscarLimitePorTipo(numeroCuenta, tipo);

        if (limite == null || !limite.getActivo()) {
            return false;
        }

        BigDecimal umbral = limite.getMontoLimite()
            .multiply(BigDecimal.valueOf(umbralPorcentaje))
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);

        return monto.compareTo(umbral) >= 0 && monto.compareTo(limite.getMontoLimite()) <= 0;
    }

    private LimiteTransaccion mapearLimite(ResultSet rs) throws SQLException {
        LimiteTransaccion limite = new LimiteTransaccion();
        limite.setId(rs.getLong("id"));
        limite.setNumeroCuenta(rs.getString("numero_cuenta"));
        limite.setTipoLimite(LimiteTransaccion.TipoLimite.valueOf(rs.getString("tipo_limite")));
        limite.setMontoLimite(rs.getBigDecimal("monto_limite"));
        limite.setPeriodo(LimiteTransaccion.PeriodoLimite.valueOf(rs.getString("periodo")));
        limite.setActivo(rs.getBoolean("activo"));
        limite.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        if (rs.getTimestamp("fecha_modificacion") != null) {
            limite.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());
        }

        return limite;
    }
}
