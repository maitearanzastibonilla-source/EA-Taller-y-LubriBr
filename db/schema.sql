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

-- -------------------------------------------------------------
-- Tabla: trabajos_realizados
-- Objetivo: registro operativo de cada trabajo hecho sobre un vehiculo
-- (Modulo de Trabajos Realizados). Puede originarse en un turno o cargarse
-- directamente desde el mostrador (turno_id queda NULL en ese caso).
--
-- La Propuesta Tecnica no incluye un valor de "estado" para la baja logica
-- dentro del enum de trabajos (en_proceso/finalizado/facturado, que
-- describe la etapa del trabajo, no si esta dado de baja). Se agrego la
-- columna `activo`, siguiendo el mismo criterio que usuarios (rol +
-- activo por separado), para poder implementar la baja administrativa que
-- la Propuesta si pide en el texto.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trabajos_realizados (
    id_trabajo     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id    BIGINT UNSIGNED NOT NULL,
    turno_id       BIGINT UNSIGNED NULL,
    usuario_id     BIGINT UNSIGNED NOT NULL,
    fecha_ingreso  DATE NOT NULL,
    fecha_egreso   DATE NULL,
    descripcion    TEXT NOT NULL,
    estado         ENUM('EN_PROCESO', 'FINALIZADO', 'FACTURADO') NOT NULL DEFAULT 'EN_PROCESO',
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_trabajos_vehiculo FOREIGN KEY (vehiculo_id)
        REFERENCES vehiculos (id_vehiculo) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_trabajos_turno FOREIGN KEY (turno_id)
        REFERENCES turnos (id_turno) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_trabajos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_trabajos_vehiculo ON trabajos_realizados (vehiculo_id);
CREATE INDEX idx_trabajos_turno ON trabajos_realizados (turno_id);
CREATE INDEX idx_trabajos_estado ON trabajos_realizados (estado);
CREATE INDEX idx_trabajos_fecha_ingreso ON trabajos_realizados (fecha_ingreso);

-- Nota: "no se puede cerrar un trabajo sin al menos un item registrado" y
-- "no se puede eliminar un trabajo con comprobantes o movimientos
-- economicos" se activan cuando existan esas tablas (Items de Trabajo,
-- Comprobantes).

-- -------------------------------------------------------------
-- Tabla: items_trabajo
-- Objetivo: detalle de repuestos/mano de obra de cada trabajo realizado
-- (Modulo de Items de Trabajo).
--
-- producto_id queda sin FK por ahora: el Modulo de Productos (catalogo y
-- stock) todavia no existe en el sistema, aunque el orden de modulos de la
-- Propuesta Tecnica pone a Items de Trabajo antes que Productos. Por eso
-- todo item se carga hoy como descripcion libre (mano de obra u otro
-- servicio); cuando se desarrolle Productos se agregara la restriccion
-- FOREIGN KEY sobre producto_id y el descuento/restauracion de stock.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS items_trabajo (
    id_item           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    trabajo_id        BIGINT UNSIGNED NOT NULL,
    producto_id       BIGINT UNSIGNED NULL,
    descripcion_libre VARCHAR(300) NULL,
    cantidad          DECIMAL(10,2) NOT NULL,
    precio_unitario   DECIMAL(12,2) NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_trabajo_trabajo FOREIGN KEY (trabajo_id)
        REFERENCES trabajos_realizados (id_trabajo) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_items_trabajo_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_items_trabajo_precio CHECK (precio_unitario >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_items_trabajo_trabajo ON items_trabajo (trabajo_id);

-- -------------------------------------------------------------
-- Tabla: comprobantes
-- Objetivo: comprobante de cobro emitido sobre un trabajo finalizado
-- (Modulo de Comprobantes). pdf_url guarda la ruta absoluta del PDF
-- generado (fuera del directorio del WAR, para que sobreviva un
-- redeploy). Se inserta con pdf_url vacio y se actualiza en la misma
-- transaccion, una vez generado el archivo.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comprobantes (
    id_comprobante  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    trabajo_id      BIGINT UNSIGNED NOT NULL,
    fecha           DATE NOT NULL,
    total           DECIMAL(12,2) NOT NULL,
    metodo_pago     ENUM('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'OTRO') NOT NULL,
    estado          ENUM('PENDIENTE', 'SENADO', 'COBRADO', 'ANULADO') NOT NULL DEFAULT 'PENDIENTE',
    pdf_url         VARCHAR(300) NOT NULL DEFAULT '',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_comprobantes_trabajo FOREIGN KEY (trabajo_id)
        REFERENCES trabajos_realizados (id_trabajo) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_comprobantes_total CHECK (total >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_comprobantes_trabajo ON comprobantes (trabajo_id);
CREATE INDEX idx_comprobantes_estado ON comprobantes (estado);
CREATE INDEX idx_comprobantes_fecha ON comprobantes (fecha);
