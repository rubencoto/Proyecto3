package com.ulatina.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Clase de prueba para verificar la conectividad a la base de datos
 * Prueba tanto conexión directa JDBC como JPA/Hibernate
 */
public class TestConexionBD {

    private static final Logger logger = Logger.getLogger(TestConexionBD.class.getName());

    // Configuración de base de datos (usando las mismas variables que Servicio)
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

    public static void main(String[] args) {
        System.out.println("=== INICIO DE PRUEBAS DE CONEXIÓN A BASE DE DATOS ===\n");

        TestConexionBD test = new TestConexionBD();

        // Ejecutar todas las pruebas
        test.probarConexionDirecta();
        System.out.println();
        test.probarConexionJPA();
        System.out.println();
        test.probarOperacionesBasicas();

        System.out.println("\n=== FIN DE PRUEBAS ===");
    }

    /**
     * Construye la URL de conexión a la base de datos
     */
    private String construirURL() {
        return "jdbc:mysql://" + host + ":" + puerto + "/" + sid +
               "?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    /**
     * Prueba la conexión directa JDBC
     */
    public void probarConexionDirecta() {
        System.out.println("1. PRUEBA DE CONEXIÓN DIRECTA JDBC");
        System.out.println("----------------------------------");

        Connection conn = null;

        try {
            // Cargar el driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL cargado correctamente");

            // Intentar conectar
            String url = construirURL();
            System.out.println("Conectando a: " + url.replaceAll("password=[^&]*", "password=****"));

            conn = DriverManager.getConnection(url, usuario, clave);

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexión JDBC establecida exitosamente");

                // Obtener información de la base de datos
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("   - Base de datos: " + metaData.getDatabaseProductName());
                System.out.println("   - Versión: " + metaData.getDatabaseProductVersion());
                System.out.println("   - Driver: " + metaData.getDriverName());
                System.out.println("   - Usuario conectado: " + metaData.getUserName());

                // Probar una consulta simple
                try (PreparedStatement ps = conn.prepareStatement("SELECT 1 as test, NOW() as timestamp")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("   - Consulta de prueba: ✅ OK");
                        System.out.println("     * Resultado: " + rs.getInt("test"));
                        System.out.println("     * Timestamp servidor: " + rs.getTimestamp("timestamp"));
                    }
                    rs.close();
                }

                // Verificar validez de la conexión
                if (conn.isValid(5)) {
                    System.out.println("   - Validación de conexión: ✅ OK (timeout: 5 segundos)");
                } else {
                    System.out.println("   - Validación de conexión: ❌ FALLÓ");
                }

            } else {
                System.out.println("❌ Error: Conexión nula o cerrada");
            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Error: Driver MySQL no encontrado");
            System.out.println("   Asegúrate de que mysql-connector-java esté en el classpath");
            logger.log(Level.SEVERE, "Driver no encontrado", e);
        } catch (SQLException e) {
            System.out.println("❌ Error de SQL: " + e.getMessage());
            System.out.println("   - Código de error: " + e.getErrorCode());
            System.out.println("   - Estado SQL: " + e.getSQLState());

            // Mensajes específicos para errores comunes
            if (e.getErrorCode() == 1045) {
                System.out.println("   ⚠️  Error de autenticación: Usuario o contraseña incorrectos");
            } else if (e.getErrorCode() == 2003) {
                System.out.println("   ⚠️  Error de conexión: No se puede conectar al servidor MySQL");
            }

            logger.log(Level.SEVERE, "Error SQL en prueba", e);
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            logger.log(Level.SEVERE, "Error inesperado", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("   - Conexión cerrada correctamente");
                } catch (SQLException e) {
                    System.out.println("   - Error cerrando conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Prueba la conexión JPA/Hibernate
     */
    public void probarConexionJPA() {
        System.out.println("2. PRUEBA DE CONEXIÓN JPA/HIBERNATE");
        System.out.println("-----------------------------------");

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            // Crear EntityManagerFactory
            System.out.println("Creando EntityManagerFactory...");
            emf = Persistence.createEntityManagerFactory("BancoPU");
            System.out.println("✅ EntityManagerFactory creado exitosamente");

            // Crear EntityManager
            System.out.println("Creando EntityManager...");
            em = emf.createEntityManager();
            System.out.println("✅ EntityManager creado exitosamente");

            // Verificar que está abierto
            if (em.isOpen()) {
                System.out.println("✅ EntityManager está abierto y listo para usar");

                // Probar una consulta nativa simple
                try {
                    Object resultado = em.createNativeQuery("SELECT 1 as test").getSingleResult();
                    System.out.println("   - Consulta nativa de prueba: ✅ OK (resultado: " + resultado + ")");
                } catch (Exception e) {
                    System.out.println("   - Consulta nativa: ❌ Error: " + e.getMessage());
                }

                // Probar consulta de información del servidor
                try {
                    Object version = em.createNativeQuery("SELECT VERSION() as version").getSingleResult();
                    System.out.println("   - Versión MySQL: " + version);
                } catch (Exception e) {
                    System.out.println("   - Error obteniendo versión: " + e.getMessage());
                }

                // Verificar configuración de Hibernate
                System.out.println("   - Configuración JPA/Hibernate cargada correctamente");

            } else {
                System.out.println("❌ EntityManager no está abierto");
            }

        } catch (Exception e) {
            System.out.println("❌ Error en conexión JPA: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("   - Causa raíz: " + e.getCause().getMessage());
            }

            // Sugerir soluciones para errores comunes de JPA
            String mensaje = e.getMessage().toLowerCase();
            if (mensaje.contains("persistence unit") || mensaje.contains("bancoju")) {
                System.out.println("   ⚠️  Verifica que persistence.xml esté correctamente configurado");
            } else if (mensaje.contains("datasource") || mensaje.contains("bancods")) {
                System.out.println("   ⚠️  Verifica que el DataSource esté configurado en context.xml");
            }

            logger.log(Level.SEVERE, "Error en prueba JPA", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                System.out.println("   - EntityManager cerrado correctamente");
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
                System.out.println("   - EntityManagerFactory cerrado correctamente");
            }
        }
    }

    /**
     * Prueba operaciones básicas en la base de datos
     */
    public void probarOperacionesBasicas() {
        System.out.println("3. PRUEBA DE OPERACIONES BÁSICAS");
        System.out.println("--------------------------------");

        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = construirURL();
            conn = DriverManager.getConnection(url, usuario, clave);

            // Probar transacciones
            System.out.println("Probando manejo de transacciones...");

            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) as total FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int totalTablas = rs.getInt("total");
                    System.out.println("   - Tablas en la BD: " + totalTablas);
                }
                rs.close();

                conn.commit();
                System.out.println("✅ Transacciones funcionando correctamente");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            // Probar información adicional
            try (PreparedStatement ps = conn.prepareStatement("SELECT DATABASE() as db_name, USER() as current_user")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("   - Base de datos actual: " + rs.getString("db_name"));
                    System.out.println("   - Usuario actual: " + rs.getString("current_user"));
                }
                rs.close();
            }

            // Probar pool de conexiones simulado
            System.out.println("Probando múltiples conexiones...");
            for (int i = 1; i <= 3; i++) {
                try (Connection testConn = DriverManager.getConnection(url, usuario, clave)) {
                    if (testConn.isValid(2)) {
                        System.out.println("   - Conexión " + i + ": ✅ OK");
                    }
                }
            }

            System.out.println("✅ Operaciones básicas completadas exitosamente");

        } catch (Exception e) {
            System.out.println("❌ Error en operaciones básicas: " + e.getMessage());
            logger.log(Level.SEVERE, "Error en pruebas básicas", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error cerrando conexión", e);
                }
            }
        }
    }
}
