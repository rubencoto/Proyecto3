package com.ulatina.service;

import com.ulatina.data.Usuario;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que requieren MFA obligatorio
 * Implementa BNK-F-012 - Autenticación MFA obligatoria
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface RequiereMfa {
}

/**
 * Interceptor para operaciones que requieren MFA obligatorio
 * Implementa BNK-F-012 - Autenticación MFA obligatoria
 */
@RequiereMfa
@Interceptor
@Priority(1000)
public class InterceptorMfa {

    @Inject
    private ServicioMfa servicioMfa;

    @AroundInvoke
    public Object validarMfa(InvocationContext context) throws Exception {

        // Obtener usuario actual de la sesión
        Usuario usuarioActual = obtenerUsuarioActual();

        if (usuarioActual == null) {
            throw new SecurityException("Usuario no autenticado");
        }

        // Verificar si la operación requiere MFA
        if (servicioMfa.requiereMfa(usuarioActual)) {
            if (!servicioMfa.mfaCompletado(usuarioActual.getId())) {

                // Agregar mensaje informativo
                FacesContext facesContext = FacesContext.getCurrentInstance();
                if (facesContext != null) {
                    facesContext.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Verificación requerida",
                        "Esta operación requiere autenticación de múltiples factores"));
                }

                // Retornar sin ejecutar la operación
                return null;
            }
        }

        // Si MFA está completado, proceder con la operación
        return context.proceed();
    }

    private Usuario obtenerUsuarioActual() {
        try {
            // Obtener usuario de la sesión JSF
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null && facesContext.getExternalContext() != null) {
                return (Usuario) facesContext.getExternalContext()
                    .getSessionMap().get("usuarioActual");
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo usuario actual: " + e.getMessage());
        }
        return null;
    }
}
