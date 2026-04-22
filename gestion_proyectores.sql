-- ============================================================
-- SISTEMA AUTOMATIZADO DE GESTIÓN DE PROYECTORES EN AULAS
-- Base de datos: gestion_proyectores
-- Motor: PostgreSQL 15
-- ============================================================
-- ORDEN DE EJECUCIÓN:
--   1. Crear la base de datos y conectarse a ella
--   2. Tablas (respetando dependencias FK)
--   3. Índices
--   4. Datos semilla (catálogos)
--   5. Datos operativos iniciales (aulas y dispositivos)
-- ============================================================

CREATE DATABASE gestion_proyectores;

-- Conectarse antes de continuar:
-- \c gestion_proyectores


-- ============================================================
-- 1. roles
--    Catálogo de roles disponibles. Define el nivel de acceso
--    de cada usuario en el sistema.
-- ============================================================
CREATE TABLE roles (
    id_rol      SERIAL       NOT NULL,
    nombre_rol  VARCHAR(100) NOT NULL,

    CONSTRAINT pk_roles        PRIMARY KEY (id_rol),
    CONSTRAINT uq_roles_nombre UNIQUE (nombre_rol)
);

COMMENT ON TABLE  roles            IS 'Catálogo de roles disponibles en el sistema.';
COMMENT ON COLUMN roles.id_rol     IS 'Identificador único del rol (PK, autoincremental).';
COMMENT ON COLUMN roles.nombre_rol IS 'Nombre descriptivo del rol (Administrador, Docente, Técnico).';


-- ============================================================
-- 2. usuarios
--    Usuarios registrados en el sistema. Cada usuario tiene
--    un rol que determina qué puede hacer en la aplicación.
-- ============================================================
CREATE TABLE usuarios (
    id_usuario  SERIAL       NOT NULL,
    nombre      VARCHAR(255) NOT NULL,
    id_rol      INTEGER      NOT NULL,
    estado      VARCHAR(50)  NOT NULL DEFAULT 'activo',

    CONSTRAINT pk_usuarios        PRIMARY KEY (id_usuario),
    CONSTRAINT fk_usuarios_rol    FOREIGN KEY (id_rol)
        REFERENCES roles (id_rol)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_usuarios_estado
        CHECK (estado IN ('activo', 'inactivo'))
);

COMMENT ON TABLE  usuarios            IS 'Usuarios registrados en el sistema.';
COMMENT ON COLUMN usuarios.id_usuario IS 'Identificador único del usuario (PK, autoincremental).';
COMMENT ON COLUMN usuarios.nombre     IS 'Nombre completo del usuario.';
COMMENT ON COLUMN usuarios.id_rol     IS 'FK hacia roles.id_rol. Define el nivel de acceso del usuario.';
COMMENT ON COLUMN usuarios.estado     IS 'Estado del usuario: activo o inactivo.';


-- ============================================================
-- 3. aulas
--    Aulas físicas del campus. Cada aula agrupa sus propios
--    dispositivos y genera lecturas de luminosidad.
-- ============================================================
CREATE TABLE aulas (
    id_aula    SERIAL       NOT NULL,
    ubicacion  VARCHAR(255) NOT NULL,
    estado     VARCHAR(50)  NOT NULL DEFAULT 'disponible',

    CONSTRAINT pk_aulas       PRIMARY KEY (id_aula),
    CONSTRAINT chk_aulas_estado
        CHECK (estado IN ('disponible', 'en_uso', 'mantenimiento'))
);

COMMENT ON TABLE  aulas           IS 'Aulas físicas del campus.';
COMMENT ON COLUMN aulas.id_aula   IS 'Identificador único del aula (PK, autoincremental).';
COMMENT ON COLUMN aulas.ubicacion IS 'Descripción textual de la ubicación física del aula.';
COMMENT ON COLUMN aulas.estado    IS 'Estado operativo: disponible, en_uso o mantenimiento.';


-- ============================================================
-- 4. tipos_dispositivo
--    Catálogo de los 6 tipos de dispositivos IoT del sistema.
--
--    IMPORTANTE: los nombre_tipo deben coincidir EXACTAMENTE
--    con los valores usados en el código Java para construir
--    los topics MQTT (patrón: aulas/{aulaId}/{tipo}/cmd|state).
--    No modificar estos nombres sin actualizar el código.
-- ============================================================
CREATE TABLE tipos_dispositivo (
    id_tipo      SERIAL       NOT NULL,
    nombre_tipo  VARCHAR(100) NOT NULL,

    CONSTRAINT pk_tipos_dispositivo  PRIMARY KEY (id_tipo),
    CONSTRAINT uq_tipos_nombre       UNIQUE (nombre_tipo)
);

COMMENT ON TABLE  tipos_dispositivo             IS 'Catálogo de tipos de dispositivos IoT.';
COMMENT ON COLUMN tipos_dispositivo.id_tipo     IS 'Identificador único del tipo (PK, autoincremental).';
COMMENT ON COLUMN tipos_dispositivo.nombre_tipo IS 'Nombre exacto usado en topics MQTT: lux_sensor, light, blind, screen, projector, monitor.';


-- ============================================================
-- 5. dispositivos
--    Inventario de los dispositivos emulados por aula.
--    Cada dispositivo tiene un tipo y pertenece a un aula.
--    estado_actual se actualiza en tiempo real cuando llegan
--    mensajes MQTT de estado desde los emuladores.
-- ============================================================
CREATE TABLE dispositivos (
    id_dispositivo  SERIAL       NOT NULL,
    id_aula         INTEGER      NOT NULL,
    id_tipo         INTEGER      NOT NULL,
    nombre          VARCHAR(255) NOT NULL,
    estado_actual   VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN',

    CONSTRAINT pk_dispositivos      PRIMARY KEY (id_dispositivo),
    CONSTRAINT uq_dispositivo_nombre UNIQUE (nombre),
    CONSTRAINT fk_dispositivos_aula FOREIGN KEY (id_aula)
        REFERENCES aulas (id_aula)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_dispositivos_tipo FOREIGN KEY (id_tipo)
        REFERENCES tipos_dispositivo (id_tipo)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

COMMENT ON TABLE  dispositivos                IS 'Inventario de dispositivos emulados por aula.';
COMMENT ON COLUMN dispositivos.id_dispositivo IS 'Identificador único del dispositivo (PK, autoincremental).';
COMMENT ON COLUMN dispositivos.id_aula        IS 'FK hacia aulas.id_aula.';
COMMENT ON COLUMN dispositivos.id_tipo        IS 'FK hacia tipos_dispositivo.id_tipo.';
COMMENT ON COLUMN dispositivos.nombre         IS 'Nombre único del dispositivo. Formato: {tipo}-{aulaId} (ej. light-aula-1). Coincide con el clientId MQTT.';
COMMENT ON COLUMN dispositivos.estado_actual  IS 'Estado en tiempo real, actualizado por cada mensaje MQTT de estado recibido.';


-- ============================================================
-- 6. solicitudes
--    Cada solicitud representa un pedido de encendido de
--    proyector hecho por un usuario desde la aplicación web.
--    Es el evento que dispara el flujo automatizado completo.
-- ============================================================
CREATE TABLE solicitudes (
    id_solicitud    SERIAL       NOT NULL,
    id_usuario      INTEGER      NOT NULL,
    id_aula         INTEGER      NOT NULL,
    fecha_solicitud TIMESTAMP    NOT NULL DEFAULT NOW(),
    estado          VARCHAR(50)  NOT NULL DEFAULT 'PROCESANDO',
    detalle         VARCHAR(500),

    CONSTRAINT pk_solicitudes        PRIMARY KEY (id_solicitud),
    CONSTRAINT fk_solicitudes_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_aula    FOREIGN KEY (id_aula)
        REFERENCES aulas (id_aula)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_solicitudes_estado
        CHECK (estado IN ('PROCESANDO', 'COMPLETADA', 'ERROR'))
);

COMMENT ON TABLE  solicitudes                 IS 'Solicitudes de encendido de proyector por usuario y aula.';
COMMENT ON COLUMN solicitudes.id_solicitud    IS 'Identificador único de la solicitud (PK, autoincremental).';
COMMENT ON COLUMN solicitudes.id_usuario      IS 'FK hacia usuarios.id_usuario. Usuario que generó la solicitud.';
COMMENT ON COLUMN solicitudes.id_aula         IS 'FK hacia aulas.id_aula. Aula objetivo de la solicitud.';
COMMENT ON COLUMN solicitudes.fecha_solicitud IS 'Fecha y hora exacta en que se registró la solicitud.';
COMMENT ON COLUMN solicitudes.estado          IS 'Estado del flujo: PROCESANDO, COMPLETADA o ERROR.';
COMMENT ON COLUMN solicitudes.detalle         IS 'Mensaje descriptivo del resultado o del error si ocurrió.';


-- ============================================================
-- 7. causas_lux
--    Catálogo de causas que explican por qué cambió el nivel
--    de iluminancia en un momento dado.
--
--    DECISIÓN DE DISEÑO: se normaliza como tabla catálogo
--    porque estos valores son referenciados por lecturas_lux
--    y permiten filtrar/agrupar lecturas por causa en consultas
--    analíticas (ej. "muéstrame solo los momentos donde se
--    apagaron las luces"). El código Java (enum LuxChangeCause)
--    usa los mismos nombres — no modificar sin actualizar el enum.
-- ============================================================
CREATE TABLE causas_lux (
    id_causa    SERIAL       NOT NULL,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255),

    CONSTRAINT pk_causas_lux  PRIMARY KEY (id_causa),
    CONSTRAINT uq_causas_nombre UNIQUE (nombre)
);

COMMENT ON TABLE  causas_lux            IS 'Catálogo de causas de cambio de iluminancia. Mapea el enum LuxChangeCause del código Java.';
COMMENT ON COLUMN causas_lux.id_causa   IS 'Identificador único de la causa (PK, autoincremental).';
COMMENT ON COLUMN causas_lux.nombre     IS 'Nombre exacto del valor del enum Java: INITIAL, LIGHTS_OFF, BLIND_CLOSED, STABLE, MANUAL.';
COMMENT ON COLUMN causas_lux.descripcion IS 'Descripción legible de la causa para el panel de administración.';


-- ============================================================
-- 8. lecturas_lux
--    Historial de lecturas de luminosidad. Cada fila es un
--    snapshot publicado por LuxSensorEmulator vía MQTT.
--    En estado estable: una lectura cada 2 segundos.
--    Durante transición: una lectura cada 300ms.
--    Umbral: <= 100 lux = condición óptima para proyección.
-- ============================================================
CREATE TABLE lecturas_lux (
    id_lectura  SERIAL    NOT NULL,
    id_aula     INTEGER   NOT NULL,
    valor_lux   INTEGER   NOT NULL,
    id_causa    INTEGER   NOT NULL,
    timestamp   TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_lecturas_lux   PRIMARY KEY (id_lectura),
    CONSTRAINT fk_lecturas_aula  FOREIGN KEY (id_aula)
        REFERENCES aulas (id_aula)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_lecturas_causa FOREIGN KEY (id_causa)
        REFERENCES causas_lux (id_causa)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_lecturas_lux_positivo
        CHECK (valor_lux >= 0)
);

COMMENT ON TABLE  lecturas_lux            IS 'Historial de snapshots de iluminancia publicados por el sensor emulado.';
COMMENT ON COLUMN lecturas_lux.id_lectura IS 'Identificador único de la lectura (PK, autoincremental).';
COMMENT ON COLUMN lecturas_lux.id_aula    IS 'FK hacia aulas.id_aula.';
COMMENT ON COLUMN lecturas_lux.valor_lux  IS 'Valor de luminosidad en lux (>= 0). Umbral óptimo: <= 100 lux.';
COMMENT ON COLUMN lecturas_lux.id_causa   IS 'FK hacia causas_lux.id_causa. Explica por qué cambió el lux en este snapshot.';
COMMENT ON COLUMN lecturas_lux.timestamp  IS 'Fecha y hora exacta del snapshot. Eje X de la gráfica en el frontend.';


-- ============================================================
-- 9. tipo_de_evento
--    Catálogo de clasificaciones de eventos MQTT que el sistema
--    procesa y registra en la bitácora eventos_sistema.
-- ============================================================
CREATE TABLE tipo_de_evento (
    id_tipo_evento  SERIAL       NOT NULL,
    descripcion     VARCHAR(255) NOT NULL,

    CONSTRAINT pk_tipo_de_evento   PRIMARY KEY (id_tipo_evento),
    CONSTRAINT uq_tipo_evento_desc UNIQUE (descripcion)
);

COMMENT ON TABLE  tipo_de_evento                IS 'Catálogo de tipos de eventos del sistema.';
COMMENT ON COLUMN tipo_de_evento.id_tipo_evento IS 'Identificador único del tipo de evento (PK, autoincremental).';
COMMENT ON COLUMN tipo_de_evento.descripcion    IS 'Descripción del tipo: solicitud_recibida, comando_enviado, estado_actualizado, lectura_sensor, error.';


-- ============================================================
-- 10. eventos_sistema
--     Bitácora completa de todos los mensajes MQTT procesados.
--     Spring Boot escribe aquí cada vez que recibe un mensaje
--     del broker EMQX, antes de enrutar al service correcto.
--     Permite auditar todo el tráfico del sistema.
-- ============================================================
CREATE TABLE eventos_sistema (
    id_evento       SERIAL       NOT NULL,
    id_aula         INTEGER,
    id_tipo_evento  INTEGER      NOT NULL,
    topico_mqtt     VARCHAR(500) NOT NULL,
    payload         TEXT,
    timestamp       TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_eventos_sistema      PRIMARY KEY (id_evento),
    CONSTRAINT fk_eventos_aula         FOREIGN KEY (id_aula)
        REFERENCES aulas (id_aula)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_eventos_tipo_evento  FOREIGN KEY (id_tipo_evento)
        REFERENCES tipo_de_evento (id_tipo_evento)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

COMMENT ON TABLE  eventos_sistema                IS 'Bitácora de todos los mensajes MQTT procesados por el API.';
COMMENT ON COLUMN eventos_sistema.id_evento      IS 'Identificador único del evento (PK, autoincremental).';
COMMENT ON COLUMN eventos_sistema.id_aula        IS 'FK hacia aulas.id_aula. Nullable si el evento no es de un aula específica.';
COMMENT ON COLUMN eventos_sistema.id_tipo_evento IS 'FK hacia tipo_de_evento.id_tipo_evento. Clasificación del evento.';
COMMENT ON COLUMN eventos_sistema.topico_mqtt    IS 'Topic MQTT completo del mensaje (ej. aulas/aula-1/light/state).';
COMMENT ON COLUMN eventos_sistema.payload        IS 'Contenido completo del mensaje en formato JSON.';
COMMENT ON COLUMN eventos_sistema.timestamp      IS 'Fecha y hora exacta en que el API procesó el mensaje.';


-- ============================================================
-- 11. acciones_dispositivo
--     Registro de cada acción ejecutada sobre un actuador.
--     Se escribe cuando Spring Boot recibe el mensaje /state
--     de un actuador con el campo "action" presente.
--     Permite auditar el historial completo de cambios.
-- ============================================================
CREATE TABLE acciones_dispositivo (
    id_accion        SERIAL       NOT NULL,
    id_dispositivo   INTEGER      NOT NULL,
    id_solicitud     INTEGER,
    accion           VARCHAR(100) NOT NULL,
    estado_anterior  VARCHAR(50),
    estado_nuevo     VARCHAR(50)  NOT NULL,
    timestamp        TIMESTAMP    NOT NULL DEFAULT NOW(),
    ejecutor         VARCHAR(100) NOT NULL DEFAULT 'EMULADOR',

    CONSTRAINT pk_acciones_dispositivo  PRIMARY KEY (id_accion),
    CONSTRAINT fk_acciones_dispositivo  FOREIGN KEY (id_dispositivo)
        REFERENCES dispositivos (id_dispositivo)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_acciones_solicitud    FOREIGN KEY (id_solicitud)
        REFERENCES solicitudes (id_solicitud)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_acciones_ejecutor
        CHECK (ejecutor IN ('EMULADOR', 'SYNC_AUTOMATICO', 'ADMIN'))
);

COMMENT ON TABLE  acciones_dispositivo                IS 'Registro de acciones ejecutadas sobre actuadores.';
COMMENT ON COLUMN acciones_dispositivo.id_accion      IS 'Identificador único de la acción (PK, autoincremental).';
COMMENT ON COLUMN acciones_dispositivo.id_dispositivo IS 'FK hacia dispositivos.id_dispositivo.';
COMMENT ON COLUMN acciones_dispositivo.id_solicitud   IS 'FK hacia solicitudes.id_solicitud. Nullable si fue acción manual.';
COMMENT ON COLUMN acciones_dispositivo.accion         IS 'Comando ejecutado (ej. TURN_OFF, CLOSE, DEPLOY, TURN_ON).';
COMMENT ON COLUMN acciones_dispositivo.estado_anterior IS 'Estado del dispositivo antes de la acción.';
COMMENT ON COLUMN acciones_dispositivo.estado_nuevo   IS 'Estado del dispositivo después de la acción.';
COMMENT ON COLUMN acciones_dispositivo.timestamp      IS 'Fecha y hora exacta de la acción.';
COMMENT ON COLUMN acciones_dispositivo.ejecutor       IS 'Quién ejecutó la acción: EMULADOR, SYNC_AUTOMATICO o ADMIN.';


-- ============================================================
-- ÍNDICES DE RENDIMIENTO
-- ============================================================

-- lecturas_lux: consultas por aula, causa y rango de tiempo
-- (la gráfica del frontend consulta por aula ordenado por timestamp)
CREATE INDEX idx_lecturas_aula    ON lecturas_lux      (id_aula);
CREATE INDEX idx_lecturas_causa   ON lecturas_lux      (id_causa);
CREATE INDEX idx_lecturas_ts      ON lecturas_lux      (timestamp DESC);
-- índice compuesto para el query más frecuente del frontend
CREATE INDEX idx_lecturas_aula_ts ON lecturas_lux      (id_aula, timestamp DESC);

-- eventos_sistema: filtros por aula, tipo y tiempo
CREATE INDEX idx_eventos_aula     ON eventos_sistema   (id_aula);
CREATE INDEX idx_eventos_tipo     ON eventos_sistema   (id_tipo_evento);
CREATE INDEX idx_eventos_ts       ON eventos_sistema   (timestamp DESC);

-- solicitudes: búsquedas por usuario, aula y estado
CREATE INDEX idx_solicitudes_usuario ON solicitudes    (id_usuario);
CREATE INDEX idx_solicitudes_aula    ON solicitudes    (id_aula);
CREATE INDEX idx_solicitudes_estado  ON solicitudes    (estado);

-- acciones_dispositivo: auditoría por dispositivo y solicitud
CREATE INDEX idx_acciones_disp    ON acciones_dispositivo (id_dispositivo);
CREATE INDEX idx_acciones_solic   ON acciones_dispositivo (id_solicitud);
CREATE INDEX idx_acciones_ts      ON acciones_dispositivo (timestamp DESC);

-- dispositivos: filtros por aula y tipo
CREATE INDEX idx_dispositivos_aula ON dispositivos     (id_aula);
CREATE INDEX idx_dispositivos_tipo ON dispositivos     (id_tipo);


-- ============================================================
-- DATOS SEMILLA — CATÁLOGOS
-- Estos datos son invariables. No deben modificarse sin
-- actualizar también el código Java correspondiente.
-- ============================================================

-- Roles
INSERT INTO roles (nombre_rol) VALUES
    ('Administrador'),
    ('Docente'),
    ('Técnico');

-- Tipos de dispositivo
-- CRÍTICO: estos nombres deben coincidir exactamente con los
-- valores del campo "tipo" en AbstractDeviceEmulator.java
-- y con los topics MQTT: aulas/{aulaId}/{nombre_tipo}/cmd|state
INSERT INTO tipos_dispositivo (nombre_tipo) VALUES
    ('lux_sensor'),   -- sensor de iluminancia
    ('light'),        -- luces del aula
    ('blind'),        -- persianas
    ('screen'),       -- telón de proyección
    ('projector'),    -- proyector
    ('monitor');      -- segunda pantalla / monitor auxiliar

-- Causas de cambio de lux
-- CRÍTICO: estos nombres deben coincidir exactamente con
-- los valores del enum LuxChangeCause.java
INSERT INTO causas_lux (nombre, descripcion) VALUES
    ('INITIAL',      'Lectura de arranque al iniciar el sistema, antes de cualquier acción'),
    ('LIGHTS_OFF',   'Transición de lux causada por apagado de luces del aula'),
    ('BLIND_CLOSED', 'Transición de lux causada por cierre de persianas'),
    ('STABLE',       'Lectura en estado estable, sin transición activa'),
    ('MANUAL',       'Cambio de lux forzado manualmente desde el panel de administración');

-- Tipos de evento del sistema
INSERT INTO tipo_de_evento (descripcion) VALUES
    ('solicitud_recibida'),   -- nueva solicitud de proyección registrada
    ('comando_enviado'),      -- Spring Boot publicó un comando MQTT a un emulador
    ('estado_actualizado'),   -- un actuador reportó cambio de estado
    ('lectura_sensor'),       -- snapshot de lux recibido del sensor
    ('error');                -- error en el procesamiento del flujo


-- ============================================================
-- DATOS OPERATIVOS INICIALES
-- Aulas y dispositivos que deben existir ANTES de arrancar
-- los emuladores Java. Sin estas filas, acciones_dispositivo
-- no puede escribirse por violación de FK.
-- ============================================================

-- Aulas del sistema
INSERT INTO aulas (ubicacion, estado) VALUES
    ('Edificio A - Aula 1', 'disponible'),
    ('Edificio A - Aula 2', 'disponible');

-- Dispositivos — 6 tipos × 2 aulas = 12 filas
-- El campo "nombre" usa el formato {tipo}-{aulaId}, que es
-- el mismo clientId con el que cada emulador se conecta a EMQX.
-- id_tipo referencia los valores insertados arriba:
--   1=lux_sensor, 2=light, 3=blind, 4=screen, 5=projector, 6=monitor
INSERT INTO dispositivos (id_aula, id_tipo, nombre, estado_actual) VALUES
    -- Aula 1
    (1, 1, 'lux_sensor-aula-1', 'UNKNOWN'),
    (1, 2, 'light-aula-1',      'OFF'),
    (1, 3, 'blind-aula-1',      'OPEN'),
    (1, 4, 'screen-aula-1',     'RETRACTED'),
    (1, 5, 'projector-aula-1',  'OFF'),
    (1, 6, 'monitor-aula-1',    'OFF'),
    -- Aula 2
    (2, 1, 'lux_sensor-aula-2', 'UNKNOWN'),
    (2, 2, 'light-aula-2',      'OFF'),
    (2, 3, 'blind-aula-2',      'OPEN'),
    (2, 4, 'screen-aula-2',     'RETRACTED'),
    (2, 5, 'projector-aula-2',  'OFF'),
    (2, 6, 'monitor-aula-2',    'OFF');

-- Usuario administrador de prueba
-- (en producción la contraseña iría hasheada en la columna correspondiente)
INSERT INTO usuarios (nombre, id_rol, estado) VALUES
    ('Administrador Sistema', 1, 'activo'),
    ('Docente Prueba',        2, 'activo');
