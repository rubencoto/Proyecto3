/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ulatina.controller;

import com.ulatina.data.Cuenta;
import com.ulatina.data.Usuario;
import com.ulatina.data.Movimiento;
import com.ulatina.service.ServicioCuenta;
import com.ulatina.service.ServicioMovimiento;
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

@Path("/cuentas")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CuentasController {

    @Inject
    private ServicioCuenta servicioCuenta;

    @Inject
    private ServicioMovimiento servicioMovimiento;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("/mis-cuentas")
    public Response obtenerMisCuentas() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<Cuenta> cuentas = servicioCuenta.obtenerPorCliente(usuario.getIdCliente());

            return Response.ok(cuentas).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener cuentas")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/{numeroCuenta}")
    public Response obtenerCuenta(@PathParam("numeroCuenta") String numeroCuenta) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Cuenta cuenta = servicioCuenta.obtenerPorNumero(numeroCuenta);

            if (cuenta == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Cuenta no encontrada")
                        .build())
                    .build();
            }

            return Response.ok(cuenta).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener cuenta")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/{numeroCuenta}/movimientos")
    public Response obtenerMovimientos(@PathParam("numeroCuenta") String numeroCuenta,
                                     @QueryParam("limite") @DefaultValue("10") int limite) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<Movimiento> movimientos = servicioMovimiento.obtenerPorCuenta(numeroCuenta, limite);

            return Response.ok(movimientos).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener movimientos")
                    .build())
                .build();
        }
    }

    @POST
    @Path("/crear")
    public Response crearCuenta(JsonObject cuentaData) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            String tipoCuenta = cuentaData.getString("tipoCuenta");
            String moneda = cuentaData.getString("moneda");
            BigDecimal montoInicial = new BigDecimal(cuentaData.getString("montoInicial", "0"));

            Cuenta nuevaCuenta = new Cuenta();
            nuevaCuenta.setIdCliente(usuario.getIdCliente());
            nuevaCuenta.setTipo(tipoCuenta);
            nuevaCuenta.setMoneda(moneda);
            nuevaCuenta.setSaldo(montoInicial);
            nuevaCuenta.setEstado("ACTIVA");

            Cuenta cuentaCreada = servicioCuenta.crear(nuevaCuenta);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Cuenta creada exitosamente")
                    .add("numeroCuenta", cuentaCreada.getNumero())
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al crear cuenta")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/resumen")
    public Response obtenerResumen() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            List<Cuenta> cuentas = servicioCuenta.obtenerPorCliente(usuario.getIdCliente());

            BigDecimal totalCRC = BigDecimal.ZERO;
            BigDecimal totalUSD = BigDecimal.ZERO;
            int cuentasActivas = 0;

            for (Cuenta cuenta : cuentas) {
                if ("ACTIVA".equals(cuenta.getEstado())) {
                    cuentasActivas++;
                    if ("CRC".equals(cuenta.getMoneda())) {
                        totalCRC = totalCRC.add(cuenta.getSaldo());
                    } else if ("USD".equals(cuenta.getMoneda())) {
                        totalUSD = totalUSD.add(cuenta.getSaldo());
                    }
                }
            }

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("totalCuentas", cuentas.size())
                    .add("cuentasActivas", cuentasActivas)
                    .add("saldoTotalCRC", totalCRC.toString())
                    .add("saldoTotalUSD", totalUSD.toString())
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener resumen")
                    .build())
                .build();
        }
    }
}