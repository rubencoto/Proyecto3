-- Script SQL para crear las tablas del sistema de préstamos y créditos
-- BNK-F-007 – Préstamos y créditos

-- Tabla de préstamos
CREATE TABLE IF NOT EXISTS prestamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numeroPrestamo VARCHAR(50) UNIQUE NOT NULL,
    idCliente INT NOT NULL,
    tipo ENUM('PERSONAL', 'HIPOTECARIO', 'VEHICULAR', 'COMERCIAL') DEFAULT 'PERSONAL',
    montoOriginal DECIMAL(15,2) NOT NULL,
    saldoPendiente DECIMAL(15,2) NOT NULL,
    tasaInteres DECIMAL(5,2) NOT NULL,
    plazoMeses INT NOT NULL,
    cuotaMensual DECIMAL(15,2) NOT NULL,
    estado ENUM('ACTIVO', 'CANCELADO', 'VENCIDO', 'PAGADO') DEFAULT 'ACTIVO',
    fechaDesembolso DATE NOT NULL,
    fechaVencimiento DATE NOT NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (idCliente) REFERENCES clientes(id) ON DELETE CASCADE,
    INDEX idx_cliente (idCliente),
    INDEX idx_estado (estado),
    INDEX idx_fecha_vencimiento (fechaVencimiento)
);

-- Tabla de cuotas (tabla de amortización)
CREATE TABLE IF NOT EXISTS cuotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    idPrestamo INT NOT NULL,
    numeroCuota INT NOT NULL,
    fechaVencimiento DATE NOT NULL,
    montoCapital DECIMAL(15,2) NOT NULL,
    montoInteres DECIMAL(15,2) NOT NULL,
    montoTotal DECIMAL(15,2) GENERATED ALWAYS AS (montoCapital + montoInteres) STORED,
    estado ENUM('PENDIENTE', 'PAGADO', 'VENCIDO') DEFAULT 'PENDIENTE',
    fechaPago DATE NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idPrestamo) REFERENCES prestamos(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cuota_prestamo (idPrestamo, numeroCuota),
    INDEX idx_prestamo (idPrestamo),
    INDEX idx_fecha_vencimiento (fechaVencimiento),
    INDEX idx_estado (estado)
);

-- Tabla de pagos de cuotas (historial de pagos)
CREATE TABLE IF NOT EXISTS pagos_cuotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    idCuota INT NOT NULL,
    idCuenta INT NOT NULL,
    montoPagado DECIMAL(15,2) NOT NULL,
    fechaPago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metodoPago ENUM('DEBITO_AUTOMATICO', 'TRANSFERENCIA', 'EFECTIVO', 'CHEQUE') DEFAULT 'DEBITO_AUTOMATICO',
    referenciaPago VARCHAR(100),
    FOREIGN KEY (idCuota) REFERENCES cuotas(id) ON DELETE CASCADE,
    FOREIGN KEY (idCuenta) REFERENCES cuentas(id) ON DELETE CASCADE,
    INDEX idx_cuota (idCuota),
    INDEX idx_cuenta (idCuenta),
    INDEX idx_fecha_pago (fechaPago)
);

-- Tabla de garantías (para préstamos que requieren garantía)
CREATE TABLE IF NOT EXISTS garantias_prestamo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    idPrestamo INT NOT NULL,
    tipoGarantia ENUM('HIPOTECARIA', 'VEHICULAR', 'FIDUCIARIA', 'PERSONAL') NOT NULL,
    descripcion TEXT,
    valorGarantia DECIMAL(15,2),
    fechaRegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('ACTIVA', 'LIBERADA', 'EJECUTADA') DEFAULT 'ACTIVA',
    FOREIGN KEY (idPrestamo) REFERENCES prestamos(id) ON DELETE CASCADE,
    INDEX idx_prestamo (idPrestamo),
    INDEX idx_tipo (tipoGarantia)
);

-- Vista para reporte de préstamos activos
CREATE OR REPLACE VIEW vista_prestamos_activos AS
SELECT
    p.id,
    p.numeroPrestamo,
    CONCAT(c.nombre, ' ', c.apellidos) as nombreCliente,
    c.cedula,
    p.tipo,
    p.montoOriginal,
    p.saldoPendiente,
    p.tasaInteres,
    p.plazoMeses,
    p.cuotaMensual,
    p.fechaDesembolso,
    p.fechaVencimiento,
    DATEDIFF(p.fechaVencimiento, CURDATE()) as diasVencimiento,
    (SELECT COUNT(*) FROM cuotas WHERE idPrestamo = p.id AND estado = 'VENCIDO') as cuotasVencidas,
    (SELECT COUNT(*) FROM cuotas WHERE idPrestamo = p.id AND estado = 'PENDIENTE') as cuotasPendientes
FROM prestamos p
JOIN clientes c ON p.idCliente = c.id
WHERE p.estado = 'ACTIVO';

-- Vista para tabla de amortización completa
CREATE OR REPLACE VIEW vista_amortizacion AS
SELECT
    c.id,
    c.idPrestamo,
    p.numeroPrestamo,
    CONCAT(cl.nombre, ' ', cl.apellidos) as nombreCliente,
    c.numeroCuota,
    c.fechaVencimiento,
    c.montoCapital,
    c.montoInteres,
    c.montoTotal,
    c.estado,
    c.fechaPago,
    CASE
        WHEN c.estado = 'VENCIDO' THEN DATEDIFF(CURDATE(), c.fechaVencimiento)
        WHEN c.estado = 'PENDIENTE' AND c.fechaVencimiento < CURDATE() THEN DATEDIFF(CURDATE(), c.fechaVencimiento)
        ELSE 0
    END as diasVencimiento
FROM cuotas c
JOIN prestamos p ON c.idPrestamo = p.id
JOIN clientes cl ON p.idCliente = cl.id
ORDER BY c.idPrestamo, c.numeroCuota;

-- Procedimiento para actualizar cuotas vencidas
-- Procedimiento para actualizar cuotas vencidas
DELIMITER //
CREATE PROCEDURE ActualizarCuotasVencidas()
BEGIN
    UPDATE cuotas
    SET estado = 'VENCIDO'
    WHERE estado = 'PENDIENTE'
    AND fechaVencimiento < CURDATE();

    UPDATE prestamos p
    SET estado = 'VENCIDO'
    WHERE estado = 'ACTIVO'
    AND EXISTS (
        SELECT 1 FROM cuotas c
        WHERE c.idPrestamo = p.id
        AND c.estado = 'VENCIDO'
        AND DATEDIFF(CURDATE(), c.fechaVencimiento) > 90
    );
END //
DELIMITER ;

-- Trigger para actualizar saldo pendiente cuando se paga una cuota
-- Trigger para actualizar saldo pendiente cuando se paga una cuota
DELIMITER //
CREATE TRIGGER tr_actualizar_saldo_prestamo
AFTER UPDATE ON cuotas
FOR EACH ROW
BEGIN
    IF OLD.estado = 'PENDIENTE' AND NEW.estado = 'PAGADO' THEN
        UPDATE prestamos
        SET saldoPendiente = saldoPendiente - NEW.montoCapital,
            fechaActualizacion = CURRENT_TIMESTAMP
        WHERE id = NEW.idPrestamo;

        -- Si el saldo pendiente es 0 o negativo, marcar préstamo como pagado
        UPDATE prestamos
        SET estado = 'PAGADO',
            fechaActualizacion = CURRENT_TIMESTAMP
        WHERE id = NEW.idPrestamo
        AND saldoPendiente <= 0;
    END IF;
END //
DELIMITER ;

-- Índices adicionales para optimización
CREATE INDEX idx_prestamos_fecha_desembolso ON prestamos(fechaDesembolso);
CREATE INDEX idx_cuotas_fecha_pago ON cuotas(fechaPago);
CREATE INDEX idx_prestamos_monto ON prestamos(montoOriginal);

-- Datos de ejemplo para tasas de interés por tipo de préstamo
CREATE TABLE IF NOT EXISTS tasas_prestamo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipoPrestamo ENUM('PERSONAL', 'HIPOTECARIO', 'VEHICULAR', 'COMERCIAL') NOT NULL,
    tasaMinima DECIMAL(5,2) NOT NULL,
    tasaMaxima DECIMAL(5,2) NOT NULL,
    plazoMinimoMeses INT NOT NULL,
    plazoMaximoMeses INT NOT NULL,
    montoMinimo DECIMAL(15,2) NOT NULL,
    montoMaximo DECIMAL(15,2) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar tasas por defecto
INSERT INTO tasas_prestamo (tipoPrestamo, tasaMinima, tasaMaxima, plazoMinimoMeses, plazoMaximoMeses, montoMinimo, montoMaximo) VALUES
('PERSONAL', 12.00, 24.00, 6, 72, 100000, 10000000),
('HIPOTECARIO', 8.00, 15.00, 60, 360, 5000000, 200000000),
('VEHICULAR', 10.00, 18.00, 12, 84, 1000000, 50000000),
('COMERCIAL', 9.00, 20.00, 12, 120, 500000, 100000000)
ON DUPLICATE KEY UPDATE fechaCreacion = fechaCreacion;
