# NotificationController API Documentation

---

## 🇬🇧 English

### Overview
REST controller for handling user notification operations. All endpoints require mandatory JWT authentication. Users are strictly sandboxed to interact exclusively with their own notification records.

### Endpoints

#### 1. Get Notifications
Retrieves a paginated collection of notifications for the authenticated user. Includes optional filtering matrix by channel type, read status, and a dynamic historical day window. Any notifications scheduled with an availability date in the future are automatically hidden.

* **URL:** `/api/v1/notifications`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
    * `type` (optional): Entity type filter mapping (`LinkedType`).
    * `read` (optional): Evaluation flag for reading state (`true`/`false`).
    * `lastDays` (optional): Limits results to the last *N* days.
* **Responses:**
    * `200 OK`: Returns a structured `NotificationPageResponseDTO`.

#### 2. Get Pending Notifications
Retrieves a paginated list specifically targeted at unread or pending actions for the user. Excludes future notifications and allows optional categorization and date range constraints.

* **URL:** `/api/v1/notifications/pending`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
    * `type` (optional): Entity type filter mapping (`LinkedType`).
    * `lastDays` (optional): Limits results to the last *N* days.
* **Responses:**
    * `200 OK`: Returns a structured `NotificationPageResponseDTO`.

#### 3. Mark Notification as Read
Updates the read state configuration of a unique notification resource using its ID. Ownership verification is executed transparently before applying the change.

* **URL:** `/api/v1/notifications/{id}/read`
* **HTTP Method:** `PATCH`
* **Responses:**
    * `200 OK`: Returns status info wrapped inside a `MessageDTO`.

---

## 🇪🇸 Español

### Descripción General
Controlador REST dedicado a la gestión de las operaciones de notificación de los usuarios. Todos los endpoints requieren autenticación obligatoria mediante token JWT. Los usuarios están estrictamente limitados a interactuar únicamente con sus propios registros de notificación.

### Endpoints

#### 1. Obtener Notificaciones
Recupera una colección paginada de todas las notificaciones asociadas al usuario autenticado. Soporta una matriz de filtros opcionales por tipo de entidad, estado de lectura y un rango dinámico de días hacia atrás. Aquellas notificaciones cuya fecha de disponibilidad sea futura quedan excluidas automáticamente.

* **URL:** `/api/v1/notifications`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
    * `type` (opcional): Filtro por el tipo de entidad vinculada (`LinkedType`).
    * `read` (opcional): Filtro por estado de lectura (`true`/`false`).
    * `lastDays` (opcional): Restringe los resultados a los últimos *N* días.
* **Respuestas:**
    * `200 OK`: Retorna un objeto estructurado `NotificationPageResponseDTO`.

#### 2. Obtener Notificaciones Pendientes
Recupera una lista paginada orientada específicamente a las acciones pendientes o no leídas por el usuario. Excluye registros futuros y permite aplicar opcionalmente restricciones de categorías y rangos de días.

* **URL:** `/api/v1/notifications/pending`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
    * `type` (opcional): Filtro por el tipo de entidad vinculada (`LinkedType`).
    * `lastDays` (opcional): Restringe los resultados a los últimos *N* días.
* **Respuestas:**
    * `200 OK`: Retorna un objeto estructurado `NotificationPageResponseDTO`.

#### 3. Marcar Notificación como Leída
Modifica el estado de lectura de una notificación específica a través de su identificador único. Se valida rigurosamente la propiedad de la notificación antes de consolidar la edición.

* **URL:** `/api/v1/notifications/{id}/read`
* **Método HTTP:** `PATCH`
* **Respuestas:**
    * `200 OK`: Retorna la información del estado envuelta en un `MessageDTO`.