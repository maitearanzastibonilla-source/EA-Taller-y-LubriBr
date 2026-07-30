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

-- -------------------------------------------------------------
-- Tabla: clientes
-- Objetivo: ficha de clientes del taller (Modulo de Clientes).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente   BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(60)  NOT NULL,
    apellido     VARCHAR(60)  NOT NULL,
    dni          VARCHAR(15)  NOT NULL,
    email        VARCHAR(150) NULL,
    telefono     VARCHAR(30)  NOT NULL,
    direccion    VARCHAR(200) NULL,
    estado       ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_clientes_dni UNIQUE (dni)
) ENGINE = InnoDB;

CREATE INDEX idx_clientes_apellido ON clientes (apellido, nombre);
CREATE INDEX idx_clientes_telefono ON clientes (telefono);
CREATE INDEX idx_clientes_estado ON clientes (estado);

-- Nota: la restriccion "no se puede dar de baja un cliente con vehiculos
-- activos, turnos pendientes o saldo en cuenta corriente" se activa cuando
-- se incorporen esos modulos (las FK correspondientes se agregaran con
-- ON DELETE RESTRICT y la validacion de negocio en ClienteServiceImpl).

-- -------------------------------------------------------------
-- Tabla: vehiculos
-- Objetivo: vehiculos asociados a cada cliente (Modulo de Vehiculos).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehiculos (
    id_vehiculo  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id   BIGINT UNSIGNED NOT NULL,
    patente      VARCHAR(10)  NOT NULL,
    marca        VARCHAR(60)  NOT NULL,
    modelo       VARCHAR(60)  NOT NULL,
    anio         SMALLINT UNSIGNED NOT NULL,
    estado       ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_vehiculos_patente UNIQUE (patente),
    CONSTRAINT fk_vehiculos_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT ck_vehiculos_anio CHECK (anio >= 1950)
) ENGINE = InnoDB;

CREATE INDEX idx_vehiculos_cliente ON vehiculos (cliente_id);
CREATE INDEX idx_vehiculos_marca_modelo ON vehiculos (marca, modelo);
CREATE INDEX idx_vehiculos_estado ON vehiculos (estado);

-- Nota: "no se puede dar de baja un vehiculo con turnos activos o trabajos
-- pendientes" se activa cuando existan esas tablas (Turnos, Trabajos
-- Realizados).

-- -------------------------------------------------------------
-- Tabla: turnos
-- Objetivo: agenda de turnos del taller (Modulo de Turnos).
--
-- La capacidad simultanea del taller (cuantos turnos puede haber en el
-- mismo horario) no esta cuantificada en la Propuesta Tecnica; se fijo en
-- AppConstants.CAPACIDAD_TALLER_SIMULTANEA (2) como valor de referencia,
-- ajustable sin tocar el modelo de datos.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS turnos (
    id_turno       BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cliente_id     BIGINT UNSIGNED NOT NULL,
    vehiculo_id    BIGINT UNSIGNED NOT NULL,
    usuario_id     BIGINT UNSIGNED NOT NULL,
    fecha_hora     DATETIME     NOT NULL,
    tipo_servicio  ENUM('MECANICA', 'LUBRICENTRO', 'OTRO') NOT NULL,
    estado         ENUM('PENDIENTE', 'CONFIRMADO', 'EN_PROCESO', 'FINALIZADO', 'CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    notas          VARCHAR(500) NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_turnos_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes (id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_turnos_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos (id_vehiculo) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_turnos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_turnos_fecha_hora ON turnos (fecha_hora);
CREATE INDEX idx_turnos_cliente ON turnos (cliente_id);
CREATE INDEX idx_turnos_vehiculo ON turnos (vehiculo_id);
CREATE INDEX idx_turnos_estado ON turnos (estado);
CREATE INDEX idx_turnos_tipo_servicio ON turnos (tipo_servicio);
