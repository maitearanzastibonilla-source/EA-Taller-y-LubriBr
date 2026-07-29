-- =============================================================
-- SGI EA Taller y LubriBr - Esquema de Base de Datos
-- Motor: MySQL 8
-- Modulo: Usuarios (base transversal de autenticacion y auditoria)
--
-- Este script se ira ampliando con una seccion por modulo, en el mismo
-- orden en que se desarrollen (Clientes, Vehiculos, Turnos, ...), respetando
-- siempre el modelo de datos definido en la Propuesta Tecnica.
-- =============================================================

CREATE DATABASE IF NOT EXISTS ea_taller_lubribr
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ea_taller_lubribr;

-- -------------------------------------------------------------
-- Tabla: usuarios
-- Objetivo: perfiles de acceso al sistema (Modulo de Usuarios).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(100)    NOT NULL,
    email             VARCHAR(150)    NOT NULL,
    password_hash     VARCHAR(60)     NOT NULL,
    rol               ENUM('ADMINISTRADOR', 'OPERADOR') NOT NULL,
    activo            BOOLEAN         NOT NULL DEFAULT TRUE,
    intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,
    bloqueado_hasta   DATETIME        NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT ck_usuarios_intentos_fallidos CHECK (intentos_fallidos >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_usuarios_rol ON usuarios (rol);
CREATE INDEX idx_usuarios_activo ON usuarios (activo);
CREATE INDEX idx_usuarios_nombre ON usuarios (nombre);

-- -------------------------------------------------------------
-- Tabla: auditoria
-- Objetivo: trazabilidad transversal de acciones relevantes de todos los
-- modulos del sistema (altas, modificaciones, bajas logicas, login).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id            BIGINT UNSIGNED NULL,
    modulo                VARCHAR(50)     NOT NULL,
    accion                VARCHAR(50)     NOT NULL,
    entidad_afectada      VARCHAR(50)     NOT NULL,
    id_registro_afectado  BIGINT UNSIGNED NULL,
    valores_anteriores    TEXT            NULL,
    valores_nuevos        TEXT            NULL,
    resultado             VARCHAR(20)     NOT NULL,
    observaciones         VARCHAR(255)    NULL,
    fecha_hora            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_auditoria_usuario ON auditoria (usuario_id);
CREATE INDEX idx_auditoria_modulo ON auditoria (modulo);
CREATE INDEX idx_auditoria_fecha ON auditoria (fecha_hora);
