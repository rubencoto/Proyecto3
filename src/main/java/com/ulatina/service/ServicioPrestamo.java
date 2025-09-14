package com.ulatina.service;

import com.ulatina.data.Prestamo;
import com.ulatina.data.Cuota;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class ServicioPrestamo {

    // Simulación de almacenamiento en memoria
    private Map<Integer, Prestamo> prestamos = new HashMap<>();
    private Map<Integer, List<Cuota>> cuotasPorPrestamo = new HashMap<>();
    private int nextId = 1;

    public ServicioPrestamo() {
        inicializarDatosPrueba();
    }

    private void inicializarDatosPrueba() {
        // Préstamo hipotecario
        Prestamo prestamo1 = new Prestamo();
        prestamo1.setId(nextId++);
        prestamo1.setIdCliente(1);
        prestamo1.setTipo("HIPOTECARIO");
        prestamo1.setMonto(new BigDecimal("45250000"));
        prestamo1.setSaldoPendiente(new BigDecimal("38750500"));
        prestamo1.setTasaAnual(new BigDecimal("8.5"));
        prestamo1.setPlazoMeses(240); // 20 años
        prestamo1.setCuotaMensual(new BigDecimal("285750"));
        prestamo1.setEstado("ACTIVO");
        prestamo1.setFechaInicio(LocalDate.now().minusYears(2));
        prestamo1.setNumeroPrestamo("HIP-2023-001234");
        prestamos.put(prestamo1.getId(), prestamo1);

        // Préstamo personal
        Prestamo prestamo2 = new Prestamo();
        prestamo2.setId(nextId++);
        prestamo2.setIdCliente(1);
        prestamo2.setTipo("PERSONAL");
        prestamo2.setMonto(new BigDecimal("2500000"));
        prestamo2.setSaldoPendiente(new BigDecimal("1850750"));
        prestamo2.setTasaAnual(new BigDecimal("12.5"));
        prestamo2.setPlazoMeses(36); // 3 años
        prestamo2.setCuotaMensual(new BigDecimal("89500"));
        prestamo2.setEstado("ACTIVO");
        prestamo2.setFechaInicio(LocalDate.now().minusMonths(6));
        prestamo2.setNumeroPrestamo("PER-2024-005678");
        prestamos.put(prestamo2.getId(), prestamo2);

        // Generar cuotas para los préstamos
        generarCuotasPrueba();
    }

    private void generarCuotasPrueba() {
        for (Prestamo prestamo : prestamos.values()) {
            List<Cuota> cuotas = new ArrayList<>();

            for (int i = 1; i <= Math.min(12, prestamo.getPlazoMeses()); i++) {
                Cuota cuota = new Cuota();
                cuota.setId(i);
                cuota.setIdPrestamo(prestamo.getId());
                cuota.setNumero(i);
                cuota.setVence(prestamo.getFechaInicio().plusMonths(i));

                // Calcular interés y capital
                BigDecimal tasaMensual = prestamo.getTasaAnual().divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);
                BigDecimal interes = prestamo.getSaldoPendiente().multiply(tasaMensual).setScale(2, RoundingMode.HALF_UP);
                BigDecimal capital = prestamo.getCuotaMensual().subtract(interes);

                cuota.setInteres(interes);
                cuota.setPrincipal(capital);
                cuota.setPagado(i <= 6); // Primeras 6 cuotas pagadas

                cuotas.add(cuota);
            }

            cuotasPorPrestamo.put(prestamo.getId(), cuotas);
        }
    }

    public List<Prestamo> listarPorCliente(int idCliente) {
        return prestamos.values().stream()
            .filter(p -> p.getIdCliente() == idCliente)
            .sorted((p1, p2) -> p2.getFechaInicio().compareTo(p1.getFechaInicio()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<Prestamo> listarTodos() {
        return new ArrayList<>(prestamos.values());
    }

    public Prestamo obtenerPorId(int id) {
        return prestamos.get(id);
    }

    public Prestamo obtenerPorNumero(String numeroPrestamo) {
        return prestamos.values().stream()
            .filter(p -> numeroPrestamo.equals(p.getNumeroPrestamo()))
            .findFirst()
            .orElse(null);
    }

    public Prestamo crear(Prestamo prestamo) {
        prestamo.setId(nextId++);
        prestamo.setNumeroPrestamo(generarNumeroPrestamo(prestamo.getTipo()));
        prestamo.setFechaInicio(LocalDate.now());
        prestamo.setSaldoPendiente(prestamo.getMonto());
        prestamo.setEstado("APROBADO");

        // Calcular cuota mensual
        BigDecimal cuotaMensual = calcularCuotaMensual(
            prestamo.getMonto(),
            prestamo.getTasaAnual(),
            prestamo.getPlazoMeses()
        );
        prestamo.setCuotaMensual(cuotaMensual);

        prestamos.put(prestamo.getId(), prestamo);
        generarTablaCuotas(prestamo);

        return prestamo;
    }

    public void aprobar(int idPrestamo) {
        Prestamo prestamo = prestamos.get(idPrestamo);
        if (prestamo != null) {
            prestamo.setEstado("APROBADO");
        }
    }

    public void desembolsar(int idPrestamo) {
        Prestamo prestamo = prestamos.get(idPrestamo);
        if (prestamo != null && "APROBADO".equals(prestamo.getEstado())) {
            prestamo.setEstado("ACTIVO");
            prestamo.setFechaDesembolso(LocalDate.now());
        }
    }

    public void pagarCuota(int idCuota) {
        for (List<Cuota> cuotas : cuotasPorPrestamo.values()) {
            for (Cuota cuota : cuotas) {
                if (cuota.getId() == idCuota && !cuota.isPagado()) {
                    cuota.setPagado(true);
                    cuota.setFechaPago(LocalDate.now());

                    // Actualizar saldo del préstamo
                    Prestamo prestamo = prestamos.get(cuota.getIdPrestamo());
                    if (prestamo != null) {
                        BigDecimal nuevoSaldo = prestamo.getSaldoPendiente().subtract(cuota.getPrincipal());
                        prestamo.setSaldoPendiente(nuevoSaldo);

                        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
                            prestamo.setEstado("CANCELADO");
                        }
                    }
                    break;
                }
            }
        }
    }

    public List<Cuota> obtenerCuotas(int idPrestamo) {
        return cuotasPorPrestamo.getOrDefault(idPrestamo, new ArrayList<>());
    }

    public List<Cuota> obtenerCuotasPendientes(int idPrestamo) {
        return obtenerCuotas(idPrestamo).stream()
            .filter(c -> !c.isPagado())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private BigDecimal calcularCuotaMensual(BigDecimal monto, BigDecimal tasaAnual, int plazoMeses) {
        if (tasaAnual.compareTo(BigDecimal.ZERO) == 0) {
            return monto.divide(BigDecimal.valueOf(plazoMeses), 2, RoundingMode.HALF_UP);
        }

        BigDecimal tasaMensual = tasaAnual.divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(tasaMensual);
        BigDecimal potencia = factor.pow(plazoMeses);

        BigDecimal numerador = monto.multiply(tasaMensual).multiply(potencia);
        BigDecimal denominador = potencia.subtract(BigDecimal.ONE);

        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private void generarTablaCuotas(Prestamo prestamo) {
        List<Cuota> cuotas = new ArrayList<>();
        BigDecimal saldoPendiente = prestamo.getMonto();
        BigDecimal tasaMensual = prestamo.getTasaAnual().divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP);

        for (int i = 1; i <= prestamo.getPlazoMeses(); i++) {
            Cuota cuota = new Cuota();
            cuota.setId(i);
            cuota.setIdPrestamo(prestamo.getId());
            cuota.setNumero(i);
            cuota.setVence(prestamo.getFechaInicio().plusMonths(i));

            BigDecimal interes = saldoPendiente.multiply(tasaMensual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal capital = prestamo.getCuotaMensual().subtract(interes);

            if (i == prestamo.getPlazoMeses()) {
                capital = saldoPendiente; // Ajuste en última cuota
            }

            cuota.setInteres(interes);
            cuota.setPrincipal(capital);
            cuota.setPagado(false);

            cuotas.add(cuota);
            saldoPendiente = saldoPendiente.subtract(capital);
        }

        cuotasPorPrestamo.put(prestamo.getId(), cuotas);
    }

    private String generarNumeroPrestamo(String tipo) {
        String prefijo = tipo != null ? tipo.substring(0, 3).toUpperCase() : "PRE";
        return prefijo + "-" + System.currentTimeMillis() % 1000000;
    }

    public BigDecimal calcularTotalPrestamos() {
        return prestamos.values().stream()
            .filter(p -> "ACTIVO".equals(p.getEstado()))
            .map(Prestamo::getSaldoPendiente)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Prestamo> buscarPorEstado(String estado) {
        return prestamos.values().stream()
            .filter(p -> estado.equals(p.getEstado()))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
