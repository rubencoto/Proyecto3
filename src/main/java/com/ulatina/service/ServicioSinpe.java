package com.ulatina.service;

import com.ulatina.data.TransferenciaSinpe;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class ServicioSinpe {

    // Simulación de almacenamiento en memoria
    private Map<String, TransferenciaSinpe> transferencias = new HashMap<>();
    private int nextId = 1;

    public TransferenciaSinpe procesarTransferencia(String cuentaOrigen, String cuentaDestino,
                                                  BigDecimal monto, String descripcion) {
        TransferenciaSinpe transferencia = new TransferenciaSinpe();
        transferencia.setId(nextId++);
        transferencia.setCuentaOrigen(cuentaOrigen);
        transferencia.setCuentaDestino(cuentaDestino);
        transferencia.setMonto(monto);
        transferencia.setDescripcion(descripcion);
        transferencia.setReferencia(generarReferencia());
        transferencia.setFecha(LocalDateTime.now());
        transferencia.setEstado("PROCESADA");

        transferencias.put(transferencia.getReferencia(), transferencia);

        return transferencia;
    }

    public List<TransferenciaSinpe> obtenerHistorial(int idUsuario) {
        return new ArrayList<>(transferencias.values());
    }

    private String generarReferencia() {
        return "SINPE" + System.currentTimeMillis() % 1000000;
    }
}
