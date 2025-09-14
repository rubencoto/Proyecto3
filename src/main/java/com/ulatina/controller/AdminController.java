/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ulatina.controller;

import com.ulatina.data.Cliente;
import com.ulatina.data.Cuenta;
import com.ulatina.data.Usuario;
import com.ulatina.service.ServicioCuenta;
import com.ulatina.service.ServicioCliente;
import com.ulatina.service.ServicioKyc;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import java.math.BigDecimal;
import java.util.List;

@Path("/admin")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminController {

    @Inject
    private ServicioCliente servicioCliente;

    @Inject
    private ServicioCuenta servicioCuenta;

    @Inject
    private ServicioKyc servicioKyc;

    @Context
    private HttpServletRequest request;

    private boolean isAdmin() {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "admin".equalsIgnoreCase(usuario.getTipo());
    }

    @GET
    @Path("/estadisticas")
    public Response obtenerEstadisticas() {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            List<Cliente> clientes = servicioCliente.listarTodos();
            List<Cuenta> cuentas = servicioCuenta.listarTodas();

            long clientesActivos = clientes.stream().filter(Cliente::isActivo).count();
            long cuentasActivas = cuentas.stream().filter(c -> "ACTIVA".equals(c.getEstado())).count();

            BigDecimal totalDepositos = cuentas.stream()
                .filter(c -> "ACTIVA".equals(c.getEstado()))
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("totalClientes", clientes.size())
                    .add("clientesActivos", clientesActivos)
                    .add("totalCuentas", cuentas.size())
                    .add("cuentasActivas", cuentasActivas)
                    .add("totalDepositos", totalDepositos.toString())
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener estadísticas")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/clientes")
    public Response listarClientes(@QueryParam("page") @DefaultValue("1") int page,
                                 @QueryParam("size") @DefaultValue("10") int size) {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            List<Cliente> clientes = servicioCliente.listarTodos();

            // Paginación simple
            int start = (page - 1) * size;
            int end = Math.min(start + size, clientes.size());
            List<Cliente> clientesPaginados = clientes.subList(start, end);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("clientes", Json.createArrayBuilder(clientesPaginados))
                    .add("total", clientes.size())
                    .add("page", page)
                    .add("size", size)
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener clientes")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/clientes/{clienteId}")
    public Response obtenerCliente(@PathParam("clienteId") int clienteId) {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            Cliente cliente = servicioCliente.obtenerPorId(clienteId);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Cliente no encontrado")
                        .build())
                    .build();
            }

            return Response.ok(cliente).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener cliente")
                    .build())
                .build();
        }
    }

    @POST
    @Path("/clientes")
    public Response crearCliente(JsonObject clienteData) {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            Cliente cliente = new Cliente();
            cliente.setNombre(clienteData.getString("nombre"));
            cliente.setApellido1(clienteData.getString("apellido1"));
            cliente.setApellido2(clienteData.getString("apellido2", ""));
            cliente.setCedula(clienteData.getString("cedula"));
            cliente.setEmail(clienteData.getString("email"));
            cliente.setTelefono(clienteData.getString("telefono"));
            cliente.setActivo(true);

            Cliente clienteCreado = servicioCliente.crear(cliente);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Cliente creado exitosamente")
                    .add("clienteId", clienteCreado.getId())
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al crear cliente")
                    .build())
                .build();
        }
    }

    @PUT
    @Path("/clientes/{clienteId}")
    public Response actualizarCliente(@PathParam("clienteId") int clienteId, JsonObject clienteData) {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            Cliente cliente = servicioCliente.obtenerPorId(clienteId);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Cliente no encontrado")
                        .build())
                    .build();
            }

            cliente.setNombre(clienteData.getString("nombre", cliente.getNombre()));
            cliente.setApellido1(clienteData.getString("apellido1", cliente.getApellido1()));
            cliente.setApellido2(clienteData.getString("apellido2", cliente.getApellido2()));
            cliente.setEmail(clienteData.getString("email", cliente.getEmail()));
            cliente.setTelefono(clienteData.getString("telefono", cliente.getTelefono()));

            servicioCliente.actualizar(cliente);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Cliente actualizado exitosamente")
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al actualizar cliente")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/cuentas")
    public Response listarCuentas(@QueryParam("page") @DefaultValue("1") int page,
                                @QueryParam("size") @DefaultValue("10") int size) {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            List<Cuenta> cuentas = servicioCuenta.listarTodas();

            // Paginación simple
            int start = (page - 1) * size;
            int end = Math.min(start + size, cuentas.size());
            List<Cuenta> cuentasPaginadas = cuentas.subList(start, end);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("cuentas", Json.createArrayBuilder(cuentasPaginadas))
                    .add("total", cuentas.size())
                    .add("page", page)
                    .add("size", size)
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener cuentas")
                    .build())
                .build();
        }
    }

    @PUT
    @Path("/cuentas/{numeroCuenta}/estado")
    public Response cambiarEstadoCuenta(@PathParam("numeroCuenta") String numeroCuenta,
                                      JsonObject estadoData) {
        if (!isAdmin()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            String nuevoEstado = estadoData.getString("estado");
            String motivo = estadoData.getString("motivo", "");

            Cuenta cuenta = servicioCuenta.obtenerPorNumero(numeroCuenta);
            if (cuenta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Cuenta no encontrada")
                        .build())
                    .build();
            }

            cuenta.setEstado(nuevoEstado);
            servicioCuenta.actualizar(cuenta);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Estado de cuenta actualizado exitosamente")
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al cambiar estado de cuenta")
                    .build())
                .build();
        }
    }
}
