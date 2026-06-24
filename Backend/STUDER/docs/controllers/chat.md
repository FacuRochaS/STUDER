# ChatController API Documentation

---

## 🇬🇧 English

### Overview
REST controller for managing chat operations. It provides endpoints to retrieve chat lists, fetch historical messages, send new messages (supporting text and media files), and manage message read statuses for the authenticated user.

### Endpoints

#### 1. Get User Chats
Retrieves a summary list of all conversations/chats belonging to the currently authenticated user.

* **URL:** `/api/v1/chats`
* **HTTP Method:** `GET`
* **Responses:**
  * `200 OK`: Returns a `List<ChatSummaryDTO>`.

#### 2. Get Chat Messages
Fetches a paginated list of messages from a specific chat, ordered chronologically (handled descending in repository).

* **URL:** `/api/v1/chats/{chatId}/messages`
* **HTTP Method:** `GET`
* **Query Parameters:**
  * `page` (default: `0`): The page index to retrieve.
  * `size` (default: `20`): The number of messages per page.
* **Responses:**
  * `200 OK`: Returns a `Page<MessageResponseDTO>`.

#### 3. Send Message to User
Initiates a new chat or sends a message directly to a target user using their unique ID. Supports multipart data for optional file attachments.

* **URL:** `/api/v1/chats/user/{targetUserId}`
* **HTTP Method:** `POST`
* **Headers:** `Content-Type: multipart/form-data`
* **Request Parts:**
  * `request` (`MessageRequestDTO`): Text content and metadata (Validated).
  * `file` (`MultipartFile`, optional): Binary file/media payload.
* **Responses:**
  * `200 OK`: Returns the sent message details as `MessageResponseDTO`.

#### 4. Send Message to Chat
Sends a message to an existing chat channel. Supports multipart data for optional file attachments.

* **URL:** `/api/v1/chats/{chatId}/messages`
* **HTTP Method:** `POST`
* **Headers:** `Content-Type: multipart/form-data`
* **Request Parts:**
  * `request` (`MessageRequestDTO`): Text content and metadata (Validated).
  * `file` (`MultipartFile`, optional): Binary file/media payload.
* **Responses:**
  * `200 OK`: Returns the sent message details as `MessageResponseDTO`.

#### 5. Mark Chat as Read
Marks all unread messages within a specific chat room as read for the current user.

* **URL:** `/api/v1/chats/{chatId}/read`
* **HTTP Method:** `PUT`
* **Responses:**
  * `204 No Content`: Chat successfully marked as read.

---

## 🇪🇸 Español

### Descripción General
Controlador REST para gestionar las operaciones de chat. Proporciona endpoints para recuperar listas de chats, obtener el historial de mensajes, enviar nuevos mensajes (con soporte para texto y archivos multimedia) y gestionar el estado de lectura de los mensajes para el usuario autenticado.

### Endpoints

#### 1. Obtener Chats del Usuario
Recupera una lista resumen de todas las conversaciones o chats que pertenecen al usuario actualmente autenticado.

* **URL:** `/api/v1/chats`
* **Método HTTP:** `GET`
* **Respuestas:**
  * `200 OK`: Retorna un `List<ChatSummaryDTO>`.

#### 2. Obtener Mensajes de un Chat
Obtiene una lista paginada de mensajes de un chat específico, ordenados cronológicamente (manejado de forma descendente en el repositorio).

* **URL:** `/api/v1/chats/{chatId}/messages`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
  * `page` (por defecto: `0`): Índice de la página a recuperar.
  * `size` (por defecto: `20`): Cantidad de mensajes por página.
* **Respuestas:**
  * `200 OK`: Retorna un `Page<MessageResponseDTO>`.

#### 3. Enviar Mensaje a un Usuario
Inicia un nuevo chat o envía un mensaje directamente a un usuario específico mediante su ID. Soporta datos multipart para adjuntar archivos de forma opcional.

* **URL:** `/api/v1/chats/user/{targetUserId}`
* **Método HTTP:** `POST`
* **Cabeceras:** `Content-Type: multipart/form-data`
* **Partes de la Petición (Request Parts):**
  * `request` (`MessageRequestDTO`): Contenido de texto y metadatos (Validado).
  * `file` (`MultipartFile`, opcional): Archivo binario o multimedia adjunto.
* **Respuestas:**
  * `200 OK`: Retorna los detalles del mensaje enviado como `MessageResponseDTO`.

#### 4. Enviar Mensaje a un Chat
Envía un mensaje a una sala de chat ya existente. Soporta datos multipart para adjuntar archivos de forma opcional.

* **URL:** `/api/v1/chats/{chatId}/messages`
* **Método HTTP:** `POST`
* **Cabeceras:** `Content-Type: multipart/form-data`
* **Partes de la Petición (Request Parts):**
  * `request` (`MessageRequestDTO`): Contenido de texto y metadatos (Validado).
  * `file` (`MultipartFile`, opcional): Archivo binario o multimedia adjunto.
* **Respuestas:**
  * `200 OK`: Retorna los detalles del mensaje enviado como `MessageResponseDTO`.

#### 5. Marcar Chat como Leído
Marca todos los mensajes no leídos dentro de un chat específico como leídos por el usuario actual.

* **URL:** `/api/v1/chats/{chatId}/read`
* **Método HTTP:** `PUT`
* **Respuestas:**
  * `204 No Content`: Chat marcado como leído exitosamente.