package com.ulatina.service;

import com.ulatina.data.ListaRestrictiva;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ServicioKyc extends Servicio {

    private static final Logger logger = Logger.getLogger(ServicioKyc.class.getName());

    public List<ListaRestrictiva> listarListasRestrictivas() throws Exception {
        List<ListaRestrictiva> lista = new ArrayList<>();
        String sql = "SELECT * FROM lista_restrictiva WHERE activo = 1 ORDER BY fecha_inclusion DESC";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lista.add(mapearListaRestrictiva(rs));
                }

                logger.info("Listado de listas restrictivas completado. Registros encontrados: " + lista.size());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de SQL listando listas restrictivas", e);
            throw new Exception("Error de base de datos listando listas restrictivas: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error listando listas restrictivas", e);
            throw new Exception("Error listando listas restrictivas: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return lista;
    }

    public void agregarListaRestrictiva(ListaRestrictiva item) throws Exception {
        // Validaciones de entrada
        if (item == null) {
            throw new IllegalArgumentException("El item no puede ser nulo");
        }
        if (item.getIdentificacion() == null || item.getIdentificacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La identificación es requerida");
        }
        if (item.getNombreCompleto() == null || item.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es requerido");
        }
        if (item.getTipoLista() == null || item.getTipoLista().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de lista es requerido");
        }

        // Verificar si ya existe
        if (existeIdentificacion(item.getIdentificacion())) {
            throw new Exception("Ya existe un registro con la identificación: " + item.getIdentificacion());
        }

        String sql = "INSERT INTO lista_restrictiva (identificacion, nombre_completo, tipo_lista, " +
                    "motivo_inclusion, fecha_inclusion, activo, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            conectarBD();
            conexion.setAutoCommit(false); // Iniciar transacción

            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, item.getIdentificacion().trim());
                ps.setString(2, item.getNombreCompleto().trim());
                ps.setString(3, item.getTipoLista().trim());
                ps.setString(4, item.getMotivoInclusion());
                ps.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                ps.setBoolean(6, true);
                ps.setString(7, item.getObservaciones());

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No se pudo insertar el registro");
                }

                conexion.commit(); // Confirmar transacción
                logger.info("Lista restrictiva agregada exitosamente: " + item.getIdentificacion());
            }
        } catch (SQLException e) {
            try {
                if (conexion != null) {
                    conexion.rollback(); // Revertir cambios
                }
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error en rollback", rollbackEx);
            }
            logger.log(Level.SEVERE, "Error de SQL agregando a lista restrictiva", e);
            throw new Exception("Error de base de datos agregando a lista restrictiva: " + e.getMessage());
        } catch (Exception e) {
            try {
                if (conexion != null) {
                    conexion.rollback();
                }
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error en rollback", rollbackEx);
            }
            logger.log(Level.SEVERE, "Error agregando a lista restrictiva", e);
            throw new Exception("Error agregando a lista restrictiva: " + e.getMessage());
        } finally {
            try {
                if (conexion != null) {
                    conexion.setAutoCommit(true); // Restaurar auto-commit
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error restaurando auto-commit", e);
            }
            cerrarConexion();
        }
    }

    public boolean verificarListaRestrictiva(String identificacion, String nombreCompleto) throws Exception {
        // Validaciones de entrada
        if ((identificacion == null || identificacion.trim().isEmpty()) &&
            (nombreCompleto == null || nombreCompleto.trim().isEmpty())) {
            throw new IllegalArgumentException("Debe proporcionar al menos identificación o nombre completo");
        }

        boolean encontrado = false;
        String sql = "SELECT COUNT(*) FROM lista_restrictiva WHERE " +
                    "(identificacion = ? OR UPPER(nombre_completo) LIKE UPPER(?)) AND activo = 1";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, identificacion != null ? identificacion.trim() : "");
                ps.setString(2, nombreCompleto != null ? "%" + nombreCompleto.trim() + "%" : "");

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        encontrado = rs.getInt(1) > 0;
                    }
                }

                logger.info("Verificación de lista restrictiva completada. Encontrado: " + encontrado);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de SQL verificando lista restrictiva", e);
            throw new Exception("Error de base de datos verificando lista restrictiva: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error verificando lista restrictiva", e);
            throw new Exception("Error verificando lista restrictiva: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return encontrado;
    }

    public ListaRestrictiva obtenerDetalleListaRestrictiva(String identificacion) throws Exception {
        // Validación de entrada
        if (identificacion == null || identificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La identificación es requerida");
        }

        ListaRestrictiva detalle = null;
        String sql = "SELECT * FROM lista_restrictiva WHERE identificacion = ? AND activo = 1 LIMIT 1";

        try {
            conectarBD();
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, identificacion.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        detalle = mapearListaRestrictiva(rs);
                    }
                }

                logger.info("Detalle de lista restrictiva obtenido para: " + identificacion);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de SQL obteniendo detalle de lista restrictiva", e);
            throw new Exception("Error de base de datos obteniendo detalle de lista restrictiva: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error obteniendo detalle de lista restrictiva", e);
            throw new Exception("Error obteniendo detalle de lista restrictiva: " + e.getMessage());
        } finally {
            cerrarConexion();
        }

        return detalle;
    }

    /**
     * Método privado para mapear ResultSet a ListaRestrictiva
     * Evita duplicación de código
     */
    private ListaRestrictiva mapearListaRestrictiva(ResultSet rs) throws SQLException {
        ListaRestrictiva item = new ListaRestrictiva();
        item.setId(rs.getInt("id"));
        item.setIdentificacion(rs.getString("identificacion"));
        item.setNombreCompleto(rs.getString("nombre_completo"));
        item.setTipoLista(rs.getString("tipo_lista"));
        item.setMotivoInclusion(rs.getString("motivo_inclusion"));
        item.setFechaInclusion(rs.getTimestamp("fecha_inclusion").toLocalDateTime());

        if (rs.getTimestamp("fecha_actualizacion") != null) {
            item.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
        }

        item.setActivo(rs.getBoolean("activo"));
        item.setObservaciones(rs.getString("observaciones"));

        return item;
    }

    /**
     * Método privado para verificar si existe una identificación
     */
    private boolean existeIdentificacion(String identificacion) throws Exception {
        String sql = "SELECT COUNT(*) FROM lista_restrictiva WHERE identificacion = ? AND activo = 1";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, identificacion.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error verificando existencia de identificación", e);
            throw new Exception("Error verificando existencia de identificación: " + e.getMessage());
        }

        return false;
    }
}
