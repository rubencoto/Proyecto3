package com.ulatina.controller;

import com.ulatina.data.Tarjeta;
import com.ulatina.service.Servicio;
import com.ulatina.service.ServicioTarjeta;
import com.ulatina.service.ServicioMfa;
import com.ulatina.service.ServicioNotificacion;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Named @SessionScoped
public class TarjetasController implements Serializable {

    private final Servicio servicio = new Servicio() {};
    private final ServicioTarjeta servicioTarjeta = new ServicioTarjeta();

    @Inject
    private ServicioMfa servicioMfa;

    @Inject
    private ServicioNotificacion servicioNotificacion;

    // Propiedades para la vista JSF
    private List<Tarjeta> misTarjetas = new ArrayList<>();
    private Tarjeta nuevaTarjeta = new Tarjeta();
    private Tarjeta tarjetaSeleccionada = new Tarjeta();

    // Propiedades para formularios de diálogo
    private String cvvActivacion;
    private String codigoActivacion;
    private String motivoBloqueo;
    private String observacionesBloqueo;
    private BigDecimal limiteRetiro = BigDecimal.ZERO;
    private BigDecimal limiteCompra = BigDecimal.ZERO;
    private BigDecimal limiteTransferencia = BigDecimal.ZERO;
    private boolean alertasActivas = true;
    private boolean transaccionesInternacionales = false;
    private boolean comprasOnline = true;
    private boolean contactless = true;

    // Propiedades para MFA en operaciones sensibles
    private boolean requiereMfaBloqueo = false;
    private String tokenMfaBloqueo;

    /**
     * Carga las tarjetas del cliente
     */
    public void cargar(String numeroCuenta) {
        try {
            misTarjetas = servicioTarjeta.listarPorCliente(numeroCuenta);
            servicio.redireccionar("/Tarjetas.xhtml");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "No se pudieron cargar las tarjetas: " + e.getMessage()));
        }
    }

    /**
     * Crea una nueva tarjeta
     */
    public void crearTarjeta(String numeroCuenta) {
        try {
            nuevaTarjeta.setNumeroCuenta(numeroCuenta);
            servicioTarjeta.crear(nuevaTarjeta);
            misTarjetas = servicioTarjeta.listarPorCliente(numeroCuenta);
            nuevaTarjeta = new Tarjeta();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                "Tarjeta creada correctamente"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    /**
     * Bloqueo remoto de tarjeta - Funcionalidad principal BNK-F-012
     */
    public void iniciarBloqueoRemoto(Tarjeta tarjeta) {
        this.tarjetaSeleccionada = tarjeta;

        // Verificar que la tarjeta esté activa
        if (!"ACTIVA".equals(tarjeta.getEstado())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                "La tarjeta ya se encuentra bloqueada o inactiva"));
            return;
        }

        // Requerir MFA para operaciones de bloqueo (seguridad adicional)
        if (!servicioMfa.mfaCompletado(obtenerIdUsuarioActual())) {
            requiereMfaBloqueo = true;
            // Generar token MFA específico para esta operación
            try {
                servicioMfa.generarTokenMfa(obtenerUsuarioActual(), "SMS");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Verificación requerida",
                    "Se ha enviado un código de verificación para confirmar el bloqueo"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo enviar el código de verificación"));
            }
        } else {
            // Si ya completó MFA, proceder directamente
            mostrarDialogoBloqueo();
        }
    }

    /**
     * Valida MFA para bloqueo y muestra diálogo
     */
    public void validarMfaBloqueo() {
        if (tokenMfaBloqueo == null || tokenMfaBloqueo.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Debe ingresar el código de verificación"));
            return;
        }

        if (servicioMfa.validarTokenMfa(obtenerIdUsuarioActual(), tokenMfaBloqueo)) {
            requiereMfaBloqueo = false;
            tokenMfaBloqueo = "";
            mostrarDialogoBloqueo();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Código de verificación inválido"));
            tokenMfaBloqueo = "";
        }
    }

    /**
     * Muestra el diálogo para seleccionar motivo de bloqueo
     */
    private void mostrarDialogoBloqueo() {
        // Limpiar campos del diálogo
        motivoBloqueo = "";
        observacionesBloqueo = "";

        // En JSF, esto activaría un modal/diálogo
        // Por ahora, establecemos las propiedades para el diálogo
    }

    /**
     * Confirma el bloqueo de la tarjeta
     */
    public void confirmarBloqueo() {
        if (motivoBloqueo == null || motivoBloqueo.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Debe seleccionar un motivo de bloqueo"));
            return;
        }

        try {
            // Realizar el bloqueo
            String motivoCompleto = motivoBloqueo;
            if (observacionesBloqueo != null && !observacionesBloqueo.trim().isEmpty()) {
                motivoCompleto += " - " + observacionesBloqueo;
            }

            boolean bloqueada = tarjetaSeleccionada.bloquear(motivoCompleto);

            if (bloqueada) {
                // Actualizar en base de datos
                servicioTarjeta.actualizar(tarjetaSeleccionada);

                // Enviar notificación al cliente
                enviarNotificacionBloqueo(tarjetaSeleccionada);

                // Actualizar lista de tarjetas
                cargar(tarjetaSeleccionada.getNumeroCuenta());

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    "Tarjeta bloqueada correctamente. Se ha enviado una notificación al cliente."));

                // Limpiar selección
                tarjetaSeleccionada = new Tarjeta();
                motivoBloqueo = "";
                observacionesBloqueo = "";

            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo bloquear la tarjeta. Verifique su estado actual."));
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error al bloquear la tarjeta: " + e.getMessage()));
        }
    }

    /**
     * Desbloquea una tarjeta (requiere autorización adicional)
     */
    public void desbloquearTarjeta(Tarjeta tarjeta) {
        // Esta operación requiere permisos especiales y MFA adicional
        if (!servicioMfa.mfaCompletado(obtenerIdUsuarioActual())) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Autorización requerida",
                "Esta operación requiere verificación adicional"));
            return;
        }

        try {
            boolean desbloqueada = tarjeta.desbloquear();
            if (desbloqueada) {
                servicioTarjeta.actualizar(tarjeta);

                // Notificar al cliente
                enviarNotificacionDesbloqueo(tarjeta);

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    "Tarjeta desbloqueada correctamente"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "No se pudo desbloquear la tarjeta"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error al desbloquear: " + e.getMessage()));
        }
    }

    /**
     * Actualiza los límites de transacción de la tarjeta
     */
    public void actualizarLimites(Tarjeta tarjeta) {
        try {
            tarjeta.setLimiteRetiroDiario(limiteRetiro);
            tarjeta.setLimiteCompraDiaria(limiteCompra);
            tarjeta.setLimiteTransferenciaDiaria(limiteTransferencia);

            servicioTarjeta.actualizar(tarjeta);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                "Límites actualizados correctamente"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error al actualizar límites: " + e.getMessage()));
        }
    }

    /**
     * Actualiza configuraciones de seguridad de la tarjeta
     */
    public void actualizarConfiguracionSeguridad(Tarjeta tarjeta) {
        try {
            tarjeta.setAlertasActivas(alertasActivas);
            tarjeta.setTransaccionesInternacionalesHabilitadas(transaccionesInternacionales);
            tarjeta.setComprasOnlineHabilitadas(comprasOnline);
            tarjeta.setContactlessHabilitado(contactless);

            servicioTarjeta.actualizar(tarjeta);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                "Configuración de seguridad actualizada"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error al actualizar configuración: " + e.getMessage()));
        }
    }

    /**
     * Envía notificación de bloqueo al cliente
     */
    private void enviarNotificacionBloqueo(Tarjeta tarjeta) {
        try {
            String mensaje = String.format(
                "Su tarjeta terminada en %s ha sido bloqueada. " +
                "Motivo: %s. " +
                "Si no realizó esta acción, contacte inmediatamente al banco.",
                tarjeta.getNumeroTarjeta().substring(tarjeta.getNumeroTarjeta().length() - 4),
                tarjeta.getMotivoBloqueo()
            );

            // Enviar por múltiples canales para mayor seguridad
            servicioNotificacion.enviarNotificacionSeguridad(
                tarjeta.getIdCliente(),
                "Tarjeta Bloqueada",
                mensaje
            );

        } catch (Exception e) {
            // Log del error pero no interrumpir el proceso principal
            System.err.println("Error enviando notificación de bloqueo: " + e.getMessage());
        }
    }

    /**
     * Envía notificación de desbloqueo al cliente
     */
    private void enviarNotificacionDesbloqueo(Tarjeta tarjeta) {
        try {
            String mensaje = String.format(
                "Su tarjeta terminada en %s ha sido desbloqueada y está activa nuevamente. " +
                "Si no solicitó esta acción, contacte inmediatamente al banco.",
                tarjeta.getNumeroTarjeta().substring(tarjeta.getNumeroTarjeta().length() - 4)
            );

            servicioNotificacion.enviarNotificacionSeguridad(
                tarjeta.getIdCliente(),
                "Tarjeta Desbloqueada",
                mensaje
            );

        } catch (Exception e) {
            System.err.println("Error enviando notificación de desbloqueo: " + e.getMessage());
        }
    }

    /**
     * Obtiene el usuario actual de la sesión
     */
    private com.ulatina.data.Usuario obtenerUsuarioActual() {
        // Implementar según la gestión de sesiones del proyecto
        // Por ahora, retornar un usuario mock
        return new com.ulatina.data.Usuario();
    }

    /**
     * Obtiene el ID del usuario actual
     */
    private int obtenerIdUsuarioActual() {
        return obtenerUsuarioActual().getId();
    }

    // Getters y Setters
    public List<Tarjeta> getMisTarjetas() { return misTarjetas; }
    public void setMisTarjetas(List<Tarjeta> misTarjetas) { this.misTarjetas = misTarjetas; }

    public Tarjeta getNuevaTarjeta() { return nuevaTarjeta; }
    public void setNuevaTarjeta(Tarjeta nuevaTarjeta) { this.nuevaTarjeta = nuevaTarjeta; }

    public Tarjeta getTarjetaSeleccionada() { return tarjetaSeleccionada; }
    public void setTarjetaSeleccionada(Tarjeta tarjetaSeleccionada) { this.tarjetaSeleccionada = tarjetaSeleccionada; }

    public String getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    public String getObservacionesBloqueo() { return observacionesBloqueo; }
    public void setObservacionesBloqueo(String observacionesBloqueo) { this.observacionesBloqueo = observacionesBloqueo; }

    public boolean isRequiereMfaBloqueo() { return requiereMfaBloqueo; }
    public void setRequiereMfaBloqueo(boolean requiereMfaBloqueo) { this.requiereMfaBloqueo = requiereMfaBloqueo; }

    public String getTokenMfaBloqueo() { return tokenMfaBloqueo; }
    public void setTokenMfaBloqueo(String tokenMfaBloqueo) { this.tokenMfaBloqueo = tokenMfaBloqueo; }

    public BigDecimal getLimiteRetiro() { return limiteRetiro; }
    public void setLimiteRetiro(BigDecimal limiteRetiro) { this.limiteRetiro = limiteRetiro; }

    public BigDecimal getLimiteCompra() { return limiteCompra; }
    public void setLimiteCompra(BigDecimal limiteCompra) { this.limiteCompra = limiteCompra; }

    public BigDecimal getLimiteTransferencia() { return limiteTransferencia; }
    public void setLimiteTransferencia(BigDecimal limiteTransferencia) { this.limiteTransferencia = limiteTransferencia; }

    public boolean isAlertasActivas() { return alertasActivas; }
    public void setAlertasActivas(boolean alertasActivas) { this.alertasActivas = alertasActivas; }

    public boolean isTransaccionesInternacionales() { return transaccionesInternacionales; }
    public void setTransaccionesInternacionales(boolean transaccionesInternacionales) {
        this.transaccionesInternacionales = transaccionesInternacionales;
    }

    public boolean isComprasOnline() { return comprasOnline; }
    public void setComprasOnline(boolean comprasOnline) { this.comprasOnline = comprasOnline; }

    public boolean isContactless() { return contactless; }
    public void setContactless(boolean contactless) { this.contactless = contactless; }
}
