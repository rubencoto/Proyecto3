package com.ulatina.service;

import com.ulatina.data.MfaToken;
import com.ulatina.data.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar autenticación MFA obligatoria
 */
@ApplicationScoped
public class ServicioMfa {

    // Simulación de almacenamiento en memoria (en producción usar base de datos)
    private Map<Integer, List<MfaToken>> tokensUsuario = new HashMap<>();
    private Map<Integer, Boolean> mfaCompletadoSesion = new HashMap<>();
    private ServicioNotificacion servicioNotificacion = new ServicioNotificacion();

    /**
     * Genera un token MFA para el usuario (método requerido por LoginController)
     */
    public MfaToken generarToken(int idUsuario, String tipoCanal) {
        String dispositivo = "Web";
        String ipOrigen = "127.0.0.1";

        // Invalidar tokens anteriores activos
        invalidarTokensActivos(idUsuario);

        MfaToken token = new MfaToken(idUsuario, tipoCanal, dispositivo, ipOrigen);

        // Almacenar token
        tokensUsuario.computeIfAbsent(idUsuario, k -> new ArrayList<>()).add(token);

        // Simular envío de token
        System.out.println("Token MFA enviado a usuario " + idUsuario + ": " + token.getToken());

        return token;
    }

    /**
     * Valida un token MFA (método requerido por LoginController)
     */
    public boolean validarToken(int tokenId, String tokenIngresado) {
        // Buscar token por ID en todos los usuarios
        for (List<MfaToken> tokens : tokensUsuario.values()) {
            for (MfaToken token : tokens) {
                if (token.getId() == tokenId) {
                    return token.validarToken(tokenIngresado);
                }
            }
        }
        return false;
    }

    /**
     * Verifica si el usuario requiere MFA
     */
    public boolean requiereMfa(Usuario usuario) {
        // MFA obligatorio para todos los usuarios según BNK-F-012
        return true;
    }

    /**
     * Verifica si el usuario ya completó MFA en esta sesión
     */
    public boolean mfaCompletado(int idUsuario) {
        return mfaCompletadoSesion.getOrDefault(idUsuario, false);
    }

    /**
     * Marca MFA como completado para el usuario
     */
    public void marcarMfaCompletado(int idUsuario) {
        mfaCompletadoSesion.put(idUsuario, true);
    }

    /**
     * Genera un token MFA para el usuario (método alternativo)
     */
    public MfaToken generarTokenMfa(Usuario usuario, String tipoCanal) {
        return generarToken(usuario.getId(), tipoCanal);
    }

    /**
     * Valida un token MFA ingresado por el usuario
     */
    public boolean validarTokenMfa(int idUsuario, String tokenIngresado) {
        List<MfaToken> tokens = tokensUsuario.get(idUsuario);
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }

        // Buscar el token activo más reciente
        MfaToken tokenActivo = tokens.stream()
            .filter(MfaToken::esValido)
            .max((t1, t2) -> t1.getFechaGeneracion().compareTo(t2.getFechaGeneracion()))
            .orElse(null);

        if (tokenActivo == null) {
            return false;
        }

        boolean valido = tokenActivo.validarToken(tokenIngresado);
        if (valido) {
            marcarMfaCompletado(idUsuario);
        }
        return valido;
    }

    /**
     * Invalida todos los tokens activos del usuario
     */
    private void invalidarTokensActivos(int idUsuario) {
        List<MfaToken> tokens = tokensUsuario.get(idUsuario);
        if (tokens != null) {
            tokens.forEach(token -> token.setActivo(false));
        }
    }

    /**
     * Limpia la sesión MFA del usuario
     */
    public void limpiarSesionMfa(int idUsuario) {
        mfaCompletadoSesion.remove(idUsuario);
        invalidarTokensActivos(idUsuario);
    }
}
