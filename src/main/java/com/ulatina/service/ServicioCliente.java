/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ulatina.service;

import com.ulatina.data.Cliente;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class ServicioCliente {

    private final ServicioKyc servicioKyc = new ServicioKyc();

    // Simulación de almacenamiento en memoria
    private final Map<Integer, Cliente> clientes = new HashMap<>();
    private int nextId = 1;

    public ServicioCliente() {
        inicializarDatosPrueba();
    }

    private void inicializarDatosPrueba() {
        Cliente cliente1 = new Cliente();
        cliente1.setId(nextId++);
        cliente1.setNombreCompleto("María Rodríguez Jiménez");
        cliente1.setIdentificacion("1-1234-5678");
        cliente1.setEmail("maria.rodriguez@email.com");
        cliente1.setTelefono("8888-1234");
        cliente1.setEstadoKyc("APROBADO");
        cliente1.setKycVerificado(true);
        cliente1.setPaisNacimiento("Costa Rica");
        cliente1.setNacionalidad("Costarricense");
        cliente1.setProfesion("Ingeniera");
        cliente1.setIngresosMensuales("1000000-2000000");
        cliente1.setOrigenFondos("Salario");
        cliente1.setPep(false);
        clientes.put(cliente1.getId(), cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setId(nextId++);
        cliente2.setNombreCompleto("Juan Carlos Pérez Soto");
        cliente2.setIdentificacion("2-2345-6789");
        cliente2.setEmail("juan.perez@email.com");
        cliente2.setTelefono("7777-5678");
        cliente2.setEstadoKyc("PENDIENTE");
        cliente2.setKycVerificado(false);
        clientes.put(cliente2.getId(), cliente2);

        Cliente cliente3 = new Cliente();
        cliente3.setId(nextId++);
        cliente3.setNombreCompleto("Ana Patricia Morales Vega");
        cliente3.setIdentificacion("1-3456-7890");
        cliente3.setEmail("ana.morales@email.com");
        cliente3.setTelefono("6666-9012");
        cliente3.setEstadoKyc("RECHAZADO");
        cliente3.setKycVerificado(false);
        cliente3.setEnListaRestrictiva(true);
        cliente3.setMotivoListaRestrictiva("Actividad sospechosa");
        clientes.put(cliente3.getId(), cliente3);
    }

    public Cliente crear(Cliente cliente) throws Exception {
        cliente.setId(nextId++);

        // Verificar KYC antes de crear
        try {
            boolean esValido = verificarKycInterno(cliente);
            String estadoKyc = esValido ? "APROBADO" : "PENDIENTE";
            cliente.setEstadoKyc(estadoKyc);
            cliente.setKycVerificado(esValido);
            cliente.setFechaVerificacionKyc(LocalDateTime.now());
        } catch (Exception e) {
            cliente.setEstadoKyc("PENDIENTE");
            cliente.setKycVerificado(false);
        }

        // Si el cliente está en lista restrictiva, mostrar advertencia
        if (cliente.isEnListaRestrictiva()) {
            System.out.println("ADVERTENCIA: Cliente creado pero se encuentra en lista restrictiva: " +
                cliente.getMotivoListaRestrictiva());
        }

        clientes.put(cliente.getId(), cliente);
        return cliente;
    }

    public List<Cliente> listar() {
        return new ArrayList<>(clientes.values());
    }

    public List<Cliente> listarTodos() {
        return new ArrayList<>(clientes.values());
    }

    public Cliente obtenerPorId(int id) {
        return clientes.get(id);
    }

    public Cliente obtenerPorCedula(String cedula) {
        return clientes.values().stream()
            .filter(c -> cedula.equals(c.getIdentificacion()))
            .findFirst()
            .orElse(null);
    }

    public void actualizar(Cliente cliente) throws Exception {
        if (clientes.containsKey(cliente.getId())) {
            cliente.setFechaVerificacionKyc(LocalDateTime.now());
            clientes.put(cliente.getId(), cliente);
        }
    }

    public void reverificarKyc(int clienteId) throws Exception {
        Cliente cliente = obtenerPorId(clienteId);
        if (cliente != null) {
            boolean esValido = verificarKycInterno(cliente);
            String nuevoEstado = esValido ? "APROBADO" : "RECHAZADO";
            cliente.setEstadoKyc(nuevoEstado);
            cliente.setKycVerificado(esValido);
            cliente.setFechaVerificacionKyc(LocalDateTime.now());
            actualizar(cliente);
        }
    }

    private boolean verificarKycInterno(Cliente cliente) {
        // Verificar si está en lista restrictiva
        try {
            boolean enListaRestrictiva = servicioKyc.verificarListaRestrictiva(
                cliente.getIdentificacion(),
                cliente.getNombreCompleto()
            );

            if (enListaRestrictiva) {
                cliente.setEnListaRestrictiva(true);
                return false;
            }
        } catch (Exception e) {
            // Si hay error verificando lista restrictiva, continuar con verificación básica
        }

        // Verificación básica de campos requeridos
        return cliente.getIdentificacion() != null &&
               !cliente.getIdentificacion().trim().isEmpty() &&
               cliente.getNombreCompleto() != null &&
               !cliente.getNombreCompleto().trim().isEmpty() &&
               cliente.getEmail() != null &&
               !cliente.getEmail().trim().isEmpty();
    }

    public void eliminar(int id) {
        clientes.remove(id);
    }

    public void suspender(int id) {
        Cliente cliente = clientes.get(id);
        if (cliente != null) {
            cliente.setEstadoKyc("SUSPENDIDO");
        }
    }

    public void activar(int id) {
        Cliente cliente = clientes.get(id);
        if (cliente != null && "SUSPENDIDO".equals(cliente.getEstadoKyc())) {
            cliente.setEstadoKyc("PENDIENTE");
        }
    }

    public List<Cliente> buscarPorNombre(String nombre) {
        return clientes.values().stream()
            .filter(c -> c.getNombreCompleto() != null &&
                        c.getNombreCompleto().toLowerCase().contains(nombre.toLowerCase()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public boolean existeCedula(String cedula) {
        return clientes.values().stream()
            .anyMatch(c -> cedula.equals(c.getIdentificacion()));
    }

    public boolean existeEmail(String email) {
        return clientes.values().stream()
            .anyMatch(c -> email.equals(c.getEmail()));
    }

    /**
     * Listar clientes con KYC pendiente de verificación
     */
    public List<Cliente> listarPendientesKyc() {
        return clientes.values().stream()
            .filter(c -> "PENDIENTE".equals(c.getEstadoKyc()) ||
                        c.getEstadoKyc() == null ||
                        !c.isKycVerificado())
            .sorted((c1, c2) -> Integer.compare(c1.getId(), c2.getId()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
