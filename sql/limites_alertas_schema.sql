-- Script SQL para crear las tablas del sistema de límites, alertas y notificaciones
-- BNK-F-010: Límites, alertas y notificaciones

-- Tabla para límites de transacciones
CREATE TABLE limite_transaccion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL,
    tipo_limite VARCHAR(50) NOT NULL,
    monto_limite DECIMAL(15,2) NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NULL,

    INDEX idx_limite_cuenta (numero_cuenta),
    INDEX idx_limite_tipo (tipo_limite),
    INDEX idx_limite_activo (activo),
    UNIQUE KEY uk_limite_cuenta_tipo (numero_cuenta, tipo_limite)
);

-- Tabla para alertas del sistema
CREATE TABLE alerta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL,
    tipo_alerta VARCHAR(50) NOT NULL,
    mensaje TEXT NOT NULL,
    monto_transaccion DECIMAL(15,2) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    prioridad VARCHAR(10) NOT NULL DEFAULT 'MEDIA',
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_procesamiento DATETIME NULL,
    email_enviado BOOLEAN DEFAULT FALSE,
    sms_enviado BOOLEAN DEFAULT FALSE,
    metadatos TEXT NULL,

    INDEX idx_alerta_cuenta (numero_cuenta),
    INDEX idx_alerta_tipo (tipo_alerta),
    INDEX idx_alerta_estado (estado),
    INDEX idx_alerta_prioridad (prioridad),
    INDEX idx_alerta_fecha (fecha_generacion)
);

-- Tabla para configuración de notificaciones
CREATE TABLE configuracion_notificacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    notificacion_email BOOLEAN NOT NULL DEFAULT TRUE,
    notificacion_sms BOOLEAN NOT NULL DEFAULT TRUE,
    alertas_limite BOOLEAN NOT NULL DEFAULT TRUE,
    alertas_transacciones BOOLEAN NOT NULL DEFAULT TRUE,
    alertas_seguridad BOOLEAN NOT NULL DEFAULT TRUE,
    umbral_alerta_porcentaje INT NOT NULL DEFAULT 80,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion DATETIME NULL,

    INDEX idx_config_cuenta (numero_cuenta),
    INDEX idx_config_activo (activo),
    UNIQUE KEY uk_config_cuenta (numero_cuenta)
);

-- Tabla para historial de bloqueos
CREATE TABLE historial_bloqueo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL,
    tipo_bloqueo VARCHAR(50) NOT NULL,
    motivo TEXT NOT NULL,
    fecha_bloqueo DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_desbloqueo DATETIME NULL,
    usuario_bloqueo VARCHAR(50) NOT NULL,
    usuario_desbloqueo VARCHAR(50) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    INDEX idx_bloqueo_cuenta (numero_cuenta),
    INDEX idx_bloqueo_tipo (tipo_bloqueo),
    INDEX idx_bloqueo_fecha (fecha_bloqueo),
    INDEX idx_bloqueo_activo (activo)
);

-- Tabla para auditoría de cambios en límites
CREATE TABLE auditoria_limite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    limite_id BIGINT NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL,
    tipo_limite VARCHAR(50) NOT NULL,
    monto_anterior DECIMAL(15,2) NULL,
    monto_nuevo DECIMAL(15,2) NOT NULL,
    periodo_anterior VARCHAR(20) NULL,
    periodo_nuevo VARCHAR(20) NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    fecha_cambio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_operacion VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE

    INDEX idx_audit_limite (limite_id),
    INDEX idx_audit_cuenta (numero_cuenta),
    INDEX idx_audit_fecha (fecha_cambio),
    FOREIGN KEY (limite_id) REFERENCES limite_transaccion(id)
);

-- Insertar configuraciones predeterminadas de límites
INSERT INTO limite_transaccion (numero_cuenta, tipo_limite, monto_limite, periodo) VALUES
('1001', 'TRANSFERENCIA_NACIONAL', 500000.00, 'DIARIO'),
('1001', 'TRANSFERENCIA_INTERNACIONAL', 1000000.00, 'DIARIO'),
('1001', 'RETIRO_ATM', 200000.00, 'DIARIO'),
('1001', 'COMPRA_TARJETA', 300000.00, 'DIARIO'),
('1001', 'PAGO_SERVICIOS', 150000.00, 'DIARIO');

-- Insertar configuración predeterminada de notificaciones
INSERT INTO configuracion_notificacion (numero_cuenta, email, telefono) VALUES
('1001', 'cliente@example.com', '8888-8888');

-- Crear vista para resumen de límites por cuenta
CREATE VIEW vista_resumen_limites AS
SELECT
    numero_cuenta,
    COUNT(*) as total_limites,
    SUM(CASE WHEN activo = TRUE THEN 1 ELSE 0 END) as limites_activos,
    SUM(CASE WHEN tipo_limite = 'TRANSFERENCIA_NACIONAL' THEN monto_limite ELSE 0 END) as limite_transferencias,
    SUM(CASE WHEN tipo_limite = 'RETIRO_ATM' THEN monto_limite ELSE 0 END) as limite_retiros,
    MAX(fecha_modificacion) as ultima_modificacion
FROM limite_transaccion
GROUP BY numero_cuenta;

-- Crear vista para resumen de alertas
CREATE VIEW vista_resumen_alertas AS
SELECT
    numero_cuenta,
    COUNT(*) as total_alertas,
    SUM(CASE WHEN estado = 'PENDIENTE' THEN 1 ELSE 0 END) as alertas_pendientes,
    SUM(CASE WHEN prioridad = 'CRITICA' THEN 1 ELSE 0 END) as alertas_criticas,
    SUM(CASE WHEN email_enviado = TRUE THEN 1 ELSE 0 END) as emails_enviados,
    SUM(CASE WHEN sms_enviado = TRUE THEN 1 ELSE 0 END) as sms_enviados,
    MAX(fecha_generacion) as ultima_alerta
FROM alerta
WHERE fecha_generacion >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY numero_cuenta;

-- Procedimiento para generar alerta automática
DELIMITER //
CREATE PROCEDURE GenerarAlertaLimite(
    IN p_numero_cuenta VARCHAR(20),
    IN p_tipo_alerta VARCHAR(50),
    IN p_mensaje TEXT,
    IN p_monto DECIMAL(15,2),
    IN p_prioridad VARCHAR(10)
)
BEGIN
    INSERT INTO alerta (numero_cuenta, tipo_alerta, mensaje, monto_transaccion, prioridad)
    VALUES (p_numero_cuenta, p_tipo_alerta, p_mensaje, p_monto, p_prioridad);
END //
DELIMITER ;

-- Trigger para auditoría de cambios en límites
DELIMITER //
CREATE TRIGGER tr_limite_auditoria
AFTER UPDATE ON limite_transaccion
FOR EACH ROW
BEGIN
    INSERT INTO auditoria_limite (
        limite_id, numero_cuenta, tipo_limite,
        monto_anterior, monto_nuevo,
        periodo_anterior, periodo_nuevo,
        usuario, tipo_operacion
    ) VALUES (
        NEW.id, NEW.numero_cuenta, NEW.tipo_limite,
        OLD.monto_limite, NEW.monto_limite,
        OLD.periodo, NEW.periodo,
        USER(), 'UPDATE'
    );
END //
DELIMITER ;

-- Índices adicionales para optimización
CREATE INDEX idx_alerta_cuenta_fecha ON alerta(numero_cuenta, fecha_generacion DESC);
CREATE INDEX idx_limite_cuenta_activo ON limite_transaccion(numero_cuenta, activo);
CREATE INDEX idx_config_notif_activo ON configuracion_notificacion(activo, numero_cuenta);

-- Comentarios en las tablas
ALTER TABLE limite_transaccion COMMENT = 'Tabla para gestión de límites de transacciones por cuenta';
ALTER TABLE alerta COMMENT = 'Tabla para registro de alertas automáticas del sistema';
ALTER TABLE configuracion_notificacion COMMENT = 'Configuración de notificaciones por cuenta de cliente';
ALTER TABLE historial_bloqueo COMMENT = 'Historial de bloqueos y desbloqueos de cuentas';
ALTER TABLE auditoria_limite COMMENT = 'Auditoría de cambios en límites de transacciones';
