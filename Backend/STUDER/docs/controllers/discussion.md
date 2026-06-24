# DiscussionController API Documentation

---

## 🇬🇧 English

### Overview
REST controller handling public forums and discussion threads. Requires complete authentication via JWT tokens. It governs the management of open discussions, tag assignments, nested hierarchical comment threads, user favoriting mechanisms, and message-level like interactions.

### Endpoints

#### 1. Get Public Discussions
Retrieves a paginated list of global discussions. Results are dynamically sorted by recent activity metrics (messages posted within an adjustable hour window). Supports filtering by creation date constraints and specific tag arrays.

* **URL:** `/api/v1/discussions`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
    * `tags` (optional): Comma-separated listing of metadata tags.
    * `lastDays` (optional): Filters threads created within the last *N* days.
    * `activityHours` (default: `24`): Window configuration for recent activity sorting.
* **Responses:**
    * `200 OK`: Returns a structured `DiscussionPageResponseDTO`.

#### 2. Get User Participated Discussions
Retrieves paginated discussions where the calling identity has interacted directly (acting as the thread creator, an active commenter, or by favoriting it).

* **URL:** `/api/v1/discussions/me`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
* **Responses:**
    * `200 OK`: Returns a `DiscussionPageResponseDTO` embedded with contextual interaction profiles.

#### 3. Get Favorite Discussions
Fetches a paginated collection containing only the threads marked explicitly as bookmarks or favorites by the authenticated user.

* **URL:** `/api/v1/discussions/favourites`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
* **Responses:**
    * `200 OK`: Returns a `DiscussionPageResponseDTO`.

#### 4. Get Own Discussions
Returns a paginated list containing threads owned and published strictly by the authenticated caller.

* **URL:** `/api/v1/discussions/own`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
* **Responses:**
    * `200 OK`: Returns a `DiscussionPageResponseDTO`.

#### 5. Get Popular Discussions
Lists paginated discussion topics calculated and flagged as popular by the business service tier.

* **URL:** `/api/v1/discussions/popular`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
* **Responses:**
    * `200 OK`: Returns a `DiscussionPageResponseDTO`.

#### 6. Get New Discussions
Lists the latest public discussions ordered primarily by raw creation timestamps.

* **URL:** `/api/v1/discussions/new`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
* **Responses:**
    * `200 OK`: Returns a `DiscussionPageResponseDTO`.

#### 7. Get Discussion by ID
Fetches details of a specific discussion entry through its unique reference key.

* **URL:** `/api/v1/discussions/{id}`
* **HTTP Method:** `GET`
* **Responses:**
    * `200 OK`: Returns a single `DiscussionResponseDTO`.

#### 8. Create Discussion
Launches a new public thread topic. Provided tags are resolved dynamically (dynamically instantiated if missing from system catalogs).

* **URL:** `/api/v1/discussions`
* **HTTP Method:** `POST`
* **Request Body:** `DiscussionCreateRequestDTO` (Validated)
* **Responses:**
    * `200 OK`: Returns the instantiated `DiscussionResponseDTO`.

#### 9. Get Discussion Messages (Hierarchical)
Fetches comment logs belonging to a thread arranged in a hierarchical tree. Top-tier root comments are paginated, while child responses are deeply nested inside their parents. Elements are sorted by approval status (highest vote/like counts first).

* **URL:** `/api/v1/discussions/{id}/messages`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Root-level page index.
* **Responses:**
    * `200 OK`: Returns a tree-mapped `DiscussionMessagePageResponseDTO`.

#### 10. Post Message / Reply
Appends a fresh message entry to a topic thread or replies directly underneath an existing statement. Fails instantly if the parent thread status is locked or archived.

* **URL:** `/api/v1/discussions/{id}/messages`
* **HTTP Method:** `POST`
* **Request Body:** `DiscussionMessageCreateRequestDTO` (Validated)
* **Responses:**
    * `200 OK`: Returns a structured `DiscussionMessageResponseDTO`.

#### 11. Add Favorite
Saves a specific discussion entry into the user's personal favorites list.

* **URL:** `/api/v1/discussions/{id}/favourite`
* **HTTP Method:** `POST`
* **Responses:**
    * `200 OK`: Returns execution feedback inside a `MessageDTO`.

#### 12. Remove Favorite
Removes a specific discussion entry from the user's personal favorites list.

* **URL:** `/api/v1/discussions/{id}/favourite`
* **HTTP Method:** `DELETE`
* **Responses:**
    * `200 OK`: Returns execution feedback inside a `MessageDTO`.

#### 13. Like Message
Registers an upvote/like endorsement on a targeted response entry. Business logic permits exactly one transaction entry per identity.

* **URL:** `/api/v1/discussions/messages/{messageId}/like`
* **HTTP Method:** `POST`
* **Responses:**
    * `200 OK`: Returns execution feedback inside a `MessageDTO`.

#### 14. Unlike Message
Retracts a previously registered like endorsement from a targeted comment asset.

* **URL:** `/api/v1/discussions/messages/{messageId}/like`
* **HTTP Method:** `DELETE`
* **Responses:**
    * `200 OK`: Returns execution feedback inside a `MessageDTO`.

---

## 🇪🇸 Español

### Descripción General
Controlador REST diseñado para administrar foros públicos e hilos de discusión. Requiere autenticación obligatoria mediante tokens JWT. Gobierna la creación de debates abiertos, la gestión y mapeo de etiquetas (*tags*), la estructuración jerárquica de comentarios anidados, marcadores de favoritos y reacciones de me gusta a nivel de mensajes.

### Endpoints

#### 1. Obtener Discusiones Públicas
Recupera una lista paginada de discusiones globales. Los resultados se ordenan dinámicamente según métricas de actividad reciente (mensajes publicados en una ventana horaria parametrizable). Admite filtros por rango de días de creación y colecciones específicas de etiquetas.

* **URL:** `/api/v1/discussions`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
    * `tags` (opcional): Lista de etiquetas de metadatos.
    * `lastDays` (opcional): Filtra hilos creados en los últimos *N* días.
    * `activityHours` (por defecto: `24`): Ventana horaria de ordenación por actividad.
* **Respuestas:**
    * `200 OK`: Retorna un objeto estructurado `DiscussionPageResponseDTO`.

#### 2. Obtener Discusiones del Usuario (Participación)
Recupera las discusiones paginadas donde la identidad de la sesión ha interactuado directamente (ya sea como creador del hilo, comentador activo o marcándolo como favorito).

* **URL:** `/api/v1/discussions/me`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
* **Respuestas:**
    * `200 OK`: Retorna un `DiscussionPageResponseDTO` con perfiles de interacción contextuales.

#### 3. Obtener Discusiones Favoritas
Obtiene una colección paginada que contiene exclusivamente los hilos marcados de forma explícita como favoritos por el usuario autenticado.

* **URL:** `/api/v1/discussions/favourites`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
* **Respuestas:**
    * `200 OK`: Retorna un objeto `DiscussionPageResponseDTO`.

#### 4. Obtener Discusiones Propias
Devuelve una lista paginada que contiene únicamente los hilos iniciados y de propiedad estricta del usuario autenticado.

* **URL:** `/api/v1/discussions/own`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
* **Respuestas:**
    * `200 OK`: Retorna un objeto `DiscussionPageResponseDTO`.

#### 5. Obtener Discusiones Populares
Lista los temas de discusión paginados calculados y catalogados bajo criterios de popularidad por la capa de servicio de negocio.

* **URL:** `/api/v1/discussions/popular`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
* **Respuestas:**
    * `200 OK`: Retorna un objeto `DiscussionPageResponseDTO`.

#### 6. Obtener Discusiones Nuevas
Lista los hilos públicos más recientes ordenados principalmente por marcas de tiempo de creación cronológica.

* **URL:** `/api/v1/discussions/new`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de la página solicitada.
* **Respuestas:**
    * `200 OK`: Retorna un objeto `DiscussionPageResponseDTO`.

#### 7. Obtener Discusión por ID
Busca y expone los datos detallados de una discusión a través de su identificador numérico único.

* **URL:** `/api/v1/discussions/{id}`
* **Método HTTP:** `GET`
* **Respuestas:**
    * `200 OK`: Retorna una instancia de `DiscussionResponseDTO`.

#### 8. Crear Discusión
Inicia un nuevo tema de discusión pública. Las etiquetas enviadas son resueltas dinámicamente (se registran automáticamente si no existen previamente en los catálogos).

* **URL:** `/api/v1/discussions`
* **Método HTTP:** `POST`
* **Cuerpo de la Petición (Body):** `DiscussionCreateRequestDTO` (Validado)
* **Respuestas:**
    * `200 OK`: Retorna la entidad creada como `DiscussionResponseDTO`.

#### 9. Obtener Mensajes de la Discusión (Jerárquicos)
Obtiene los comentarios pertenecientes a un hilo organizados en un árbol jerárquico. Los comentarios raíz (principales) se entregan paginados, mientras que las respuestas hijas se anidan dentro de su respectivo padre. Se ordenan de forma descendente por popularidad (mensajes con más me gusta primero).

* **URL:** `/api/v1/discussions/{id}/messages`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice de página para los comentarios raíz.
* **Respuestas:**
    * `200 OK`: Retorna la estructura arbórea en un `DiscussionMessagePageResponseDTO`.

#### 10. Publicar Mensaje / Responder
Añade un nuevo mensaje a un hilo o responde a una intervención existente de manera anidada. Falla inmediatamente si el debate principal ha sido cerrado o archivado.

* **URL:** `/api/v1/discussions/{id}/messages`
* **Método HTTP:** `POST`
* **Cuerpo de la Petición (Body):** `DiscussionMessageCreateRequestDTO` (Validado)
* **Respuestas:**
    * `200 OK`: Retorna la estructura del mensaje como `DiscussionMessageResponseDTO`.

#### 11. Agregar Favorito
Guarda un registro de discusión específico dentro de la lista de favoritos personal del usuario autenticado.

* **URL:** `/api/v1/discussions/{id}/favourite`
* **Método HTTP:** `POST`
* **Respuestas:**
    * `200 OK`: Retorna el estado de la operación en un `MessageDTO`.

#### 12. Eliminar Favorito
Remueve un registro de discusión específico de la lista de favoritos personal del usuario autenticado.

* **URL:** `/api/v1/discussions/{id}/favourite`
* **Método HTTP:** `DELETE`
* **Respuestas:**
    * `200 OK`: Retorna el estado de la operación en un `MessageDTO`.

#### 13. Dar Me Gusta a un Mensaje
Registra una reacción positiva (*like*) sobre un comentario determinado. Las restricciones de negocio permiten estrictamente un solo registro por usuario.

* **URL:** `/api/v1/discussions/messages/{messageId}/like`
* **Método HTTP:** `POST`
* **Respuestas:**
    * `200 OK`: Retorna el estado de la operación en un `MessageDTO`.

#### 14. Quitar Me Gusta de un Mensaje
Retira un voto positivo registrado previamente sobre un comentario específico.

* **URL:** `/api/v1/discussions/messages/{messageId}/like`
* **Método HTTP:** `DELETE`
* **Respuestas:**
    * `200 OK`: Retorna el estado de la operación en un `MessageDTO`.