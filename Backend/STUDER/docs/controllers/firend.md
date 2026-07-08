# FriendController API Documentation

---

## 🇬🇧 English

### Overview
REST controller for handling social connections, friend requests, and following operations. All endpoints require the user to be fully authenticated via a JWT token. It allows users to follow/add others, remove connections, view confirmed friends, and inspect relationship states.

### Endpoints

#### 1. Follow User (Request Body)
Sends a friend request or follows another user using a request payload. This action triggers a `USER` type notification for the recipient.

* **URL:** `/api/v1/friends/follow`
* **HTTP Method:** `POST`
* **Request Body:** `FollowRequestDTO` (Valid JSON with target user ID)
* **Responses:**
    * `200 OK`: Returns a `FriendResponseDTO` containing updated status.

#### 2. Follow User (Path Variable)
Alternative or fallback mechanism to follow/request a user using a path parameter instead of a JSON request body.

* **URL:** `/api/v1/friends/follow/{userId}`
* **HTTP Method:** `POST`
* **Responses:**
    * `200 OK`: Returns a `FriendResponseDTO` containing updated status.

#### 3. Unfollow User / Cancel Request
Unfollows an active contact or cancels a pending friend request by changing relation flags to inactive.

* **URL:** `/api/v1/friends/follow/{userId}`
* **HTTP Method:** `DELETE`
* **Responses:**
    * `200 OK`: Returns a localized response wrapper (`MessageDTO`). Key fields reflect `friend.unfollow_success` or `friend.not_found`.

#### 4. Get Confirmed Friends List
Retrieves a paginated collection of confirmed mutual friendships. This dataset lists individuals with whom the caller shares a symmetric, accepted bond—ideal for messaging or list assembly.

* **URL:** `/api/v1/friends`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `page` (default: `0`): Target page index.
* **Responses:**
    * `200 OK`: Returns a paginated wrapper `FriendsListResponseDTO`.

#### 5. Get Relationship Status
Checks the absolute context state of a connection against a specific target user (e.g., pending, accepted, blocked, or non-existent).

* **URL:** `/api/v1/friends/status/{userId}`
* **HTTP Method:** `GET`
* **Responses:**
    * `200 OK`: Returns a discrete `FriendStatusResponseDTO`.

---

## 🇪🇸 Español

### Descripción General
Controlador REST encargado de las interacciones sociales, solicitudes de amistad y el seguimiento de usuarios. Todos los endpoints exigen una autenticación previa obligatoria por medio de un token JWT. Facilita los flujos para entablar lazos relacionales, eliminar conexiones, listar amigos mutuos y consultar estados relacionales en tiempo real.

### Endpoints

#### 1. Seguir Usuario / Enviar Solicitud (Por Body)
Envía una solicitud de amistad o comienza a seguir a otro miembro mediante una estructura JSON. Esta operación despacha de forma reactiva una notificación interna de tipo `USER`.

* **URL:** `/api/v1/friends/follow`
* **Método HTTP:** `POST`
* **Cuerpo de la Petición (Body):** `FollowRequestDTO` (Debe ser estructurado y válido)
* **Respuestas:**
    * `200 OK`: Retorna un `FriendResponseDTO` con las propiedades actualizadas.

#### 2. Seguir Usuario / Enviar Solicitud (Por Path Variable)
Mecanismo alternativo o de respaldo diseñado para efectuar la acción de seguimiento a través de una variable en la ruta URL, evitando el uso de cuerpos de mensajes.

* **URL:** `/api/v1/friends/follow/{userId}`
* **Método HTTP:** `POST`
* **Respuestas:**
    * `200 OK`: Retorna un `FriendResponseDTO` con las propiedades de la relación.

#### 3. Dejar de Seguir / Cancelar Solicitud
Anula una conexión existente o cancela una invitación de amistad que esté en curso, modificando internamente los marcadores de aceptación a inactivos.

* **URL:** `/api/v1/friends/follow/{userId}`
* **Método HTTP:** `DELETE`
* **Respuestas:**
    * `200 OK`: Retorna un `MessageDTO` cuyo mensaje interno indicará `friend.unfollow_success` o `friend.not_found`.

#### 4. Obtener Lista de Amigos Confirmados
Recupera de manera paginada el catálogo de amistades directas y consolidadas (donde ambos participantes han brindado su aceptación explícita). Utilizado con frecuencia para flujos internos como el inicio de nuevos hilos de chat.

* **URL:** `/api/v1/friends`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `page` (por defecto: `0`): Índice numérico de la página solicitada.
* **Respuestas:**
    * `200 OK`: Retorna la colección estructurada en un `FriendsListResponseDTO`.

#### 5. Consultar Estado de la Relación
Inspecciona el escenario actual de vinculación frente a un usuario objetivo específico (por ejemplo, si la relación está pendiente, aceptada, o es inexistente).

* **URL:** `/api/v1/friends/status/{userId}`
* **Método HTTP:** `GET`
* **Respuestas:**
    * `200 OK`: Retorna un `FriendStatusResponseDTO` con el estado detallado.