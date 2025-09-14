-- Schema para módulo de contabilidad automática
-- BNK-F-003 - Contabilidad y asientos automáticos

-- Tabla de cuentas contables (catálogo contable)
CREATE TABLE IF NOT EXISTS cuentas_contables (
    id INT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('ACTIVO', 'PASIVO', 'PATRIMONIO', 'INGRESO', 'GASTO') NOT NULL,
    naturaleza ENUM('DEUDORA', 'ACREEDORA') NOT NULL,
    nivel INT NOT NULL DEFAULT 1,
    cuentaPadre VARCHAR(20),
    saldo DECIMAL(15,2) DEFAULT 0.00,
    activa BOOLEAN DEFAULT TRUE,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_codigo (codigo),
    INDEX idx_tipo (tipo)
);

-- Tabla de asientos contables (cabecera)
CREATE TABLE IF NOT EXISTS asientos_contables (
    id INT PRIMARY KEY AUTO_INCREMENT,
    numeroAsiento VARCHAR(50) UNIQUE NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    concepto TEXT,
    tipoTransaccion VARCHAR(50) NOT NULL,
    referenciaTransaccion VARCHAR(100),
    totalDebito DECIMAL(15,2) DEFAULT 0.00,
    totalCredito DECIMAL(15,2) DEFAULT 0.00,
    balanceado BOOLEAN DEFAULT FALSE,
    usuarioCreacion VARCHAR(50),
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_numero (numeroAsiento),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipoTransaccion),
    INDEX idx_referencia (referenciaTransaccion)
);

-- Tabla de detalles de asientos contables
CREATE TABLE IF NOT EXISTS detalles_asientos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    idAsiento INT NOT NULL,
    idCuentaContable INT NOT NULL,
    debito DECIMAL(15,2) DEFAULT 0.00,
    credito DECIMAL(15,2) DEFAULT 0.00,
    concepto VARCHAR(255),
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (idAsiento) REFERENCES asientos_contables(id) ON DELETE CASCADE,
    FOREIGN KEY (idCuentaContable) REFERENCES cuentas_contables(id),
    INDEX idx_asiento (idAsiento),
    INDEX idx_cuenta (idCuentaContable)
);

-- Insertar catálogo básico de cuentas contables
INSERT INTO cuentas_contables (codigo, nombre, tipo, naturaleza, nivel) VALUES
-- ACTIVOS
('1.1.1.001', 'Efectivo en Caja CRC', 'ACTIVO', 'DEUDORA', 4),
('1.1.1.002', 'Efectivo en Caja USD', 'ACTIVO', 'DEUDORA', 4),
('1.1.2.001', 'Cuentas Corrientes CRC', 'ACTIVO', 'DEUDORA', 4),
('1.1.2.002', 'Cuentas Corrientes USD', 'ACTIVO', 'DEUDORA', 4),
('1.1.3.001', 'Cuentas de Ahorro CRC', 'ACTIVO', 'DEUDORA', 4),
('1.1.3.002', 'Cuentas de Ahorro USD', 'ACTIVO', 'DEUDORA', 4),
('1.3.1.001', 'Cuentas por Cobrar SINPE', 'ACTIVO', 'DEUDORA', 4),
('1.3.1.002', 'Cuentas por Cobrar SWIFT', 'ACTIVO', 'DEUDORA', 4),
('1.4.1.001', 'Préstamos por Cobrar CRC', 'ACTIVO', 'DEUDORA', 4),
('1.4.1.002', 'Préstamos por Cobrar USD', 'ACTIVO', 'DEUDORA', 4),

-- PASIVOS
('2.1.1.001', 'Depósitos a la Vista CRC', 'PASIVO', 'ACREEDORA', 4),
('2.1.1.002', 'Depósitos a la Vista USD', 'PASIVO', 'ACREEDORA', 4),
('2.1.2.001', 'Depósitos de Ahorro CRC', 'PASIVO', 'ACREEDORA', 4),
('2.1.2.002', 'Depósitos de Ahorro USD', 'PASIVO', 'ACREEDORA', 4),

-- PATRIMONIO
('3.1.1.001', 'Capital Social', 'PATRIMONIO', 'ACREEDORA', 4),
('3.2.1.001', 'Utilidades Retenidas', 'PATRIMONIO', 'ACREEDORA', 4),

-- INGRESOS
('4.1.1.001', 'Ingresos por Intereses CRC', 'INGRESO', 'ACREEDORA', 4),
('4.1.1.002', 'Ingresos por Intereses USD', 'INGRESO', 'ACREEDORA', 4),
('4.2.1.001', 'Ingresos por Comisiones CRC', 'INGRESO', 'ACREEDORA', 4),
('4.2.1.002', 'Ingresos por Comisiones USD', 'INGRESO', 'ACREEDORA', 4),
('4.3.1.001', 'Ingresos por Servicios', 'INGRESO', 'ACREEDORA', 4),

-- GASTOS
('5.1.1.001', 'Gastos por Intereses CRC', 'GASTO', 'DEUDORA', 4),
('5.1.1.002', 'Gastos por Intereses USD', 'GASTO', 'DEUDORA', 4),
('5.2.1.001', 'Gastos Administrativos', 'GASTO', 'DEUDORA', 4),
('5.3.1.001', 'Gastos Operativos', 'GASTO', 'DEUDORA', 4);

-- Crear vista para balance de comprobación
CREATE OR REPLACE VIEW balance_comprobacion AS
SELECT
    cc.codigo,
    cc.nombre,
    cc.tipo,
    cc.naturaleza,
    COALESCE(SUM(da.debito), 0) as total_debito,
    COALESCE(SUM(da.credito), 0) as total_credito,
    CASE
        WHEN cc.naturaleza = 'DEUDORA' THEN COALESCE(SUM(da.debito), 0) - COALESCE(SUM(da.credito), 0)
        ELSE COALESCE(SUM(da.credito), 0) - COALESCE(SUM(da.debito), 0)
    END as saldo_actual
FROM cuentas_contables cc
LEFT JOIN detalles_asientos da ON cc.id = da.idCuentaContable
LEFT JOIN asientos_contables ac ON da.idAsiento = ac.id AND ac.balanceado = TRUE
WHERE cc.activa = TRUE
GROUP BY cc.id, cc.codigo, cc.nombre, cc.tipo, cc.naturaleza
ORDER BY cc.codigo;

-- Crear vista para libro mayor
CREATE OR REPLACE VIEW libro_mayor AS
SELECT
    ac.fecha,
    ac.numeroAsiento,
    cc.codigo as cuenta_codigo,
    cc.nombre as cuenta_nombre,
    da.concepto,
    da.debito,
    da.credito,
    ac.tipoTransaccion,
    ac.referenciaTransaccion
FROM detalles_asientos da
JOIN asientos_contables ac ON da.idAsiento = ac.id
JOIN cuentas_contables cc ON da.idCuentaContable = cc.id
WHERE ac.balanceado = TRUE
ORDER BY ac.fecha DESC, ac.numeroAsiento, da.id;

-- Procedimiento para validar que los asientos estén balanceados
DELIMITER //
CREATE PROCEDURE ValidarAsientosBalanceados()
BEGIN
    DECLARE total_desbalanceados INT;

    SELECT COUNT(*) INTO total_desbalanceados
    FROM asientos_contables
    WHERE balanceado = FALSE;

    IF total_desbalanceados > 0 THEN
        SELECT CONCAT('Hay ', total_desbalanceados, ' asientos desbalanceados') as mensaje;

        SELECT
            numeroAsiento,
            fecha,
            concepto,
            totalDebito,
            totalCredito,
            (totalDebito - totalCredito) as diferencia
        FROM asientos_contables
        WHERE balanceado = FALSE
        ORDER BY fecha DESC;
    ELSE
        SELECT 'Todos los asientos están balanceados' as mensaje;
    END IF;
END //
DELIMITER ;
