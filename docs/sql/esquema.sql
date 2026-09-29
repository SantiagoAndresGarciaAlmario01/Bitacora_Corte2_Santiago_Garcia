-- =====================================================================
-- Sushi Craft (Kaze & Nori) - Esquema relacional en PostgreSQL
-- Bitacora DOSW Corte 2 - Santiago Garcia
--
-- La aplicacion crea estas tablas sola (spring.jpa.hibernate.ddl-auto=update)
-- a partir de las entidades del paquete persistence.entity. Este script es
-- el equivalente en SQL, util para revisar el modelo o crear la base a mano:
--   psql -h localhost -p 5433 -U sushicraft -d sushicraft_db -f docs/sql/esquema.sql
-- =====================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id               BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    correo           VARCHAR(120) NOT NULL UNIQUE,
    contrasena_hash  VARCHAR(100) NOT NULL,          -- hash BCrypt
    rol              VARCHAR(20)  NOT NULL
        CHECK (rol IN ('GERENTE', 'MESERO', 'COCINERO', 'CLIENTE'))
);

CREATE TABLE IF NOT EXISTS platos (
    id           BIGSERIAL        PRIMARY KEY,
    nombre       VARCHAR(100)     NOT NULL,
    precio       DOUBLE PRECISION NOT NULL CHECK (precio > 0),
    categoria    VARCHAR(30)      NOT NULL,
    disponible   BOOLEAN          NOT NULL DEFAULT TRUE,
    descripcion  VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS mesas (
    id         BIGSERIAL   PRIMARY KEY,
    numero     INTEGER     NOT NULL UNIQUE,
    capacidad  INTEGER     NOT NULL CHECK (capacidad BETWEEN 1 AND 12),
    estado     VARCHAR(20) NOT NULL
        CHECK (estado IN ('LIBRE', 'OCUPADA', 'RESERVADA'))
);

CREATE TABLE IF NOT EXISTS cuentas (
    id              BIGSERIAL        PRIMARY KEY,
    id_mesa         BIGINT           NOT NULL,
    estado          VARCHAR(20)      NOT NULL CHECK (estado IN ('ABIERTA', 'CERRADA')),
    total           DOUBLE PRECISION NOT NULL DEFAULT 0,
    fecha_apertura  TIMESTAMP        NOT NULL,
    fecha_cierre    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pedidos (
    id              BIGSERIAL   PRIMARY KEY,
    id_mesa         BIGINT      NOT NULL,
    id_cuenta       BIGINT      NOT NULL,
    estado          VARCHAR(20) NOT NULL
        CHECK (estado IN ('RECIBIDO', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO')),
    fecha_creacion  TIMESTAMP   NOT NULL
);

-- Los items se borran junto con su pedido
CREATE TABLE IF NOT EXISTS items_pedido (
    id               BIGSERIAL        PRIMARY KEY,
    id_pedido        BIGINT           NOT NULL REFERENCES pedidos (id) ON DELETE CASCADE,
    id_plato         BIGINT           NOT NULL,
    nombre_plato     VARCHAR(100)     NOT NULL,   -- copia del nombre al momento de pedir
    precio_unitario  DOUBLE PRECISION NOT NULL,   -- copia del precio al momento de pedir
    cantidad         INTEGER          NOT NULL CHECK (cantidad BETWEEN 1 AND 20)
);

CREATE TABLE IF NOT EXISTS reservas (
    id               BIGSERIAL    PRIMARY KEY,
    id_mesa          BIGINT       NOT NULL,
    nombre_cliente   VARCHAR(100) NOT NULL,
    fecha_hora       TIMESTAMP    NOT NULL,
    numero_personas  INTEGER      NOT NULL CHECK (numero_personas >= 1),
    cancelada        BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS registros_parqueadero (
    id            BIGSERIAL   PRIMARY KEY,
    placa         VARCHAR(10) NOT NULL,
    hora_entrada  TIMESTAMP   NOT NULL,
    hora_salida   TIMESTAMP
);

-- Indices para las consultas mas frecuentes de la API
CREATE INDEX IF NOT EXISTS idx_pedidos_cuenta      ON pedidos (id_cuenta);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado      ON pedidos (estado);
CREATE INDEX IF NOT EXISTS idx_cuentas_mesa_estado ON cuentas (id_mesa, estado);
CREATE INDEX IF NOT EXISTS idx_reservas_mesa       ON reservas (id_mesa) WHERE cancelada = FALSE;
CREATE INDEX IF NOT EXISTS idx_parqueadero_activos ON registros_parqueadero (placa) WHERE hora_salida IS NULL;
