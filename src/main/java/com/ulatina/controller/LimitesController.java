package com.ulatina.controller;

import com.ulatina.data.LimiteTransaccion;
import com.ulatina.data.Alerta;
import com.ulatina.data.ConfiguracionNotificacion;
import com.ulatina.service.ServicioLimite;
import com.ulatina.service.ServicioAlerta;
import com.ulatina.service.ServicioNotificacion;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controlador para gestión de límites, alertas y notificaciones
 */
@Named("limitesController")
@RequestScoped
public class LimitesController {

    private static final Logger logger = Logger.getLogger(LimitesController.class.getName());

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ServicioLimite servicioLimite;

    @Inject
    private ServicioAlerta servicioAlerta;

    @Inject
    private ServicioNotificacion servicioNotificacion;

    // Propiedades para la vista
    private String numeroCuenta;
    private List<LimiteTransaccion> limites;
    private List<Alerta> alertas;
    private ConfiguracionNotificacion configuracionNotificacion;

    // Propiedades para crear/editar límites
    private LimiteTransaccion nuevoLimite;
    private LimiteTransaccion limiteSeleccionado;

    // Propiedades para configuración de notificaciones
    private String emailPrueba;
    private String telefonoPrueba;

    @PostConstruct
    public void init() {
        nuevoLimite = new LimiteTransaccion();
        configuracionNotificacion = new ConfiguracionNotificacion();
    }

    /**
     * Cargar datos de la cuenta
     */
    public void cargarDatosCuenta() {
        if (numeroCuenta != null && !numeroCuenta.isEmpty()) {
            cargarLimites();
            cargarAlertas();
            cargarConfiguracionNotificacion();
        }
    }

    /**
     * Cargar límites de la cuenta
     */
    public void cargarLimites() {
        try {
            limites = servicioLimite.obtenerLimitesPorCuenta(numeroCuenta);
        } catch (Exception e) {
            logger.severe("Error cargando límites: " + e.getMessage());
            mostrarMensajeError("Error cargando límites de la cuenta");
        }
    }

    /**
     * Cargar alertas de la cuenta
     */
    public void cargarAlertas() {
        try {
            alertas = servicioAlerta.obtenerAlertasPorCuenta(numeroCuenta);
        } catch (Exception e) {
            logger.severe("Error cargando alertas: " + e.getMessage());
            mostrarMensajeError("Error cargando alertas de la cuenta");
        }
    }

    /**
     * Cargar configuración de notificaciones
     */
    public void cargarConfiguracionNotificacion() {
        try {
            configuracionNotificacion = em.createQuery(
                "SELECT c FROM ConfiguracionNotificacion c WHERE c.numeroCuenta = :cuenta AND c.activo = true",
                ConfiguracionNotificacion.class
            )
            .setParameter("cuenta", numeroCuenta)
            .getSingleResult();
        } catch (Exception e) {
            // Si no existe, crear una nueva configuración
            configuracionNotificacion = new ConfiguracionNotificacion();
            configuracionNotificacion.setNumeroCuenta(numeroCuenta);
        }
    }

    /**
     * Crear nuevo límite
     */
    @Transactional
    public void crearLimite() {
        try {
            if (validarDatosLimite(nuevoLimite)) {
                servicioLimite.crearLimite(
                    numeroCuenta,
                    nuevoLimite.getTipoLimite(),
                    nuevoLimite.getMontoLimite(),
                    nuevoLimite.getPeriodo()
                );

                mostrarMensajeExito("Límite creado exitosamente");
                cargarLimites();
                nuevoLimite = new LimiteTransaccion(); // Limpiar formulario
            }
        } catch (Exception e) {
            logger.severe("Error creando límite: " + e.getMessage());
            mostrarMensajeError("Error al crear el límite");
        }
    }

    /**
     * Actualizar límite existente
     */
    @Transactional
    public void actualizarLimite() {
        try {
            if (limiteSeleccionado != null && validarDatosLimite(limiteSeleccionado)) {
                em.merge(limiteSeleccionado);
                mostrarMensajeExito("Límite actualizado exitosamente");
                cargarLimites();
            }
        } catch (Exception e) {
            logger.severe("Error actualizando límite: " + e.getMessage());
            mostrarMensajeError("Error al actualizar el límite");
        }
    }

    /**
     * Activar/desactivar límite
     */
    @Transactional
    public void cambiarEstadoLimite(LimiteTransaccion limite) {
        try {
            servicioLimite.cambiarEstadoLimite(limite.getId(), !limite.getActivo());
            cargarLimites();
            String estado = limite.getActivo() ? "desactivado" : "activado";
            mostrarMensajeExito("Límite " + estado + " exitosamente");
        } catch (Exception e) {
            logger.severe("Error cambiando estado del límite: " + e.getMessage());
            mostrarMensajeError("Error al cambiar el estado del límite");
        }
    }

    /**
     * Guardar configuración de notificaciones
     */
    @Transactional
    public void guardarConfiguracionNotificacion() {
        try {
            if (validarConfiguracionNotificacion()) {
                if (configuracionNotificacion.getId() == null) {
                    em.persist(configuracionNotificacion);
                } else {
                    em.merge(configuracionNotificacion);
                }
                mostrarMensajeExito("Configuración de notificaciones guardada exitosamente");
            }
        } catch (Exception e) {
            logger.severe("Error guardando configuración: " + e.getMessage());
            mostrarMensajeError("Error al guardar la configuración de notificaciones");
        }
    }

    /**
     * Enviar notificación de prueba
     */
    public void enviarNotificacionPrueba() {
        try {
            String email = (emailPrueba != null && !emailPrueba.isEmpty()) ? emailPrueba : configuracionNotificacion.getEmail();
            String telefono = (telefonoPrueba != null && !telefonoPrueba.isEmpty()) ? telefonoPrueba : configuracionNotificacion.getTelefono();

            boolean exitoso = servicioNotificacion.enviarNotificacionPrueba(email, telefono);

            if (exitoso) {
                mostrarMensajeExito("Notificación de prueba enviada exitosamente");
            } else {
                mostrarMensajeError("Error enviando notificación de prueba");
            }
        } catch (Exception e) {
            logger.severe("Error en notificación de prueba: " + e.getMessage());
            mostrarMensajeError("Error enviando notificación de prueba");
        }
    }

    /**
     * Simular transacción para probar límites
     */
    public void simularTransaccion(LimiteTransaccion.TipoLimite tipo, BigDecimal monto) {
        try {
            boolean permitida = servicioLimite.verificarLimite(numeroCuenta, tipo, monto);

            if (!permitida) {
                LimiteTransaccion limite = servicioLimite.buscarLimitePorTipo(numeroCuenta, tipo);
                servicioAlerta.generarAlertaLimiteExcedido(numeroCuenta, tipo, monto, limite.getMontoLimite());
                mostrarMensajeError("Transacción rechazada: Límite excedido");
            } else {
                // Verificar si está cerca del límite
                if (servicioLimite.estaProximoAlLimite(numeroCuenta, tipo, monto,
                    configuracionNotificacion.getUmbralAlertaPorcentaje())) {
                    LimiteTransaccion limite = servicioLimite.buscarLimitePorTipo(numeroCuenta, tipo);
                    servicioAlerta.generarAlertaProximidadLimite(numeroCuenta, tipo, monto,
                        limite.getMontoLimite(), configuracionNotificacion.getUmbralAlertaPorcentaje());
                }
                mostrarMensajeExito("Transacción simulada exitosamente");
            }

            cargarAlertas(); // Refrescar alertas
        } catch (Exception e) {
            logger.severe("Error simulando transacción: " + e.getMessage());
            mostrarMensajeError("Error simulando transacción");
        }
    }

    /**
     * Validar datos del límite
     */
    private boolean validarDatosLimite(LimiteTransaccion limite) {
        if (limite.getTipoLimite() == null) {
            mostrarMensajeError("Debe seleccionar un tipo de límite");
            return false;
        }

        if (limite.getMontoLimite() == null || limite.getMontoLimite().compareTo(BigDecimal.ZERO) <= 0) {
            mostrarMensajeError("El monto del límite debe ser mayor a cero");
            return false;
        }

        if (limite.getPeriodo() == null) {
            mostrarMensajeError("Debe seleccionar un período para el límite");
            return false;
        }

        return true;
    }

    /**
     * Validar configuración de notificaciones
     */
    private boolean validarConfiguracionNotificacion() {
        if (configuracionNotificacion.getNotificacionEmail() &&
            !servicioNotificacion.validarEmail(configuracionNotificacion.getEmail())) {
            mostrarMensajeError("Dirección de email inválida");
            return false;
        }

        if (configuracionNotificacion.getNotificacionSms() &&
            !servicioNotificacion.validarTelefono(configuracionNotificacion.getTelefono())) {
            mostrarMensajeError("Número de teléfono inválido");
            return false;
        }

        return true;
    }

    /**
     * Mostrar mensaje de éxito
     */
    private void mostrarMensajeExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    /**
     * Mostrar mensaje de error
     */
    private void mostrarMensajeError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje));
    }

    // Getters y Setters
    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public List<LimiteTransaccion> getLimites() {
        return limites;
    }

    public void setLimites(List<LimiteTransaccion> limites) {
        this.limites = limites;
    }

    public List<Alerta> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<Alerta> alertas) {
        this.alertas = alertas;
    }

    public ConfiguracionNotificacion getConfiguracionNotificacion() {
        return configuracionNotificacion;
    }

    public void setConfiguracionNotificacion(ConfiguracionNotificacion configuracionNotificacion) {
        this.configuracionNotificacion = configuracionNotificacion;
    }

    public LimiteTransaccion getNuevoLimite() {
        return nuevoLimite;
    }

    public void setNuevoLimite(LimiteTransaccion nuevoLimite) {
        this.nuevoLimite = nuevoLimite;
    }

    public LimiteTransaccion getLimiteSeleccionado() {
        return limiteSeleccionado;
    }

    public void setLimiteSeleccionado(LimiteTransaccion limiteSeleccionado) {
        this.limiteSeleccionado = limiteSeleccionado;
    }

    public String getEmailPrueba() {
        return emailPrueba;
    }

    public void setEmailPrueba(String emailPrueba) {
        this.emailPrueba = emailPrueba;
    }

    public String getTelefonoPrueba() {
        return telefonoPrueba;
    }

    public void setTelefonoPrueba(String telefonoPrueba) {
        this.telefonoPrueba = telefonoPrueba;
    }

    // Métodos auxiliares para la vista
    public LimiteTransaccion.TipoLimite[] getTiposLimite() {
        return LimiteTransaccion.TipoLimite.values();
    }

    public LimiteTransaccion.PeriodoLimite[] getPeriodosLimite() {
        return LimiteTransaccion.PeriodoLimite.values();
    }
}
