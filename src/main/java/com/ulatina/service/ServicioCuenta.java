package com.ulatina.service;

import com.ulatina.data.Cuenta;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ServicioCuenta {

    // Simulación de almacenamiento en memoria (en producción usar base de datos)
    private final Map<String, Cuenta> cuentas = new HashMap<>();
    private int nextId = 1;

    public ServicioCuenta() {
        // Inicializar con datos de prueba
        inicializarDatosPrueba();
    }

    private void inicializarDatosPrueba() {
        // Cuenta corriente CRC
        Cuenta cuenta1 = new Cuenta();
        cuenta1.setId(nextId++);
        cuenta1.setNumero("CR05015108410026012345");
        cuenta1.setTipo("CORRIENTE");
        cuenta1.setMoneda("CRC");
        cuenta1.setSaldo(new BigDecimal("1250750.00"));
        cuenta1.setEstado("ACTIVA");
        cuenta1.setIdCliente(1);
        cuenta1.setFechaApertura(LocalDateTime.now().minusMonths(6));
        cuentas.put(cuenta1.getNumero(), cuenta1);

        // Cuenta ahorros CRC
        Cuenta cuenta2 = new Cuenta();
        cuenta2.setId(nextId++);
        cuenta2.setNumero("CR05015108410026067890");
        cuenta2.setTipo("AHORROS");
        cuenta2.setMoneda("CRC");
        cuenta2.setSaldo(new BigDecimal("850300.50"));
        cuenta2.setEstado("ACTIVA");
        cuenta2.setIdCliente(1);
        cuenta2.setFechaApertura(LocalDateTime.now().minusMonths(8));
        cuentas.put(cuenta2.getNumero(), cuenta2);

        // Cuenta USD
        Cuenta cuenta3 = new Cuenta();
        cuenta3.setId(nextId++);
        cuenta3.setNumero("CR05015108410026098765");
        cuenta3.setTipo("USD");
        cuenta3.setMoneda("USD");
        cuenta3.setSaldo(new BigDecimal("2150.75"));
        cuenta3.setEstado("ACTIVA");
        cuenta3.setIdCliente(2);
        cuenta3.setFechaApertura(LocalDateTime.now().minusMonths(3));
        cuentas.put(cuenta3.getNumero(), cuenta3);

        // Cuenta con saldo negativo (para pruebas)
        Cuenta cuenta4 = new Cuenta();
        cuenta4.setId(nextId++);
        cuenta4.setNumero("CR05015108410026123456");
        cuenta4.setTipo("CORRIENTE");
        cuenta4.setMoneda("CRC");
        cuenta4.setSaldo(new BigDecimal("-15250.00"));
        cuenta4.setEstado("BLOQUEADA");
        cuenta4.setIdCliente(3);
        cuenta4.setFechaApertura(LocalDateTime.now().minusMonths(4));
        cuentas.put(cuenta4.getNumero(), cuenta4);
    }

    public Cuenta crear(Cuenta cuenta) {
        cuenta.setId(nextId++);
        if (cuenta.getNumero() == null) {
            cuenta.setNumero(generarNumeroCuenta(cuenta.getMoneda()));
        }
        if (cuenta.getFechaApertura() == null) {
            cuenta.setFechaApertura(LocalDateTime.now());
        }
        if (cuenta.getEstado() == null) {
            cuenta.setEstado("ACTIVA");
        }
        if (cuenta.getSaldo() == null) {
            cuenta.setSaldo(BigDecimal.ZERO);
        }

        cuentas.put(cuenta.getNumero(), cuenta);
        return cuenta;
    }

    public Cuenta obtenerPorNumero(String numeroCuenta) {
        return cuentas.get(numeroCuenta);
    }

    public Cuenta obtenerPorId(int id) {
        return cuentas.values().stream()
            .filter(c -> c.getId() == id)
            .findFirst()
            .orElse(null);
    }

    public void actualizarSaldo(String numeroCuenta, BigDecimal nuevoSaldo) {
        Cuenta cuenta = cuentas.get(numeroCuenta);
        if (cuenta != null) {
            cuenta.setSaldo(nuevoSaldo);
        }
    }

    public List<Cuenta> obtenerPorCliente(int idCliente) {
        return cuentas.values().stream()
            .filter(c -> c.getIdCliente() == idCliente)
            .collect(Collectors.toList());
    }

    public List<Cuenta> listarTodas() {
        return new ArrayList<>(cuentas.values());
    }

    public void cambiarEstado(String numeroCuenta, String nuevoEstado) {
        Cuenta cuenta = cuentas.get(numeroCuenta);
        if (cuenta != null) {
            cuenta.setEstado(nuevoEstado);
        }
    }

    public List<Cuenta> listarActivas() {
        return cuentas.values().stream()
            .filter(c -> "ACTIVA".equals(c.getEstado()))
            .collect(Collectors.toList());
    }

    public void actualizar(Cuenta cuenta) {
        if (cuentas.containsKey(cuenta.getNumero())) {
            cuentas.put(cuenta.getNumero(), cuenta);
        }
    }

    private String generarNumeroCuenta(String tipoMoneda) {
        return "CR05015108410026" + String.format("%06d", nextId);
    }

    public boolean transferir(String cuentaOrigen, String cuentaDestino, BigDecimal monto) {
        Cuenta origen = cuentas.get(cuentaOrigen);
        Cuenta destino = cuentas.get(cuentaDestino);

        if (origen == null || destino == null) {
            return false;
        }

        if (origen.getSaldo().compareTo(monto) < 0) {
            return false; // Saldo insuficiente
        }

        // Realizar transferencia
        origen.setSaldo(origen.getSaldo().subtract(monto));
        destino.setSaldo(destino.getSaldo().add(monto));

        return true;
    }

    public void bloquear(String numeroCuenta) {
        Cuenta cuenta = cuentas.get(numeroCuenta);
        if (cuenta != null) {
            cuenta.setEstado("BLOQUEADA");
        }
    }

    /**
     * Obtiene una cuenta por su número (método requerido por ServicioSwift)
     */
    public Cuenta obtenerCuenta(String numeroCuenta) {
        return cuentas.get(numeroCuenta);
    }

    /**
     * Actualiza una cuenta existente (método requerido por ServicioSwift)
     */
    public void actualizarCuenta(Cuenta cuenta) {
        if (cuenta != null && cuenta.getNumero() != null) {
            cuentas.put(cuenta.getNumero(), cuenta);
        }
    }
}
