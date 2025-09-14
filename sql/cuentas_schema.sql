-- Script SQL para la funcionalidad BNK-F-002: Apertura y manejo de cuentas
-- Cuentas de ahorro y corrientes en colones y dólares con configuración multimoneda
-- Estados: activa, bloqueada, cerrada

-- Crear tabla de cuentas si no existe
CREATE TABLE IF NOT EXISTS cuentas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    numeroCuenta VARCHAR(50) UNIQUE NOT NULL,
    tipo ENUM('AHORRO', 'CORRIENTE') NOT NULL,
    tipoMoneda ENUM('CRC', 'USD') NOT NULL,
    saldo DECIMAL(15,2) DEFAULT 0.00,
    estado ENUM('ACTIVA', 'BLOQUEADA', 'CERRADA') DEFAULT 'ACTIVA',
    idCliente INT NOT NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fechaUltimoMovimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Índices para mejorar rendimiento
    INDEX idx_cliente (idCliente),
    INDEX idx_numero_cuenta (numeroCuenta),
    INDEX idx_estado (estado),
    INDEX idx_moneda (tipoMoneda),
    INDEX idx_tipo (tipo),

    -- Constraint para asegurar que el saldo no sea negativo
    CONSTRAINT chk_saldo_positivo CHECK (saldo >= 0),

    -- Foreign key constraint (assuming clientes table exists)
    FOREIGN KEY (idCliente) REFERENCES clientes(id) ON DELETE RESTRICT
);

-- Insertar datos de ejemplo para pruebas (opcional)
INSERT IGNORE INTO cuentas (numeroCuenta, tipo, tipoMoneda, saldo, estado, idCliente) VALUES
('CR000000000000000001', 'AHORRO', 'CRC', 100000.00, 'ACTIVA', 1),
('US000000000000000001', 'AHORRO', 'USD', 500.00, 'ACTIVA', 1),
('CR000000000000000002', 'CORRIENTE', 'CRC', 250000.00, 'ACTIVA', 2),
('US000000000000000002', 'CORRIENTE', 'USD', 1000.00, 'BLOQUEADA', 2),
('CR000000000000000003', 'AHORRO', 'CRC', 0.00, 'CERRADA', 3);

-- Crear vista para estadísticas del sistema
CREATE OR REPLACE VIEW vista_estadisticas_cuentas AS
SELECT
    COUNT(*) as total_cuentas,
    COUNT(CASE WHEN estado = 'ACTIVA' THEN 1 END) as cuentas_activas,
    COUNT(CASE WHEN estado = 'BLOQUEADA' THEN 1 END) as cuentas_bloqueadas,
    COUNT(CASE WHEN estado = 'CERRADA' THEN 1 END) as cuentas_cerradas,
    COUNT(CASE WHEN tipoMoneda = 'CRC' THEN 1 END) as cuentas_colones,
    COUNT(CASE WHEN tipoMoneda = 'USD' THEN 1 END) as cuentas_dolares,
    COUNT(CASE WHEN tipo = 'AHORRO' THEN 1 END) as cuentas_ahorro,
    COUNT(CASE WHEN tipo = 'CORRIENTE' THEN 1 END) as cuentas_corrientes,
    SUM(CASE WHEN tipoMoneda = 'CRC' AND estado != 'CERRADA' THEN saldo ELSE 0 END) as total_saldo_crc,
    SUM(CASE WHEN tipoMoneda = 'USD' AND estado != 'CERRADA' THEN saldo ELSE 0 END) as total_saldo_usd
FROM cuentas;

-- Crear función para generar número de cuenta único
DELIMITER //
CREATE FUNCTION IF NOT EXISTS generar_numero_cuenta(moneda VARCHAR(3))
RETURNS VARCHAR(50)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE prefijo VARCHAR(2);
    DECLARE numero_base BIGINT;
    DECLARE numero_cuenta VARCHAR(50);
    DECLARE existe INT DEFAULT 1;

    -- Determinar prefijo según moneda
    IF moneda = 'CRC' THEN
        SET prefijo = 'CR';
    ELSE
        SET prefijo = 'US';
    END IF;

    -- Generar número único
    WHILE existe > 0 DO
        SET numero_base = FLOOR(RAND() * 1000000000000000000);
        SET numero_cuenta = CONCAT(prefijo, LPAD(numero_base, 18, '0'));

        SELECT COUNT(*) INTO existe
        FROM cuentas
        WHERE numeroCuenta = numero_cuenta;
    END WHILE;

    RETURN numero_cuenta;
END //
DELIMITER ;

-- Crear trigger para auditoría de cambios de estado
CREATE TABLE IF NOT EXISTS auditoria_cuentas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id INT NOT NULL,
    numero_cuenta VARCHAR(50) NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20),
    usuario VARCHAR(100),
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT
);

DELIMITER //
CREATE TRIGGER IF NOT EXISTS tr_auditoria_estado_cuenta
AFTER UPDATE ON cuentas
FOR EACH ROW
BEGIN
    IF OLD.estado != NEW.estado THEN
        INSERT INTO auditoria_cuentas (cuenta_id, numero_cuenta, estado_anterior, estado_nuevo, usuario)
        VALUES (NEW.id, NEW.numeroCuenta, OLD.estado, NEW.estado, USER());
    END IF;
END //
DELIMITER ;

-- Crear procedimientos almacenados para operaciones comunes

-- Procedimiento para crear cuenta con validaciones
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_crear_cuenta(
    IN p_idCliente INT,
    IN p_tipo VARCHAR(20),
    IN p_tipoMoneda VARCHAR(3),
    IN p_saldoInicial DECIMAL(15,2)
)
BEGIN
    DECLARE v_numeroCuenta VARCHAR(50);
    DECLARE v_kycVerificado BOOLEAN DEFAULT FALSE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Verificar que el cliente existe y tiene KYC verificado
    SELECT kycVerificado INTO v_kycVerificado
    FROM clientes
    WHERE id = p_idCliente;

    IF v_kycVerificado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cliente no encontrado';
    END IF;

    IF v_kycVerificado = FALSE THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cliente no tiene KYC verificado';
    END IF;

    -- Generar número de cuenta
    SET v_numeroCuenta = generar_numero_cuenta(p_tipoMoneda);

    -- Insertar nueva cuenta
    INSERT INTO cuentas (numeroCuenta, tipo, tipoMoneda, saldo, estado, idCliente)
    VALUES (v_numeroCuenta, p_tipo, p_tipoMoneda, IFNULL(p_saldoInicial, 0), 'ACTIVA', p_idCliente);

    COMMIT;

    SELECT v_numeroCuenta as numeroCuenta;
END //
DELIMITER ;

-- Procedimiento para cambiar estado de cuenta
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_cambiar_estado_cuenta(
    IN p_numeroCuenta VARCHAR(50),
    IN p_nuevoEstado VARCHAR(20),
    IN p_motivo TEXT
)
BEGIN
    DECLARE v_saldoActual DECIMAL(15,2);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- Obtener saldo actual
    SELECT saldo INTO v_saldoActual
    FROM cuentas
    WHERE numeroCuenta = p_numeroCuenta;

    IF v_saldoActual IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cuenta no encontrada';
    END IF;

    -- Validar que se puede cerrar la cuenta (saldo debe ser cero)
    IF p_nuevoEstado = 'CERRADA' AND v_saldoActual != 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede cerrar cuenta con saldo diferente de cero';
    END IF;

    -- Actualizar estado
    UPDATE cuentas
    SET estado = p_nuevoEstado, fechaUltimoMovimiento = NOW()
    WHERE numeroCuenta = p_numeroCuenta;

    -- Insertar en auditoría si se proporcionó motivo
    IF p_motivo IS NOT NULL THEN
        UPDATE auditoria_cuentas
        SET motivo = p_motivo
        WHERE cuenta_id = (SELECT id FROM cuentas WHERE numeroCuenta = p_numeroCuenta)
        ORDER BY fecha_cambio DESC
        LIMIT 1;
    END IF;

    COMMIT;
END //
DELIMITER ;

-- Crear índices adicionales para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente_moneda ON cuentas(idCliente, tipoMoneda);
CREATE INDEX IF NOT EXISTS idx_cuentas_estado_tipo ON cuentas(estado, tipo);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria_cuentas(fecha_cambio);

-- Comentarios sobre la estructura
-- Esta tabla soporta:
-- 1. Configuración multimoneda (CRC y USD)
-- 2. Tipos de cuenta (AHORRO y CORRIENTE)
-- 3. Estados (ACTIVA, BLOQUEADA, CERRADA)
-- 4. Auditoría de cambios
-- 5. Restricciones de integridad
-- 6. Índices para optimización de consultas
