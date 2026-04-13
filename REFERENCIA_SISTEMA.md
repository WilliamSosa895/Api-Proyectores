# ══════════════════════════════════════════════════════════════════════
# SISTEMA AUTOMATIZADO DE GESTIÓN DE PROYECTORES EN AULAS
# Documento de referencia: Estructura del API + Tabla de Topics MQTT
# ══════════════════════════════════════════════════════════════════════


# ──────────────────────────────────────────────────────────────────────
# 1. ESTRUCTURA DEL PROYECTO API (Spring Boot)
# ──────────────────────────────────────────────────────────────────────

api-proyectores/
├── pom.xml
└── src/main/
    ├── java/com/proyecto/api/
    │   │
    │   ├── ApiProyectoresApplication.java   ← main(), @EnableAsync
    │   │
    │   ├── config/
    │   │   ├── MqttConfig.java              ← Bean MqttClient conectado a EMQX
    │   │   └── WebSocketConfig.java         ← STOMP sobre WebSocket para React
    │   │
    │   ├── mqtt/
    │   │   ├── MqttSubscriberService.java   ← suscribe a aulas/+/+/state, enruta mensajes
    │   │   └── MqttPublisherService.java    ← publica comandos a aulas/{aulaId}/{tipo}/cmd
    │   │
    │   ├── model/
    │   │   ├── Aula.java                    ← tabla aulas
    │   │   ├── TipoDispositivo.java         ← tabla tipos_dispositivo
    │   │   ├── Dispositivo.java             ← tabla dispositivos
    │   │   ├── Solicitud.java               ← tabla solicitudes
    │   │   ├── LecturaLux.java              ← tabla lecturas_lux (incluye campo causa)
    │   │   └── AccionDispositivo.java       ← tabla acciones_dispositivo
    │   │
    │   ├── repository/
    │   │   ├── AulaRepository.java
    │   │   ├── DispositivoRepository.java   ← findByIdAulaAndTipoNombre()
    │   │   ├── SolicitudRepository.java
    │   │   ├── LecturaLuxRepository.java    ← findTopByIdAulaOrderByTimestampDesc()
    │   │   └── AccionDispositivoRepository.java
    │   │
    │   ├── service/
    │   │   ├── SolicitudService.java        ← orquesta el flujo completo de proyección
    │   │   ├── LuxService.java              ← persiste snapshots de lux en lecturas_lux
    │   │   └── DispositivoService.java      ← actualiza estado en dispositivos y acciones
    │   │
    │   ├── controller/
    │   │   ├── AulaController.java          ← GET /api/aulas
    │   │   ├── SolicitudController.java     ← POST /api/solicitudes
    │   │   └── DispositivoController.java   ← GET /api/aulas/{id}/dispositivos
    │   │
    │   └── websocket/
    │       └── WebSocketEventPublisher.java ← reenvía estados MQTT → React vía STOMP
    │
    └── resources/
        └── application.properties


# ──────────────────────────────────────────────────────────────────────
# 2. RESPONSABILIDADES POR CAPA
# ──────────────────────────────────────────────────────────────────────

# MqttSubscriberService
#   - Suscribe al wildcard "aulas/+/+/state" (cubre todos los dispositivos de todas las aulas)
#   - Extrae aulaId y tipo del topic recibido
#   - Si tipo = lux_sensor → llama LuxService.procesarSnapshot()
#   - Si tipo = actuador   → llama DispositivoService.actualizarEstado()
#   - Ambos services llaman a WebSocketEventPublisher para reenviar a React

# MqttPublisherService
#   - Expone métodos de conveniencia: apagarLuces(), bajarPersianas(), encenderProyector(), etc.
#   - SolicitudService lo llama durante el flujo de proyección
#   - Construye el topic "aulas/{aulaId}/{tipo}/cmd" y publica {"action": "..."}

# SolicitudService (@Async)
#   - POST /api/solicitudes → registra solicitud → responde inmediatamente con idSolicitud
#   - Lanza ejecutarFlujo() en hilo separado (no bloquea HTTP)
#   - Flujo: leer lux BD → si >100: apagar luces → esperar 4s → leer lux BD
#            → si >100: bajar persianas → esperar 4s → leer lux BD
#            → bajar telón → encender proyector → marcar COMPLETADA


# ──────────────────────────────────────────────────────────────────────
# 3. TABLA COMPLETA DE TOPICS MQTT
# ──────────────────────────────────────────────────────────────────────
# Patrón general: aulas/{aulaId}/{tipo}/{cmd|state}
# QoS: 1 en todos los topics
# retained=true en todos los /state   (suscriptores nuevos reciben el último estado)
# retained=false en todos los /cmd    (si el emulador no está, el comando se descarta)
# ──────────────────────────────────────────────────────────────────────

# ┌─────────────────────────────────┬──────────────────────┬──────────────────────────────┐
# │ TOPIC                           │ PUBLICA              │ SUSCRIBE                     │
# ├─────────────────────────────────┼──────────────────────┼──────────────────────────────┤
# │ aulas/{aulaId}/lux_sensor/state │ LuxSensorEmulator    │ Spring Boot                  │
# │ aulas/{aulaId}/lux_sensor/cmd   │ Spring Boot (admin)  │ LuxSensorEmulator            │
# │ aulas/{aulaId}/light/state      │ LightEmulator        │ Spring Boot                  │
# │ aulas/{aulaId}/light/cmd        │ Spring Boot          │ LightEmulator                │
# │ aulas/{aulaId}/blind/state      │ BlindEmulator        │ Spring Boot                  │
# │ aulas/{aulaId}/blind/cmd        │ Spring Boot          │ BlindEmulator                │
# │ aulas/{aulaId}/screen/state     │ ScreenEmulator       │ Spring Boot                  │
# │ aulas/{aulaId}/screen/cmd       │ Spring Boot          │ ScreenEmulator               │
# │ aulas/{aulaId}/projector/state  │ ProjectorEmulator    │ Spring Boot + MonitorEmulator│
# │ aulas/{aulaId}/projector/cmd    │ Spring Boot          │ ProjectorEmulator            │
# │ aulas/{aulaId}/monitor/state    │ MonitorEmulator      │ Spring Boot                  │
# │ aulas/{aulaId}/monitor/cmd      │ Spring Boot (admin)  │ MonitorEmulator              │
# └─────────────────────────────────┴──────────────────────┴──────────────────────────────┘

# Nota: MonitorEmulator suscribe a DOS topics simultáneamente:
#   - aulas/{aulaId}/monitor/cmd      ← sus propios comandos
#   - aulas/{aulaId}/projector/state  ← para sincronizarse automáticamente con el proyector


# ──────────────────────────────────────────────────────────────────────
# 4. PAYLOADS DE ESTADO — lo que publican los emuladores en /state
# ──────────────────────────────────────────────────────────────────────

# lux_sensor/state
# {
#   "aulaId":    "aula-1",
#   "id":        "lux_sensor-aula-1",
#   "luxValue":  87,
#   "cause":     "LIGHTS_OFF",   ← INITIAL | LIGHTS_OFF | BLIND_CLOSED | STABLE | MANUAL
#   "timestamp": 1719000000000
# }

# light/state · blind/state · screen/state
# {
#   "id":            "light-aula-1",
#   "aulaId":        "aula-1",
#   "tipo":          "light",
#   "state":         "OFF",
#   "previousState": "ON",
#   "action":        "TURN_OFF",
#   "timestamp":     1719000000000,
#   "executor":      "EMULADOR"
# }

# projector/state  (payload extendido — MonitorEmulator lo lee)
# {
#   "id":        "projector-aula-1",
#   "aulaId":    "aula-1",
#   "tipo":      "projector",
#   "state":     "ON",
#   "isOn":      true,
#   "input":     "HDMI",
#   "action":    "TURN_ON",
#   "timestamp": 1719000000000,
#   "executor":  "EMULADOR"
# }

# monitor/state  (incluye campo de sincronización)
# {
#   "id":                  "monitor-aula-1",
#   "aulaId":              "aula-1",
#   "tipo":                "monitor",
#   "state":               "ON",
#   "isOn":                true,
#   "syncedWithProjector": true,
#   "input":               "HDMI",
#   "timestamp":           1719000000000,
#   "executor":            "SYNC_AUTOMATICO"   ← o "EMULADOR" si fue manual
# }


# ──────────────────────────────────────────────────────────────────────
# 5. PAYLOADS DE COMANDO — lo que Spring Boot publica en /cmd
# ──────────────────────────────────────────────────────────────────────

# light/cmd
#   { "action": "TURN_ON" }
#   { "action": "TURN_OFF" }

# blind/cmd
#   { "action": "CLOSE" }
#   { "action": "OPEN" }

# screen/cmd
#   { "action": "DEPLOY" }
#   { "action": "RETRACT" }

# projector/cmd
#   { "action": "TURN_ON" }
#   { "action": "TURN_OFF" }
#   { "action": "SET_INPUT:HDMI" }
#   { "action": "SET_INPUT:VGA" }
#   { "action": "SET_INPUT:WIRELESS" }

# monitor/cmd
#   { "action": "TURN_ON" }
#   { "action": "TURN_OFF" }
#   { "action": "UNSYNC" }       ← desvincula del proyector sin apagar

# lux_sensor/cmd  (solo para pruebas y panel admin)
#   { "action": "SET_LUX:150" }  ← fuerza un valor específico


# ──────────────────────────────────────────────────────────────────────
# 6. ENDPOINTS REST DEL API
# ──────────────────────────────────────────────────────────────────────

# GET  /api/aulas                          → lista todas las aulas
# GET  /api/aulas/{id}                     → detalle de un aula
# GET  /api/aulas/{id}/dispositivos        → estado actual de los 6 dispositivos del aula
# POST /api/solicitudes                    → inicia el flujo de proyección
#      body: { "idAula": 1, "idUsuario": 3 }
#      response: { "idSolicitud": 7, "estado": "PROCESANDO" }
# GET  /api/solicitudes/{id}               → consulta el estado de una solicitud


# ──────────────────────────────────────────────────────────────────────
# 7. TOPICS WEBSOCKET — lo que React escucha en tiempo real
# ──────────────────────────────────────────────────────────────────────

# Conexión React → Spring Boot:
#   ws://192.168.1.10:8080/ws   (con SockJS)

# Destinos STOMP que React suscribe:
#   /topic/aulas/{aulaId}/lux          ← cada snapshot de lux (gráfica en tiempo real)
#   /topic/aulas/{aulaId}/dispositivos ← estado de cualquier actuador al cambiar


# ──────────────────────────────────────────────────────────────────────
# 8. CONEXIONES DE RED POR NODO
# ──────────────────────────────────────────────────────────────────────

# Nodo 1 — Servidor (192.168.1.10)
#   Docker: EMQX      → puertos 1883 (TCP), 8083 (WS), 18083 (dashboard)
#   Docker: PostgreSQL → puerto 5432
#   Spring Boot API   → puerto 8080

# Nodo 2 — Emuladores (192.168.1.30)
#   JVM: EmulatorLauncher → conecta a tcp://192.168.1.10:1883

# Nodo 3 — Cliente (navegador)
#   React → HTTP  → 192.168.1.10:8080  (REST)
#   React → WS    → 192.168.1.10:8080/ws  (WebSocket STOMP)
#   (React NO se conecta directamente a EMQX)
