package com.ulatina.service;

import com.ulatina.data.TransferenciaSwift;
import com.ulatina.data.Cuenta;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Random;

@ApplicationScoped
public class ServicioSwift {

    private static final String SWIFT_CODE_ORIGEN = "BNCRCRSJ"; // Código SWIFT del banco origen
    private static final Pattern SWIFT_CODE_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    private static final BigDecimal COMISION_BASE = new BigDecimal("25.00");
    private static final BigDecimal COMISION_PORCENTAJE = new BigDecimal("0.001"); // 0.1%

    private ServicioCuenta servicioCuenta = new ServicioCuenta();
    private List<TransferenciaSwift> transferenciasSwift = new ArrayList<>();

    /**
     * Procesa una transferencia internacional SWIFT MT103
     */
    public String procesarTransferenciaSwift(TransferenciaSwift transferencia) throws Exception {
        // Validar transferencia
        validarTransferenciaSwift(transferencia);

        // Validar cuenta origen
        Cuenta cuentaOrigen = servicioCuenta.obtenerCuenta(transferencia.getNumeroCuentaOrigen());
        if (cuentaOrigen == null) {
            throw new Exception("Cuenta origen no encontrada");
        }

        // Calcular comisión
        BigDecimal comision = calcularComision(transferencia.getMonto());
        transferencia.setComision(comision);

        // Validar saldo suficiente
        BigDecimal montoTotal = transferencia.getMonto().add(comision);
        if (cuentaOrigen.getSaldo().compareTo(montoTotal) < 0) {
            throw new Exception("Saldo insuficiente para la transferencia y comisión");
        }

        // Generar referencia SWIFT
        String referenciaSwift = generarReferenciaSwift();
        transferencia.setReferenciaSwift(referenciaSwift);

        // Generar mensaje MT103
        String mensajeMT103 = generarMensajeMT103(transferencia);
        transferencia.setMensajeMT103(mensajeMT103);

        // Establecer código SWIFT origen
        transferencia.setCodigoSwiftOrigen(SWIFT_CODE_ORIGEN);

        // Actualizar estado y fecha
        transferencia.setEstado("ENVIADO");
        transferencia.setFechaEnvio(LocalDateTime.now());

        // Debitar cuenta origen
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(montoTotal));
        servicioCuenta.actualizarCuenta(cuentaOrigen);

        // Simular envío a red SWIFT
        boolean exito = enviarARedSwift(transferencia);

        if (exito) {
            transferencia.setEstado("CONFIRMADO");
            transferencia.setFechaConfirmacion(LocalDateTime.now());
        } else {
            transferencia.setEstado("RECHAZADO");
            transferencia.setCodigoError("MT103_ERR");
            transferencia.setMensajeError("Error en procesamiento SWIFT");

            // Reversar débito en caso de error
            cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().add(montoTotal));
            servicioCuenta.actualizarCuenta(cuentaOrigen);
        }

        // Guardar transferencia
        transferencia.setId(transferenciasSwift.size() + 1);
        transferenciasSwift.add(transferencia);

        return referenciaSwift;
    }

    /**
     * Valida los datos de una transferencia SWIFT
     */
    private void validarTransferenciaSwift(TransferenciaSwift transferencia) throws Exception {
        // Validar código SWIFT destino
        if (!validarCodigoSwift(transferencia.getCodigoSwiftDestino())) {
            throw new Exception("Código SWIFT destino inválido");
        }

        // Validar número de cuenta destino (IBAN o formato local)
        if (!validarNumeroCuentaInternacional(transferencia.getNumeroCuentaDestino())) {
            throw new Exception("Número de cuenta destino inválido");
        }

        // Validar beneficiario
        if (transferencia.getBeneficiario() == null || transferencia.getBeneficiario().trim().length() < 3) {
            throw new Exception("Nombre del beneficiario debe tener al menos 3 caracteres");
        }

        // Validar monto
        if (transferencia.getMonto() == null || transferencia.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Monto debe ser mayor a cero");
        }

        // Validar límites
        if (transferencia.getMonto().compareTo(new BigDecimal("50000")) > 0) {
            throw new Exception("Monto excede límite máximo de $50,000 USD");
        }

        // Validar moneda
        if (!"USD".equals(transferencia.getMoneda()) && !"EUR".equals(transferencia.getMoneda())) {
            throw new Exception("Moneda no soportada para transferencias internacionales");
        }

        // Validar país beneficiario
        if (transferencia.getPaisBeneficiario() == null || transferencia.getPaisBeneficiario().length() != 2) {
            throw new Exception("Código de país beneficiario inválido");
        }
    }

    /**
     * Valida formato de código SWIFT
     */
    public boolean validarCodigoSwift(String codigoSwift) {
        if (codigoSwift == null) return false;
        return SWIFT_CODE_PATTERN.matcher(codigoSwift.toUpperCase()).matches();
    }

    /**
     * Valida número de cuenta internacional (IBAN o formato estándar)
     */
    private boolean validarNumeroCuentaInternacional(String numeroCuenta) {
        if (numeroCuenta == null) return false;

        // Verificar si es IBAN (longitud entre 15 y 34 caracteres)
        if (numeroCuenta.length() >= 15 && numeroCuenta.length() <= 34) {
            // Verificar que los primeros dos caracteres sean letras (código de país)
            String paisCodigo = numeroCuenta.substring(0, 2);
            if (paisCodigo.chars().allMatch(Character::isLetter)) {
                // Verificar que el tercer carácter sea un dígito (número de control)
                char digitoControl = numeroCuenta.charAt(2);
                if (Character.isDigit(digitoControl)) {
                    return true;
                }
            }
        }

        // Verificar formato estándar (8-34 caracteres alfanuméricos)
        return numeroCuenta.matches("^[A-Z0-9]{8,34}$");
    }

    /**
     * Calcula la comisión de la transferencia
     */
    private BigDecimal calcularComision(BigDecimal monto) {
        BigDecimal comisionPorcentual = monto.multiply(COMISION_PORCENTAJE);
        return COMISION_BASE.add(comisionPorcentual);
    }

    /**
     * Genera referencia única SWIFT
     */
    private String generarReferenciaSwift() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        Random random = new Random();
        int randomNum = random.nextInt(9999);
        return String.format("SW%s%04d", timestamp, randomNum);
    }

    /**
     * Genera mensaje MT103 estándar
     */
    private String generarMensajeMT103(TransferenciaSwift transferencia) {
        StringBuilder mt103 = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));

        mt103.append("{1:F01").append(SWIFT_CODE_ORIGEN).append("0000000000}\n");
        mt103.append("{2:I103").append(transferencia.getCodigoSwiftDestino()).append("N}\n");
        mt103.append("{3:{108:").append(transferencia.getReferenciaSwift()).append("}}\n");
        mt103.append("{4:\n");
        mt103.append(":20:").append(transferencia.getReferenciaSwift()).append("\n");
        mt103.append(":23B:CRED\n");
        mt103.append(":32A:").append(timestamp).append(transferencia.getMoneda())
               .append(String.format("%.2f", transferencia.getMonto())).append("\n");
        mt103.append(":50K:/").append(transferencia.getNumeroCuentaOrigen()).append("\n");
        mt103.append("BANCO NACIONAL DE COSTA RICA\n");
        mt103.append("SAN JOSE, COSTA RICA\n");
        mt103.append(":57A:").append(transferencia.getCodigoSwiftDestino()).append("\n");
        mt103.append(":59:/").append(transferencia.getNumeroCuentaDestino()).append("\n");
        mt103.append(transferencia.getBeneficiario()).append("\n");
        if (transferencia.getDireccionBeneficiario() != null) {
            mt103.append(transferencia.getDireccionBeneficiario()).append("\n");
        }
        mt103.append(":70:").append(transferencia.getConcepto() != null ?
                                   transferencia.getConcepto() : "INTERNATIONAL TRANSFER").append("\n");
        mt103.append("-}");

        return mt103.toString();
    }

    /**
     * Simula envío a red SWIFT
     */
    private boolean enviarARedSwift(TransferenciaSwift transferencia) {
        try {
            // Simular latencia de red
            Thread.sleep(2000);

            // Simular 95% de éxito
            Random random = new Random();
            return random.nextInt(100) < 95;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Consulta transferencias SWIFT por cuenta
     */
    public List<TransferenciaSwift> consultarTransferenciasSwift(String numeroCuenta) {
        List<TransferenciaSwift> resultado = new ArrayList<>();
        for (TransferenciaSwift t : transferenciasSwift) {
            if (t.getNumeroCuentaOrigen().equals(numeroCuenta)) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    /**
     * Obtiene transferencia por referencia
     */
    public TransferenciaSwift obtenerTransferenciaPorReferencia(String referencia) {
        return transferenciasSwift.stream()
            .filter(t -> t.getReferenciaSwift().equals(referencia))
            .findFirst()
            .orElse(null);
    }

    /**
     * Obtiene todas las transferencias SWIFT
     */
    public List<TransferenciaSwift> obtenerTodasTransferenciasSwift() {
        return new ArrayList<>(transferenciasSwift);
    }
}
