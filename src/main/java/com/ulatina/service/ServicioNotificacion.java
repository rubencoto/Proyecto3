package com.ulatina.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;

@ApplicationScoped
public class ServicioNotificacion {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[0-9]{4}-[0-9]{4}$|^[0-9]{8}$");

    public boolean enviarNotificacionPrueba(String email, String telefono) {
        boolean exitoso = true;

        try {
            if (email != null && !email.isEmpty()) {
                enviarEmail(email, "Notificación de Prueba",
                           "Esta es una notificación de prueba del sistema bancario.");
            }

            if (telefono != null && !telefono.isEmpty()) {
                enviarSms(telefono, "Notificación de prueba del sistema bancario.");
            }

        } catch (Exception e) {
            System.err.println("Error enviando notificación de prueba: " + e.getMessage());
            exitoso = false;
        }

        return exitoso;
    }

    public boolean enviarAlertaEmail(String email, String asunto, String mensaje) {
        try {
            return enviarEmail(email, asunto, mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando alerta por email: " + e.getMessage());
            return false;
        }
    }

    public boolean enviarAlertaSms(String telefono, String mensaje) {
        try {
            return enviarSms(telefono, mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando alerta por SMS: " + e.getMessage());
            return false;
        }
    }

    public boolean validarEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean validarTelefono(String telefono) {
        return telefono != null && PHONE_PATTERN.matcher(telefono).matches();
    }

    private boolean enviarEmail(String email, String asunto, String mensaje) throws Exception {
        // Simulación de envío de email
        System.out.println("=== ENVIANDO EMAIL ===");
        System.out.println("Para: " + email);
        System.out.println("Asunto: " + asunto);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("======================");

        // En una implementación real, aquí se integraría con un servicio de email
        // como JavaMail, SendGrid, Amazon SES, etc.

        // Simular un pequeño delay
        Thread.sleep(100);

        return true;
    }

    private boolean enviarSms(String telefono, String mensaje) throws Exception {
        // Simulación de envío de SMS
        System.out.println("=== ENVIANDO SMS ===");
        System.out.println("Para: " + telefono);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("===================");

        // En una implementación real, aquí se integraría con un servicio de SMS
        // como Twilio, Amazon SNS, etc.

        // Simular un pequeño delay
        Thread.sleep(100);

        return true;
    }

    public void enviarNotificacionLimiteExcedido(String email, String telefono,
                                               String tipoLimite, String monto) {
        String mensaje = String.format("ALERTA: Límite excedido para %s. Monto: %s",
                                      tipoLimite, monto);

        if (email != null && validarEmail(email)) {
            enviarAlertaEmail(email, "Límite Excedido", mensaje);
        }

        if (telefono != null && validarTelefono(telefono)) {
            enviarAlertaSms(telefono, mensaje);
        }
    }

    public void enviarNotificacionProximidadLimite(String email, String telefono,
                                                 String tipoLimite, String monto,
                                                 int porcentaje) {
        String mensaje = String.format("AVISO: Próximo al límite para %s (%d%%). Monto: %s",
                                      tipoLimite, porcentaje, monto);

        if (email != null && validarEmail(email)) {
            enviarAlertaEmail(email, "Próximo al Límite", mensaje);
        }

        if (telefono != null && validarTelefono(telefono)) {
            enviarAlertaSms(telefono, mensaje);
        }
    }
}
