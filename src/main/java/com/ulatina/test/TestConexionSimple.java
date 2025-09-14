package com.ulatina.test;

import java.sql.*;

/**
 * Test simple de conexión a la base de datos - sin dependencias externas
 */
public class TestConexionSimple {

    // Configuración de base de datos
    private static final String HOST = "db-mysql-nyc3-21466-do-user-24289380-0.d.db.ondigitalocean.com";
    private static final String PUERTO = "25060";
    private static final String DATABASE = "defaultdb";
    private static final String USUARIO = "doadmin";
    private static final String CLAVE = ""; // Credencial removida por seguridad

    public static void main(String[] args) {
        System.out.println("=== TEST DE CONEXIÓN A BASE DE DATOS ===\n");

        TestConexionSimple test = new TestConexionSimple();
        test.probarConexion();

        System.out.println("\n=== FIN DEL TEST ===");
    }

    public void probarConexion() {
        Connection conn = null;

        try {
            // Construir URL de conexión
            String url = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + DATABASE +
                        "?useSSL=true&requireSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            System.out.println("🔄 Intentando conectar a la base de datos...");
            System.out.println("   Host: " + HOST);
            System.out.println("   Puerto: " + PUERTO);
            System.out.println("   Base de datos: " + DATABASE);
            System.out.println("   Usuario: " + USUARIO);

            // Cargar driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL cargado correctamente");

            // Establecer conexión
            conn = DriverManager.getConnection(url, USUARIO, CLAVE);

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ CONEXIÓN ESTABLECIDA EXITOSAMENTE");

                // Obtener metadatos
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("\n📊 INFORMACIÓN DE LA BASE DE DATOS:");
                System.out.println("   - Producto: " + metaData.getDatabaseProductName());
                System.out.println("   - Versión: " + metaData.getDatabaseProductVersion());
                System.out.println("   - Driver: " + metaData.getDriverName());
                System.out.println("   - Versión driver: " + metaData.getDriverVersion());

                // Probar consulta básica
                System.out.println("\n🔍 PROBANDO CONSULTAS:");

                try (PreparedStatement ps = conn.prepareStatement("SELECT 1 as test, NOW() as servidor_tiempo, DATABASE() as bd_actual, USER() as usuario_actual")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("   ✅ Consulta básica: OK");
                        System.out.println("      • Resultado prueba: " + rs.getInt("test"));
                        System.out.println("      • Tiempo servidor: " + rs.getTimestamp("servidor_tiempo"));
                        System.out.println("      • BD actual: " + rs.getString("bd_actual"));
                        System.out.println("      • Usuario actual: " + rs.getString("usuario_actual"));
                    }
                    rs.close();
                }

                // Verificar tablas existentes
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) as total_tablas FROM information_schema.tables WHERE table_schema = DATABASE()")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        int totalTablas = rs.getInt("total_tablas");
                        System.out.println("   ✅ Tablas en la BD: " + totalTablas);
                    }
                    rs.close();
                }

                // Probar transacciones
                System.out.println("\n⚡ PROBANDO TRANSACCIONES:");
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement("SELECT 'Transacción OK' as mensaje")) {
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        System.out.println("   ✅ " + rs.getString("mensaje"));
                    }
                    rs.close();
                    conn.commit();
                    System.out.println("   ✅ Commit exitoso");
                } catch (SQLException e) {
                    conn.rollback();
                    System.out.println("   ❌ Error en transacción, rollback ejecutado");
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }

                // Validar conexión
                if (conn.isValid(5)) {
                    System.out.println("\n✅ VALIDACIÓN DE CONEXIÓN: OK (timeout: 5 segundos)");
                } else {
                    System.out.println("\n❌ VALIDACIÓN DE CONEXIÓN: FALLÓ");
                }

                System.out.println("\n🎉 TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE");

            } else {
                System.out.println("❌ Error: Conexión nula o cerrada");
            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ ERROR: Driver MySQL no encontrado");
            System.out.println("   Causa: " + e.getMessage());
            System.out.println("   💡 Solución: Agregar mysql-connector-java al classpath");

        } catch (SQLException e) {
            System.out.println("❌ ERROR DE SQL: " + e.getMessage());
            System.out.println("   Código de error: " + e.getErrorCode());
            System.out.println("   Estado SQL: " + e.getSQLState());

            // Diagnóstico específico
            if (e.getErrorCode() == 1045) {
                System.out.println("   💡 Diagnóstico: Credenciales incorrectas");
            } else if (e.getErrorCode() == 2003) {
                System.out.println("   💡 Diagnóstico: No se puede conectar al servidor (red/firewall)");
            } else if (e.getErrorCode() == 1049) {
                System.out.println("   💡 Diagnóstico: Base de datos no existe");
            } else {
                System.out.println("   💡 Diagnóstico: Error de conexión general");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR INESPERADO: " + e.getMessage());
            e.printStackTrace();

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("\n🔒 Conexión cerrada correctamente");
                } catch (SQLException e) {
                    System.out.println("\n⚠️  Error cerrando conexión: " + e.getMessage());
                }
            }
        }
    }
}
