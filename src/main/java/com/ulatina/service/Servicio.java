package com.ulatina.service;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class Servicio implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(Servicio.class.getName());
    protected Connection conexion = null;

    // Usar variables de entorno en lugar de credenciales hardcodeadas
    private final String host = System.getenv("DB_HOST") != null ?
        System.getenv("DB_HOST") : "db-mysql-nyc3-21466-do-user-24289380-0.d.db.ondigitalocean.com";
    private final String puerto = System.getenv("DB_PORT") != null ?
        System.getenv("DB_PORT") : "25060";
    private final String sid = System.getenv("DB_NAME") != null ?
        System.getenv("DB_NAME") : "defaultdb";
    private final String usuario = System.getenv("DB_USER") != null ?
        System.getenv("DB_USER") : "doadmin";
    private final String clave = System.getenv("DB_PASSWORD") != null ?
        System.getenv("DB_PASSWORD") : "";

    public void conectarBD() throws ClassNotFoundException, SQLException {
        validarConfiguracion();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = construirURL();
            conexion = DriverManager.getConnection(url, usuario, clave);
            logger.info("Conexión establecida a la base de datos: " + sid);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error de conexión a la base de datos", e);
            throw e;
        }
    }

    private void validarConfiguracion() {
        if (host == null || puerto == null || sid == null || usuario == null || clave == null) {
            throw new IllegalStateException("Variables de entorno de BD no configuradas correctamente");
        }
    }

    private String construirURL() {
        return "jdbc:mysql://" + host + ":" + puerto + "/" + sid +
               "?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    public void cerrarPreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error cerrando PreparedStatement", e);
            }
        }
    }

    public void cerrarResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error cerrando ResultSet", e);
            }
        }
    }

    // Método mejorado para cerrar múltiples recursos de forma segura
    public void cerrarRecursos(AutoCloseable... recursos) {
        for (AutoCloseable recurso : recursos) {
            if (recurso != null) {
                try {
                    recurso.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error cerrando recurso", e);
                }
            }
        }
    }

    public void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                logger.info("Conexión cerrada correctamente");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error cerrando conexión", e);
            }
        }
    }

    protected Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                conectarBD();
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Driver MySQL no encontrado", e);
                throw new SQLException("Driver MySQL no disponible", e);
            }
        }
        return conexion;
    }

    protected void setConexion(Connection conexion) {
        this.conexion = conexion;
    }

    @Override
    public void close() {
        cerrarConexion();
    }

    public void redireccionar(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            logger.warning("Ruta de redirección nula o vacía");
            return;
        }

        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                HttpServletRequest request = (HttpServletRequest)
                    facesContext.getExternalContext().getRequest();
                facesContext.getExternalContext()
                    .redirect(request.getContextPath() + ruta);
            } else {
                logger.warning("FacesContext no disponible para redirección");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error en redirección a: " + ruta, e);
        }
    }

    // Método de utilidad para ejecutar consultas de forma segura
    protected PreparedStatement prepararStatement(String sql) throws SQLException {
        return getConexion().prepareStatement(sql);
    }

    // Método de utilidad para transacciones
    protected void ejecutarEnTransaccion(TransactionCallback callback) throws SQLException {
        Connection conn = getConexion();
        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);
            callback.execute(conn);
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error en rollback", rollbackEx);
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error restaurando autoCommit", e);
            }
        }
    }

    @FunctionalInterface
    protected interface TransactionCallback {
        void execute(Connection conn) throws SQLException;
    }
}
