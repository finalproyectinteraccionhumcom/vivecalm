# ViveCalm

App de Android (Java + SQLite) de asistencia psicológica: registro emocional diario estilo Duolingo, diario, racha y directorio de psicólogos y psiquiatras.

## Cómo abrirlo

1. Descomprime el zip.
2. En Android Studio: **File › Open** y elige la carpeta `ViveCalm` (la que tiene `settings.gradle`).
3. Espera a que termine el *Gradle Sync* (la primera vez descarga dependencias, necesita internet).
4. Dale **Run ▶** con un emulador o tu celular conectado.

Requisitos: Android 8.0 (API 26) o superior. Si Android Studio te sugiere actualizar el Android Gradle Plugin, puedes aceptar.

## Pantallas

| Pantalla | Archivo | Qué hace |
|---|---|---|
| Bienvenida | `BienvenidaActivity` | Pide tu nombre la primera vez |
| Inicio | `InicioFragment` | Saludo, racha, últimos 7 días, reto y meta diaria, Línea de la Vida |
| Registro de emoción | `CheckInActivity` | 5 emociones, intensidad 1-5, temas y nota |
| Diario | `DiarioFragment` | Pregunta del día, texto libre, 3 agradecimientos y entradas anteriores |
| Progreso | `ProgresoFragment` | Racha, mejor racha, calendario del mes por color de emoción, puntos y logros |
| Respiración 4-7-8 | `RespiracionActivity` | Reto del día con animación y contador |
| Directorio | `ProfesionalesFragment` | Búsqueda y filtros (Psicología, Psiquiatría, En línea) |
| Perfil del profesional | `ProfesionalDetalleActivity` | Llamar, mensaje, correo y datos |
| Registro de profesional | `RegistroProfesionalActivity` | Formulario con validaciones |

## Estructura

```
app/src/main/java/com/vivecalm/app/
├── data/
│   ├── ViveCalmDbHelper.java   ← crea las tablas (SQLiteOpenHelper)
│   └── Repositorio.java        ← todas las consultas SQL
├── model/                      ← RegistroEmocion, EntradaDiario, Profesional
├── ui/                         ← activities, fragments y adaptador
└── util/                       ← Fechas, Emociones, Util
```

## Base de datos (`vivecalm.db`)

| Tabla | Columnas principales |
|---|---|
| `usuario` | id, nombre, fecha_registro |
| `emociones` | id, fecha, hora, emocion (0-4), intensidad (1-5), temas, nota |
| `diario` | id, fecha (única), pregunta, texto, gratitud1, gratitud2, gratitud3 |
| `actividades` | id, fecha, tipo (`respiracion`) |
| `profesionales` | id, nombre, profesion, cedula (única), especialidades, telefono, correo, ciudad, en_linea, presencial, horario, precio, descripcion, verificado, fecha_registro |

Emociones: 0 Feliz, 1 Tranqui, 2 Triste, 3 Ansioso, 4 Enojado.

**Racha:** días seguidos con al menos un registro de emoción. Si hoy todavía no registras pero ayer sí, la racha sigue viva.

**Puntos:** emoción +5, entrada de diario +10, respiración +10.

### Ver la base de datos mientras la app corre

En Android Studio: **View › Tool Windows › App Inspection › Database Inspector**. Ahí puedes ver las tablas y correr SQL.

Para "verificar" a un profesional registrado (en la app real esto lo haría un administrador revisando la cédula):

```sql
UPDATE profesionales SET verificado = 1 WHERE cedula = '12345678';
```

Los 3 profesionales que vienen al instalar la app son **de ejemplo** (dicen "(ejemplo)" en el nombre). Bórralos o cámbialos antes de presentar con datos reales:

```sql
DELETE FROM profesionales WHERE nombre LIKE '%(ejemplo)%';
```
