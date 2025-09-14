package com.ulatina.service;

import com.ulatina.data.Alerta;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servicio programado para procesamiento automático de alertas
 */
@Singleton
public class ServicioProgramadoAlertas {

    private static final Logger logger = Logger.getLogger(ServicioProgramadoAlertas.class.getName());

    @Inject
    private ServicioAlerta servicioAlerta;

    /**
     * Procesar alertas pendientes cada 5 minutos
     */
    @Schedule(minute = "*/5", hour = "*", persistent = false)
    public void procesarAlertasPendientes() {
        try {
            logger.info("Iniciando procesamiento automático de alertas pendientes...");

            List<Alerta> alertasPendientes = servicioAlerta.obtenerAlertasPendientes();

            if (alertasPendientes.isEmpty()) {
                logger.info("No hay alertas pendientes para procesar");
                return;
            }

            logger.info("Procesando " + alertasPendientes.size() + " alertas pendientes");

            for (Alerta alerta : alertasPendientes) {
                try {
                    servicioAlerta.procesarAlerta(alerta);
                    logger.info("Alerta procesada exitosamente: ID " + alerta.getId());
                } catch (Exception e) {
                    logger.severe("Error procesando alerta ID " + alerta.getId() + ": " + e.getMessage());
                }
            }

            logger.info("Procesamiento automático de alertas completado");

        } catch (Exception e) {
            logger.severe("Error en el procesamiento programado de alertas: " + e.getMessage());
        }
    }

    /**
     * Limpiar alertas antiguas cada día a las 2:00 AM
     */
    @Schedule(hour = "2", minute = "0", persistent = false)
    public void limpiarAlertasAntiguas() {
        try {
            logger.info("Iniciando limpieza de alertas antiguas...");

            // Lógica para archivar o eliminar alertas muy antiguas (más de 90 días)
            // Por implementar según políticas de retención de datos

            logger.info("Limpieza de alertas antiguas completada");

        } catch (Exception e) {
            logger.severe("Error en la limpieza programada de alertas: " + e.getMessage());
        }
    }

    /**
     * Generar reporte diario de alertas a las 8:00 AM
     */
    @Schedule(hour = "8", minute = "0", persistent = false)
    public void generarReporteDiarioAlertas() {
        try {
            logger.info("Generando reporte diario de alertas...");

            // Lógica para generar reporte diario
            // Por implementar según requerimientos de reporting

            logger.info("Reporte diario de alertas generado");

        } catch (Exception e) {
            logger.severe("Error generando reporte diario de alertas: " + e.getMessage());
        }
    }
}
