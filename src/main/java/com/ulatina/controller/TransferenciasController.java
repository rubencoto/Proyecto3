package com.ulatina.controller;

import com.ulatina.data.TransferenciaSinpe;
import com.ulatina.data.TransferenciaSwift;
import com.ulatina.data.Cuenta;
import com.ulatina.data.Usuario;
import com.ulatina.service.ServicioTransferencia;
import com.ulatina.service.ServicioSinpe;
import com.ulatina.service.ServicioCuenta;
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

@Path("/transferencias")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransferenciasController {

    @Inject
    private ServicioTransferencia servicioTransferencia;

    @Inject
    private ServicioSinpe servicioSinpe;

    @Inject
    private ServicioCuenta servicioCuenta;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/sinpe")
    public Response transferirSinpe(JsonObject transferData) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            String cuentaOrigen = transferData.getString("cuentaOrigen");
            String cuentaDestino = transferData.getString("cuentaDestino");
            BigDecimal monto = new BigDecimal(transferData.getString("monto"));
            String descripcion = transferData.getString("descripcion", "");

            // Validaciones
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "El monto debe ser mayor a cero")
                        .build())
                    .build();
            }

            // Verificar saldo suficiente
            Cuenta cuenta = servicioCuenta.obtenerPorNumero(cuentaOrigen);
            if (cuenta == null || cuenta.getSaldo().compareTo(monto) < 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Saldo insuficiente")
                        .build())
                    .build();
            }

            // Procesar transferencia SINPE
            TransferenciaSinpe transferencia = servicioSinpe.procesarTransferencia(
                cuentaOrigen, cuentaDestino, monto, descripcion);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Transferencia SINPE procesada exitosamente")
                    .add("referencia", transferencia.getReferencia())
                    .add("monto", transferencia.getMonto().toString())
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error interno del servidor")
                    .build())
                .build();
        }
    }

    @POST
    @Path("/swift")
    public Response transferirSwift(JsonObject transferData) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            String cuentaOrigen = transferData.getString("cuentaOrigen");
            String bancoBeneficiario = transferData.getString("bancoBeneficiario");
            String swiftCode = transferData.getString("swiftCode");
            String cuentaBeneficiario = transferData.getString("cuentaBeneficiario");
            String nombreBeneficiario = transferData.getString("nombreBeneficiario");
            BigDecimal monto = new BigDecimal(transferData.getString("monto"));
            String concepto = transferData.getString("concepto", "");

            // Validaciones
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "El monto debe ser mayor a cero")
                        .build())
                    .build();
            }

            // Procesar transferencia SWIFT
            TransferenciaSwift transferencia = servicioTransferencia.procesarSwift(
                cuentaOrigen, bancoBeneficiario, swiftCode, cuentaBeneficiario,
                nombreBeneficiario, monto, concepto);

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Transferencia SWIFT enviada exitosamente")
                    .add("referencia", transferencia.getReferencia())
                    .add("monto", transferencia.getMonto().toString())
                    .add("estado", "En proceso")
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error interno del servidor")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/historial")
    public Response obtenerHistorial() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            // Obtener historial de transferencias del usuario
            List<TransferenciaSinpe> historialSinpe = servicioSinpe.obtenerHistorial(usuario.getId());

            return Response.ok(historialSinpe).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al obtener historial")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/cuentas-usuario")
    public Response obtenerCuentasUsuario() {
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
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
