/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.controller;

import com.ulatina.data.Cliente;
import com.ulatina.data.ListaRestrictiva;
import com.ulatina.service.ServicioCliente;
import com.ulatina.service.ServicioKyc;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class KycController implements Serializable {

    private ServicioKyc servicioKyc = new ServicioKyc();
    private ServicioCliente servicioCliente = new ServicioCliente();

    private List<Cliente> clientesPendientes = new ArrayList<>();
    private List<ListaRestrictiva> listasRestrictivas = new ArrayList<>();
    private Cliente clienteSeleccionado = new Cliente();
    private ListaRestrictiva nuevaEntradaLista = new ListaRestrictiva();

    @PostConstruct
    public void init() {
        cargarDatos();
    }

    public void cargarDatos() {
        try {
            clientesPendientes = servicioCliente.listarPendientesKyc();
            listasRestrictivas = servicioKyc.listarListasRestrictivas();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error cargando datos: " + e.getMessage()));
        }
    }

    /**
     * Aprobar KYC de un cliente
     */
    public void aprobarKyc(Cliente cliente) {
        try {
            cliente.setEstadoKyc("APROBADO");
            cliente.setKycVerificado(true);
            cliente.setFechaVerificacionKyc(LocalDateTime.now());
            cliente.setObservacionesKyc("KYC aprobado manualmente por administrador");

            servicioCliente.actualizar(cliente);
            cargarDatos();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("KYC Aprobado",
                "El KYC del cliente " + cliente.getNombreCompleto() + " ha sido aprobado"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error aprobando KYC: " + e.getMessage()));
        }
    }

    /**
     * Rechazar KYC de un cliente
     */
    public void rechazarKyc(Cliente cliente, String motivo) {
        try {
            cliente.setEstadoKyc("RECHAZADO");
            cliente.setKycVerificado(false);
            cliente.setObservacionesKyc(motivo != null && !motivo.trim().isEmpty() ?
                motivo : "KYC rechazado por administrador");

            servicioCliente.actualizar(cliente);
            cargarDatos();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "KYC Rechazado",
                "El KYC del cliente " + cliente.getNombreCompleto() + " ha sido rechazado"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error rechazando KYC: " + e.getMessage()));
        }
    }

    /**
     * Reverificar KYC automáticamente
     */
    public void reverificarKyc(Cliente cliente) {
        try {
            servicioCliente.reverificarKyc(cliente.getId());
            cargarDatos();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("KYC Reverificado",
                "Se ha ejecutado la reverificación automática para " + cliente.getNombreCompleto()));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error en reverificación: " + e.getMessage()));
        }
    }

    /**
     * Agregar nueva entrada a lista restrictiva
     */
    public void agregarListaRestrictiva() {
        try {
            if (nuevaEntradaLista.getIdentificacion() == null ||
                nuevaEntradaLista.getIdentificacion().trim().isEmpty()) {
                throw new RuntimeException("La identificación es requerida");
            }

            if (nuevaEntradaLista.getNombreCompleto() == null ||
                nuevaEntradaLista.getNombreCompleto().trim().isEmpty()) {
                throw new RuntimeException("El nombre completo es requerido");
            }

            if (nuevaEntradaLista.getTipoLista() == null ||
                nuevaEntradaLista.getTipoLista().trim().isEmpty()) {
                throw new RuntimeException("El tipo de lista es requerido");
            }

            if (nuevaEntradaLista.getMotivoInclusion() == null ||
                nuevaEntradaLista.getMotivoInclusion().trim().isEmpty()) {
                throw new RuntimeException("El motivo de inclusión es requerido");
            }

            servicioKyc.agregarListaRestrictiva(nuevaEntradaLista);

            // Verificar si hay clientes existentes que ahora están en lista restrictiva
            actualizarClientesAfectados(nuevaEntradaLista.getIdentificacion());

            nuevaEntradaLista = new ListaRestrictiva();
            cargarDatos();

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Lista Restrictiva Actualizada",
                "Se ha agregado la nueva entrada a la lista restrictiva"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error agregando a lista restrictiva: " + e.getMessage()));
        }
    }

    /**
     * Actualizar clientes que ahora están en lista restrictiva
     */
    private void actualizarClientesAfectados(String identificacion) {
        try {
            List<Cliente> todosClientes = servicioCliente.listar();
            for (Cliente cliente : todosClientes) {
                if (cliente.getIdentificacion().equals(identificacion)) {
                    servicioCliente.reverificarKyc(cliente.getId());
                }
            }
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo principal
            System.err.println("Error actualizando clientes afectados: " + e.getMessage());
        }
    }

    /**
     * Verificar si un cliente específico está en lista restrictiva
     */
    public void verificarListaRestrictiva(String identificacion, String nombreCompleto) {
        try {
            boolean enLista = servicioKyc.verificarListaRestrictiva(identificacion, nombreCompleto);

            if (enLista) {
                ListaRestrictiva detalle = servicioKyc.obtenerDetalleListaRestrictiva(identificacion);
                String mensaje = "Cliente SE ENCUENTRA en lista restrictiva";
                if (detalle != null) {
                    mensaje += " - Motivo: " + detalle.getMotivoInclusion();
                    mensaje += " - Tipo: " + detalle.getTipoLista();
                }

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Lista Restrictiva", mensaje));
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Lista Restrictiva",
                    "Cliente NO se encuentra en lista restrictiva"));
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                "Error verificando lista restrictiva: " + e.getMessage()));
        }
    }

    // Getters y Setters
    public List<Cliente> getClientesPendientes() {
        return clientesPendientes;
    }

    public void setClientesPendientes(List<Cliente> clientesPendientes) {
        this.clientesPendientes = clientesPendientes;
    }

    public List<ListaRestrictiva> getListasRestrictivas() {
        return listasRestrictivas;
    }

    public void setListasRestrictivas(List<ListaRestrictiva> listasRestrictivas) {
        this.listasRestrictivas = listasRestrictivas;
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
    }

    public ListaRestrictiva getNuevaEntradaLista() {
        return nuevaEntradaLista;
    }

    public void setNuevaEntradaLista(ListaRestrictiva nuevaEntradaLista) {
        this.nuevaEntradaLista = nuevaEntradaLista;
    }

    /**
     * Obtener todos los tipos de lista disponibles
     */
    public String[] getTiposLista() {
        return new String[]{"OFAC", "ONU", "PEP", "INTERNO", "INTERPOL", "NACIONAL"};
    }

    /**
     * Obtener colores para los estados KYC
     */
    public String getColorEstadoKyc(String estado) {
        if (estado == null) return "gray";

        switch (estado.toUpperCase()) {
            case "APROBADO":
                return "green";
            case "RECHAZADO":
                return "red";
            case "PENDIENTE":
                return "orange";
            default:
                return "gray";
        }
    }

    /**
     * Obtener ícono para el estado KYC
     */
    public String getIconoEstadoKyc(String estado) {
        if (estado == null) return "fa-question";

        switch (estado.toUpperCase()) {
            case "APROBADO":
                return "fa-check-circle";
            case "RECHAZADO":
                return "fa-times-circle";
            case "PENDIENTE":
                return "fa-clock";
            default:
                return "fa-question";
        }
    }
}
