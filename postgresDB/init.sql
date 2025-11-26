-- CREACIÓN DE TABLAS SEGÚN EL MODELO ENTREGADO
-- Adaptado para PostgreSQL
-- 1. PAISES
CREATE TABLE paises (
    pais_id        INTEGER       PRIMARY KEY,
    pais_nombre    VARCHAR(100)  NOT NULL
);

-- 2. CIUDADES
CREATE TABLE ciudades (
    ciud_id        INTEGER        PRIMARY KEY,
    ciud_pais_id   INTEGER        NOT NULL,
    ciud_nombre    VARCHAR(100)   NOT NULL,
    CONSTRAINT fk_ciudad_pais
        FOREIGN KEY (ciud_pais_id)
        REFERENCES paises (pais_id)
);

-- 3. LOCALIZACIONES
CREATE TABLE localizaciones (
    localiz_id         INTEGER        PRIMARY KEY,
    localiz_ciudad_id  INTEGER        NOT NULL,
    localiz_direccion  VARCHAR(200)   NOT NULL,
    CONSTRAINT fk_localiz_ciudad
        FOREIGN KEY (localiz_ciudad_id)
        REFERENCES ciudades (ciud_id)
);

-- 4. DEPARTAMENTOS  (AHORA CON FK HACIA LOCALIZACIONES)
CREATE TABLE departamentos (
    dpto_id          INTEGER        PRIMARY KEY,
    dpto_nombre      VARCHAR(100)   NOT NULL,
    dpto_localiz_id  INTEGER        NOT NULL,

    CONSTRAINT fk_dpto_localiz
        FOREIGN KEY (dpto_localiz_id)
        REFERENCES localizaciones (localiz_id)
);

-- 5. CARGOS
CREATE TABLE cargos (
    cargo_id            INTEGER        PRIMARY KEY,
    cargo_nombre        VARCHAR(100)   NOT NULL,
    cargo_sueldo_minimo NUMERIC(12,2)  NOT NULL,
    cargo_sueldo_maximo NUMERIC(12,2)  NOT NULL
);

-- 6. EMPLEADOS
CREATE TABLE empleados (
    empl_id              INTEGER        PRIMARY KEY,
    empl_primer_nombre   VARCHAR(100)   NOT NULL,
    empl_segundo_nombre  VARCHAR(100),
    empl_email           VARCHAR(150)   NOT NULL,
    empl_fecha_nac       DATE           NOT NULL,
    empl_sueldo          NUMERIC(12,2)  NOT NULL,
    empl_comision        NUMERIC(12,2),

    empl_cargo_id        INTEGER        NOT NULL,
    empl_gerente_id      INTEGER,
    empl_dpto_id         INTEGER        NOT NULL,

    CONSTRAINT fk_empleado_cargo
        FOREIGN KEY (empl_cargo_id)
        REFERENCES cargos (cargo_id),

    CONSTRAINT fk_empleado_dpto
        FOREIGN KEY (empl_dpto_id)
        REFERENCES departamentos (dpto_id),

    CONSTRAINT fk_empleado_gerente
        FOREIGN KEY (empl_gerente_id)
        REFERENCES empleados (empl_id)
);

-- 7. HISTORICO
CREATE TABLE historico (
    emphist_id          INTEGER        PRIMARY KEY,
    emphist_fecha_retiro DATE          NOT NULL,

    emphist_cargo_id    INTEGER        NOT NULL,
    emphist_dpto_id     INTEGER        NOT NULL,
    emphist_empl_id     INTEGER        NOT NULL,

    CONSTRAINT fk_hist_cargo
        FOREIGN KEY (emphist_cargo_id)
        REFERENCES cargos (cargo_id),

    CONSTRAINT fk_hist_dpto
        FOREIGN KEY (emphist_dpto_id)
        REFERENCES departamentos (dpto_id),

    CONSTRAINT fk_hist_empleado
        FOREIGN KEY (emphist_empl_id)
        REFERENCES empleados (empl_id)
);
-- FIN DEL SCRIPT