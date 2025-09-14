package com.ulatina.controller;

import com.ulatina.data.Usuario;
import com.ulatina.data.MfaToken;
import com.ulatina.service.Servicio;
import com.ulatina.service.ServicioUsuario;
import com.ulatina.service.ServicioMfa;
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
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginController {

    @Inject
    private ServicioUsuario servicioUsuario;

    @Inject
    private ServicioMfa servicioMfa;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("/login")
    public Response login(JsonObject loginData) {
        try {
            String username = loginData.getString("username");
            String password = loginData.getString("password");

            Usuario usuario = servicioUsuario.obtenerPorUsername(username);

            if (usuario == null || !password.equals(usuario.getPassword())) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Credenciales inválidas")
                        .build())
                    .build();
            }

            // Crear sesión
            HttpSession session = request.getSession(true);
            session.setAttribute("usuario", usuario);
            session.setAttribute("userId", usuario.getId());

            // Verificar si requiere MFA
            if (servicioMfa.requiereMfa(usuario)) {
                if (!servicioMfa.mfaCompletado(usuario.getId())) {
                    // Iniciar proceso MFA
                    MfaToken token = servicioMfa.generarToken(usuario.getId(), "SMS");
                    session.setAttribute("mfaToken", token);

                    return Response.ok()
                        .entity(Json.createObjectBuilder()
                            .add("success", true)
                            .add("requiresMfa", true)
                            .add("message", "Token MFA enviado")
                            .build())
                        .build();
                }
            }

            // Login exitoso
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requiresMfa", false);
            response.put("userType", usuario.getTipo());
            response.put("redirectUrl", getRedirectUrl(usuario.getTipo()));

            return Response.ok(response).build();

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
    @Path("/verify-mfa")
    public Response verifyMfa(JsonObject mfaData) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Sesión expirada")
                        .build())
                    .build();
            }

            String tokenInput = mfaData.getString("token");
            MfaToken tokenSesion = (MfaToken) session.getAttribute("mfaToken");
            Usuario usuario = (Usuario) session.getAttribute("usuario");

            if (tokenSesion == null || usuario == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Token MFA no válido")
                        .build())
                    .build();
            }

            if (servicioMfa.validarToken(tokenSesion.getId(), tokenInput)) {
                servicioMfa.marcarMfaCompletado(usuario.getId());
                session.removeAttribute("mfaToken");

                return Response.ok()
                    .entity(Json.createObjectBuilder()
                        .add("success", true)
                        .add("userType", usuario.getTipo())
                        .add("redirectUrl", getRedirectUrl(usuario.getTipo()))
                        .build())
                    .build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Token MFA incorrecto")
                        .build())
                    .build();
            }

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
    @Path("/logout")
    public Response logout() {
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Sesión cerrada exitosamente")
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error al cerrar sesión")
                    .build())
                .build();
        }
    }

    @GET
    @Path("/session")
    public Response getSession() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                        .add("authenticated", false)
                        .build())
                    .build();
            }

            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Json.createObjectBuilder()
                        .add("authenticated", false)
                        .build())
                    .build();
            }

            return Response.ok()
                .entity(Json.createObjectBuilder()
                    .add("authenticated", true)
                    .add("userType", usuario.getTipo())
                    .add("username", usuario.getUsername())
                    .build())
                .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Json.createObjectBuilder()
                    .add("authenticated", false)
                    .add("error", "Error interno del servidor")
                    .build())
                .build();
        }
    }

    private String getRedirectUrl(String userType) {
        switch (userType.toLowerCase()) {
            case "admin":
            case "administrador":
                return "/admin/adminDashboard.html";
            case "cliente":
            default:
                return "/indexCliente.html";
        }
    }
}
