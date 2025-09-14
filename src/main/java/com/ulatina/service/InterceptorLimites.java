package com.ulatina.service;

import com.ulatina.data.LimiteTransaccion;
import com.ulatina.data.Movimiento;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Interceptor para verificación automática de límites en transacciones
 */
@Interceptor
@VerificarLimites
public class InterceptorLimites {

    private static final Logger logger = Logger.getLogger(InterceptorLimites.class.getName());

    @Inject
    private ServicioLimite servicioLimite;

    @Inject
    private ServicioAlerta servicioAlerta;

    /**
     * Intercepta las operaciones de transacción para verificar límites
     */
    @AroundInvoke
    public Object verificarLimites(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();

        // Solo interceptar métodos de transacción
        if (methodName.contains("transferir") || methodName.contains("retirar") ||
            methodName.contains("pagar") || methodName.contains("comprar")) {

            try {
                // Extraer parámetros de la transacción
                Object[] parameters = context.getParameters();
                String numeroCuenta = null;
                BigDecimal monto = null;
                LimiteTransaccion.TipoLimite tipoTransaccion = null;

                // Determinar parámetros según el método
                if (parameters.length >= 2) {
                    if (parameters[0] instanceof String) {
                        numeroCuenta = (String) parameters[0];
                    }
                    if (parameters[1] instanceof BigDecimal) {
                        monto = (BigDecimal) parameters[1];
                    }
                }

                // Determinar tipo de transacción por el nombre del método
                tipoTransaccion = determinarTipoTransaccion(methodName);

                if (numeroCuenta != null && monto != null && tipoTransaccion != null) {
                    // Verificar límites antes de proceder
                    boolean limiteCumplido = servicioLimite.verificarLimite(numeroCuenta, tipoTransaccion, monto);

                    if (!limiteCumplido) {
                        LimiteTransaccion limite = servicioLimite.buscarLimitePorTipo(numeroCuenta, tipoTransaccion);
                        servicioAlerta.generarAlertaLimiteExcedido(numeroCuenta, tipoTransaccion, monto, limite.getMontoLimite());

                        throw new RuntimeException("Transacción rechazada: Límite " + tipoTransaccion + " excedido. " +
                                                 "Monto: ₡" + monto + ", Límite: ₡" + limite.getMontoLimite());
                    }

                    // Verificar proximidad al límite
                    if (servicioLimite.estaProximoAlLimite(numeroCuenta, tipoTransaccion, monto, 80)) {
                        LimiteTransaccion limite = servicioLimite.buscarLimitePorTipo(numeroCuenta, tipoTransaccion);
                        servicioAlerta.generarAlertaProximidadLimite(numeroCuenta, tipoTransaccion, monto,
                                                                   limite.getMontoLimite(), 80);
                    }
                }

            } catch (Exception e) {
                logger.warning("Error en interceptor de límites: " + e.getMessage());
            }
        }

        // Proceder con la ejecución normal del método
        return context.proceed();
    }

    /**
     * Determinar el tipo de transacción basado en el nombre del método
     */
    private LimiteTransaccion.TipoLimite determinarTipoTransaccion(String methodName) {
        if (methodName.contains("transferir")) {
            if (methodName.contains("Internacional") || methodName.contains("Swift")) {
                return LimiteTransaccion.TipoLimite.TRANSFERENCIA_INTERNACIONAL;
            } else {
                return LimiteTransaccion.TipoLimite.TRANSFERENCIA_NACIONAL;
            }
        } else if (methodName.contains("retirar") || methodName.contains("atm")) {
            return LimiteTransaccion.TipoLimite.RETIRO_ATM;
        } else if (methodName.contains("comprar") || methodName.contains("tarjeta")) {
            return LimiteTransaccion.TipoLimite.COMPRA_TARJETA;
        } else if (methodName.contains("pagar") || methodName.contains("servicio")) {
            return LimiteTransaccion.TipoLimite.PAGO_SERVICIOS;
        }

        return LimiteTransaccion.TipoLimite.TRANSFERENCIA_NACIONAL; // Por defecto
    }
}
