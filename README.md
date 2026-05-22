# IRIS IDS — App Android

Sistema de control de asistencia biométrico por reconocimiento facial. Los inspectores fichan mediante su rostro en tablets Android distribuidas en campo. El administrador gestiona el sistema desde un panel web conectado al mismo backend.

**Versión actual:** 1.0.0 | **minSdk:** 28 (Android 9) | **targetSdk:** 36

---

## Características

- **Reconocimiento facial real** con ML Kit Face Detection on-device
- **Liveness detection** — el inspector debe parpadear y girar la cabeza para evitar fotos impostoras
- **Offline-first** — los eventos se guardan localmente en Room y se sincronizan al recuperar conexión
- **6 tipos de evento** — Entrada, Salida, Descanso entrada/salida, Almuerzo entrada/salida
- **Registro único por evento** — cada tipo de evento solo puede marcarse una vez por jornada
- **URL configurable** — el servidor se puede cambiar sin recompilar la APK
- **Sesión persistente** — la sesión del inspector dura 24h y sobrevive reinicios de la app

---

## Hardware objetivo

| Dispositivo | Especificación |
|-------------|---------------|
| Tablet | Lenovo Tab M10 |
| Android | 9.0 (API 28) o superior |
| Cámara | Frontal obligatoria para reconocimiento facial |
| Conectividad | Wi-Fi para sincronización con el backend |

---

## Stack técnico

| Capa | Tecnología |
|------|-----------|
| UI | Kotlin + Jetpack Compose + Material 3 |
| Arquitectura | Clean Architecture (Domain / Data / Presentation) |
| Inyección de dependencias | Hilt |
| Navegación | Navigation Compose |
| Cámara | CameraX |
| Biometría on-device | ML Kit Face Detection |
| Red | Retrofit + OkHttp |
| Persistencia local | Room + DataStore + EncryptedSharedPreferences |
| Sync offline | WorkManager (cada 15 minutos, solo con red) |
| Asíncrono | Coroutines + Flow |

---

## Arquitectura

```
Presentation (ui/)
    screens/          → Una carpeta por pantalla, con Screen.kt y ViewModel.kt
    components/       → Composables reutilizables (ActionRow, TimelineEntry, etc.)
    theme/            → Design tokens, tipografía, IrisTheme Material 3

Domain (domain/)
    model/            → Entidades de negocio puras (sin dependencias Android)
    repository/       → Interfaces (contratos)
    usecase/          → Lógica de negocio

Data (data/)
    local/db/         → Room: Database, DAOs, Entities
    local/prefs/      → DataStore, EncryptedSharedPrefs
    remote/api/       → Retrofit interfaces
    remote/dto/       → Request / Response data classes
    remote/interceptor/ → Auth, DeviceId, BaseUrl interceptors
    repository/       → Implementaciones de los repositorios
```

**Regla de dependencia:** Domain no depende de nada. Data implementa Domain. Presentation consume Domain.

---

## Pantallas

| Pantalla | Descripción |
|----------|-------------|
| **SplashScreen** | Verifica sesión activa y conectividad con el servidor |
| **FaceLoginScreen** | Captura facial con liveness → identifica al inspector |
| **HomeScreen** | Dashboard del inspector — estado de jornada, eventos del día, historial |
| **ChooseScreen** | Selección del tipo de evento a registrar |
| **CaptureScreen** | Captura facial con liveness para confirmar el evento |
| **SuccessScreen** | Confirmación animada del evento registrado |
| **HistoryScreen** | Historial completo agrupado por día |
| **EnrollScreen** | Registro de nuevo inspector (solo desde el panel admin) |
| **AdminLoginScreen** | Login del administrador |
| **AdminDashboard** | Panel de administración — inspectores y configuración |

---

## Flujo de una jornada

```
SplashScreen
    └── FaceLoginScreen  (liveness: parpadeo + giro cabeza izquierda + derecha)
            └── HomeScreen
                    └── ChooseScreen
                            ├── Entrada (IN)      → CaptureScreen (liveness) → SuccessScreen
                            ├── Descanso (BREAK_OUT / BREAK_IN)  → CaptureScreen → SuccessScreen
                            ├── Almuerzo (FOOD_OUT / FOOD_IN)    → CaptureScreen → SuccessScreen
                            └── Salida (OUT)      → CaptureScreen (liveness) → SplashScreen
```

Cada tipo de evento solo puede marcarse **una vez por jornada**. Una vez registrado, queda bloqueado permanentemente en la pantalla de selección.

---

## Comportamiento offline

1. El evento se guarda inmediatamente en Room (base de datos local) con `isSynced = false`
2. La app intenta enviarlo al backend en ese momento
3. Si no hay conexión, el `SyncWorker` lo reintenta cada 15 minutos cuando hay red disponible
4. El historial local se muestra aunque no haya conexión
5. Los datos nunca se borran de Room — son el historial permanente local

---

## Compilar e instalar

### Requisitos

- Android Studio Hedgehog o superior
- JDK 11+
- El backend corriendo (ver [IRIS IDS Backend](../IRIS_IDS_BACK/README.md))

### Pasos

1. Abre el proyecto en Android Studio: `File → Open → selecciona la carpeta IRIS_IDS`
2. Espera a que Gradle sincronice las dependencias
3. Conecta la tablet Lenovo Tab M10 por USB con depuración USB activada
4. Pulsa **Run ▶** o usa `Shift + F10`

Para generar la APK de distribución:

```
Build → Generate Signed App Bundle / APK → APK → release
```

---

## Configurar la URL del servidor

La URL del backend **no está hardcodeada** — se configura desde la propia app:

1. En la pantalla `AdminLoginScreen`, toca el ícono **⚙** (arriba a la derecha)
2. Ingresa la URL del servidor:
   - **Desarrollo local:** `http://192.168.X.X:8090/` (IP del PC en la misma red Wi-Fi)
   - **Producción (VPS):** `https://api.tudominio.com/`
3. Toca **"Probar conexión"** — el indicador debe ponerse verde
4. Toca **"Guardar"**

Este ajuste persiste entre reinicios de la app y aplica a todas las tablets individualmente.

> En producción con HTTPS, se puede eliminar `android:usesCleartextTraffic="true"`
> del `AndroidManifest.xml` para mayor seguridad.

---

## Design tokens

| Token | Color | Uso |
|-------|-------|-----|
| `IrisBg` | `#ECEEF1` | Fondo principal |
| `IrisSurface` | `#FFFFFF` | Tarjetas y superficies |
| `IrisInk` | `#1A1F2B` | Texto principal |
| `IrisPrimary` | `#102A4C` | Azul marino — entradas, botones primarios |
| `IrisSalida` | `#D96B1F` | Naranja — salidas y pausas |
| `IrisDone` | `#2E7D52` | Verde — eventos completados |
| `IrisDanger` | `#B03A2E` | Rojo — errores |
| `IrisCameraBg` | `#0D1117` | Fondo de pantallas de cámara |

---

## Historial de versiones

### v1.0.0 — 2026-05-22

Primera versión estable con integración completa con el backend.

**Incluye:**
- Reconocimiento facial real con InsightFace buffalo_l (vía backend)
- Liveness detection on-device con ML Kit (parpadeo + giro de cabeza)
- 6 tipos de evento de asistencia — registro único por jornada
- Arquitectura offline-first con Room + WorkManager
- URL del servidor configurable sin recompilar
- Panel de administración accesible desde la misma app
- Sincronización automática de eventos pendientes cada 15 minutos
- Historial de asistencia por día con acceso offline
- Soporte completo para Lenovo Tab M10 (pantalla ~800dp)

---

## Repositorio del backend

El backend (Go API + Python biométrico + PostgreSQL + Redis + Panel React) está en un repositorio separado. Consulta su `README.md` para instrucciones de despliegue local y en VPS.
