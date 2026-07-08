# UserController API Documentation

---

## 🇬🇧 English

### Overview
REST controller dedicated to user management operations. It handles core CRUD features while deferring actual token and session generation to the dedicated `AuthController`. To ensure robust privacy, strict sandbox boundaries are enforced: users can only modify, query private endpoints, or soft-delete their own accounts based on the context extracted from their JWT token.

### Endpoints

#### 1. Register User
Registers a new user record within the system database. This is a public initialization endpoint.

* **URL:** `/api/v1/users/register`
* **HTTP Method:** `POST`
* **Request Body:** `UserCreateRequestDTO` (Validated payload)
* **Responses:**
    * `200 OK`: Registration successful. Returns a complete `UserResponseDTO`.

#### 2. Update User Profile
Updates details of the currently authenticated session owner. It accepts multipart data payloads to support structural text edits alongside optional profile file uploads.

* **URL:** `/api/v1/users`
* **HTTP Method:** `PUT`
* **Headers:** `Content-Type: multipart/form-data`
* **Request Parts:**
    * `request` (`UserUpdateRequestDTO`): Structured textual fields for updates (Validated).
    * `file` (`MultipartFile`, optional): Binary profile picture or media file asset.
* **Responses:**
    * `200 OK`: Record modified successfully. Returns an updated `UserResponseDTO`.

#### 3. Soft Delete User
Performs a soft-deletion scheme on the caller's own user record, marking it inactive or archived without removing the raw record immediately.

* **URL:** `/api/v1/users`
* **HTTP Method:** `DELETE`
* **Responses:**
    * `200 OK`: Account successfully flag-deleted. Returns a `UserResponseDTO`.

#### 4. Get Current User Data
Retrieves the complete, detailed profile payload belonging strictly to the currently authenticated identity.

* **URL:** `/api/v1/users/me`
* **HTTP Method:** `GET`
* **Responses:**
    * `200 OK`: Returns an authoritative `UserResponseDTO`.

#### 5. Get Public Profile by Username
Retrieves the stripped-down, safe public profile representation of a user searched by their handle.

* **URL:** `/api/v1/users/username/{username}`
* **HTTP Method:** `GET`
* **Responses:**
    * `200 OK`: Returns restricted fields mapped as `UserPublicResponseDTO`.

#### 6. Search Users
Searches through active profiles by typing partial matching terms against user handles. Results are distributed using traditional index pagination blocks.

* **URL:** `/api/v1/users/search`
* **HTTP Method:** `GET`
* **Query Parameters:**
    * `query` (required): Text search criteria.
    * `page` (default: `0`): Target page index.
    * `size` (default: `10`): Max results returned per slice.
* **Responses:**
    * `200 OK`: Returns a structured `UserSearchPageResponseDTO`.

#### 7. Get User by ID
Fetches standard information about a target user identifier. Access rules constrain information strictly to public-facing schemas.

* **URL:** `/api/v1/users/{id}`
* **HTTP Method:** `GET`
* **Responses:**
    * `200 OK`: Returns restricted fields mapped as `UserPublicResponseDTO`.

---

## 🇪🇸 Español

### Descripción General
Controlador REST dedicado a la gestión y ciclo de vida de los perfiles de usuario. Administra los flujos operacionales CRUD esenciales, delegando los mecanismos de autenticación y generación de sesiones al `AuthController`. Para preservar la confidencialidad de la información, se aplican límites estrictos: los usuarios solo pueden modificar, consultar datos privados o aplicar bajas lógicas sobre sus propios registros mapeados mediante el token JWT.

### Endpoints

#### 1. Registrar Usuario
Registra un nuevo usuario dentro del ecosistema del sistema. Es un endpoint de acceso público.

* **URL:** `/api/v1/users/register`
* **Método HTTP:** `POST`
* **Cuerpo de la Petición (Body):** `UserCreateRequestDTO` (Estructura validada)
* **Respuestas:**
    * `200 OK`: Registro exitoso. Retorna un objeto `UserResponseDTO`.

#### 2. Actualizar Perfil de Usuario
Actualiza los datos del usuario dueño de la sesión actual. Acepta peticiones de tipo multipart para soportar la actualización simultánea de metadatos estructurados y la carga opcional de archivos.

* **URL:** `/api/v1/users`
* **Método HTTP:** `PUT`
* **Cabeceras:** `Content-Type: multipart/form-data`
* **Partes de la Petición (Request Parts):**
    * `request` (`UserUpdateRequestDTO`): Campos de texto para la edición (Validado).
    * `file` (`MultipartFile`, opcional): Archivo de imagen de perfil o archivo multimedia.
* **Respuestas:**
    * `200 OK`: Modificación exitosa. Retorna un `UserResponseDTO` actualizado.

#### 3. Eliminar Usuario (Borrado Lógico)
Aplica un proceso de borrado lógico (*soft delete*) sobre la cuenta del usuario autenticado, marcándola como inactiva o archivada sin destruir físicamente el registro.

* **URL:** `/api/v1/users`
* **Método HTTP:** `DELETE`
* **Respuestas:**
    * `200 OK`: Baja lógica procesada correctamente. Retorna el estado en un `UserResponseDTO`.

#### 4. Obtener Datos del Usuario Actual
Recupera el perfil completo y detallado que le pertenece estrictamente a la identidad autenticada que realiza la llamada.

* **URL:** `/api/v1/users/me`
* **Método HTTP:** `GET`
* **Respuestas:**
    * `200 OK`: Retorna los detalles en un `UserResponseDTO`.

#### 5. Obtener Perfil Público por Username
Busca y recupera la vista limitada de un perfil, exponiendo únicamente los datos públicos permitidos filtrados mediante su nombre de usuario.

* **URL:** `/api/v1/users/username/{username}`
* **Método HTTP:** `GET`
* **Respuestas:**
    * `200 OK`: Retorna la vista restringida mapeada como `UserPublicResponseDTO`.

#### 6. Buscar Usuarios
Realiza una consulta a la base de datos comparando coincidencias de texto con los nombres de usuario. Los resultados se entregan fragmentados mediante bloques de paginación tradicionales.

* **URL:** `/api/v1/users/search`
* **Método HTTP:** `GET`
* **Parámetros de Consulta (Query Params):**
    * `query` (requerido): Texto o criterio clave de búsqueda.
    * `page` (por defecto: `0`): Índice numérico de la página solicitada.
    * `size` (por defecto: `10`): Cantidad máxima de registros por página.
* **Respuestas:**
    * `200 OK`: Retorna una colección estructurada en un `UserSearchPageResponseDTO`.

#### 7. Obtener Usuario por ID
Obtiene información sobre un usuario a través de su identificador numérico único. Las reglas restringen este resultado a esquemas de visualización pública.

* **URL:** `/api/v1/users/{id}`
* **Método HTTP:** `GET`
* **Respuestas:**
    * `200 OK`: Retorna la vista restringida mapeada como `UserPublicResponseDTO`.