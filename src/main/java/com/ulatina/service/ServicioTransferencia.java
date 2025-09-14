package com.ulatina.service;

import com.ulatina.data.TransferenciaSwift;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class ServicioTransferencia {

    // Simulación de almacenamiento en memoria
    private Map<String, TransferenciaSwift> transferenciasSwift = new HashMap<>();
    private int nextId = 1;

    public TransferenciaSwift procesarSwift(String cuentaOrigen, String bancoBeneficiario,
                                          String swiftCode, String cuentaBeneficiario,
                                          String nombreBeneficiario, BigDecimal monto, String concepto) {
        TransferenciaSwift transferencia = new TransferenciaSwift();
        transferencia.setId(nextId++);
        transferencia.setNumeroCuentaOrigen(cuentaOrigen);
        transferencia.setCodigoSwiftDestino(swiftCode);
        transferencia.setNumeroCuentaDestino(cuentaBeneficiario);
        transferencia.setBeneficiario(nombreBeneficiario);
        transferencia.setMonto(monto);
        transferencia.setConcepto(concepto);
        transferencia.setReferenciaSwift(generarReferenciaSwift());
        transferencia.setFechaCreacion(LocalDateTime.now());
        transferencia.setEstado("EN_PROCESO");

        transferenciasSwift.put(transferencia.getReferenciaSwift(), transferencia);

        return transferencia;
    }

    public List<TransferenciaSwift> obtenerHistorialSwift(int idUsuario) {
        return new ArrayList<>(transferenciasSwift.values());
    }

    public TransferenciaSwift obtenerPorReferencia(String referencia) {
        return transferenciasSwift.get(referencia);
    }

    private String generarReferenciaSwift() {
        return "SWIFT" + System.currentTimeMillis() % 1000000;
    }
}
