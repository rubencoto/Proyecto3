package com.ulatina.service;

import com.ulatina.data.Tarjeta;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de tarjetas
 */
@ApplicationScoped
public class ServicioTarjeta {

    // Simulación de base de datos en memoria
    private Map<Integer, Tarjeta> tarjetas = new HashMap<>();
    private int nextId = 1;

    public ServicioTarjeta() {
        // Inicializar tarjetas de prueba
        Tarjeta tarjeta1 = new Tarjeta(1, "4532123456789012", "DEBITO", 1, 2);
        tarjeta1.activar();
        tarjetas.put(1, tarjeta1);

        Tarjeta tarjeta2 = new Tarjeta(2, "5555444433332222", "CREDITO", 2, 2);
        tarjeta2.activar();
        tarjetas.put(2, tarjeta2);

        nextId = 3;
    }

    /**
     * Crear nueva tarjeta
     */
    public void crear(Tarjeta tarjeta) throws Exception {
        if (tarjeta == null) {
            throw new Exception("La tarjeta no puede ser nula");
        }

        tarjeta.setId(nextId++);

        // Generar número de tarjeta único
        String numeroTarjeta = generarNumeroTarjeta(tarjeta.getTipo());
        tarjeta.setNumeroTarjeta(numeroTarjeta);

        tarjetas.put(tarjeta.getId(), tarjeta);
    }

    /**
     * Actualizar tarjeta existente
     */
    public void actualizar(Tarjeta tarjeta) throws Exception {
        if (tarjeta == null || tarjeta.getId() == 0) {
            throw new Exception("ID de tarjeta inválido");
        }

        if (!tarjetas.containsKey(tarjeta.getId())) {
            throw new Exception("Tarjeta no encontrada");
        }

        tarjetas.put(tarjeta.getId(), tarjeta);
    }

    /**
     * Obtener tarjeta por ID
     */
    public Tarjeta obtenerPorId(int id) {
        return tarjetas.get(id);
    }

    /**
     * Listar tarjetas por cliente
     */
    public List<Tarjeta> listarPorCliente(String numeroCuenta) {
        // En una implementación real, filtrar por ID de cliente
        // Por ahora retornar todas las tarjetas
        return new ArrayList<>(tarjetas.values());
    }

    /**
     * Listar tarjetas por cliente ID
     */
    public List<Tarjeta> listarPorCliente(int idCliente) {
        return tarjetas.values().stream()
            .filter(t -> t.getIdCliente() == idCliente)
            .collect(Collectors.toList());
    }

    /**
     * Bloquear tarjeta
     */
    public boolean bloquear(int idTarjeta, String motivo) throws Exception {
        Tarjeta tarjeta = obtenerPorId(idTarjeta);
        if (tarjeta == null) {
            throw new Exception("Tarjeta no encontrada");
        }

        boolean bloqueada = tarjeta.bloquear(motivo);
        if (bloqueada) {
            actualizar(tarjeta);
        }

        return bloqueada;
    }

    /**
     * Desbloquear tarjeta
     */
    public boolean desbloquear(int idTarjeta) throws Exception {
        Tarjeta tarjeta = obtenerPorId(idTarjeta);
        if (tarjeta == null) {
            throw new Exception("Tarjeta no encontrada");
        }

        boolean desbloqueada = tarjeta.desbloquear();
        if (desbloqueada) {
            actualizar(tarjeta);
        }

        return desbloqueada;
    }

    /**
     * Activar tarjeta
     */
    public boolean activar(int idTarjeta) throws Exception {
        Tarjeta tarjeta = obtenerPorId(idTarjeta);
        if (tarjeta == null) {
            throw new Exception("Tarjeta no encontrada");
        }

        boolean activada = tarjeta.activar();
        if (activada) {
            actualizar(tarjeta);
        }

        return activada;
    }

    /**
     * Listar todas las tarjetas
     */
    public List<Tarjeta> listarTodas() {
        return new ArrayList<>(tarjetas.values());
    }

    /**
     * Obtener tarjetas que vencen pronto
     */
    public List<Tarjeta> obtenerTarjetasProximasVencer(int diasAntes) {
        LocalDate fechaLimite = LocalDate.now().plusDays(diasAntes);

        return tarjetas.values().stream()
            .filter(t -> t.getFechaVencimiento().isBefore(fechaLimite))
            .collect(Collectors.toList());
    }

    /**
     * Generar número de tarjeta único
     */
    private String generarNumeroTarjeta(String tipo) {
        String prefijo;
        switch (tipo) {
            case "DEBITO":
                prefijo = "4532"; // Visa Débito
                break;
            case "CREDITO":
                prefijo = "5555"; // Mastercard Crédito
                break;
            default:
                prefijo = "4000";
        }

        // Generar 12 dígitos adicionales
        StringBuilder numero = new StringBuilder(prefijo);
        for (int i = 0; i < 12; i++) {
            numero.append((int) (Math.random() * 10));
        }

        return numero.toString();
    }
}
