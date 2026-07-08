# AuthController API Documentation

---

## 🇬🇧 English

### Overview
REST controller for authentication operations. It is responsible for handling user login, token refresh, and logout processes.

### Endpoints

#### 1. Login
Authenticates a user and returns an access token. It also sets a refresh token securely in an `HttpOnly` cookie.

* **URL:** `/api/v1/auth/login`
* **HTTP Method:** `POST`
* **Request Body:** `LoginRequestDTO` (Must be valid)
* **Responses:**
  * `200 OK`: Successfully authenticated. Returns `LoginResponseDTO`.

#### 2. Refresh Token
Refreshes the current access token using the refresh token extracted from the cookie. It performs token rotation by setting a new refresh token cookie.

* **URL:** `/api/v1/auth/refresh`
* **HTTP Method:** `POST`
* **Responses:**
  * `200 OK`: Token successfully refreshed. Returns `RefreshResponseDTO`.
  * `401 Unauthorized`: No refresh token found in the cookie.

#### 3. Logout
Logs out the user by revoking the current refresh token and clearing the refresh token cookie.

* **URL:** `/api/v1/auth/logout`
* **HTTP Method:** `POST`
* **Responses:**
  * `204 No Content`: Successfully logged out.

---

## 🇪🇸 Español

### Descripción General
Controlador REST para las operaciones de autenticación. Es responsable de manejar los procesos de inicio de sesión de usuario (login), actualización de tokens (refresh) y cierre de sesión (logout).

### Endpoints

#### 1. Iniciar Sesión (Login)
Autentica a un usuario y devuelve un token de acceso. También establece un *refresh token* de forma segura en una cookie `HttpOnly`.

* **URL:** `/api/v1/auth/login`
* **Método HTTP:** `POST`
* **Cuerpo de la Petición (Body):** `LoginRequestDTO` (Debe ser válido)
* **Respuestas:**
  * `200 OK`: Autenticación exitosa. Retorna `LoginResponseDTO`.

#### 2. Actualizar Token (Refresh Token)
Actualiza el token de acceso actual utilizando el *refresh token* extraído de la cookie. Realiza la rotación de tokens estableciendo una nueva cookie con el *refresh token* actualizado.

* **URL:** `/api/v1/auth/refresh`
* **Método HTTP:** `POST`
* **Respuestas:**
  * `200 OK`: Token actualizado exitosamente. Retorna `RefreshResponseDTO`.
  * `401 Unauthorized`: No se encontró ningún *refresh token* en la cookie.

#### 3. Cerrar Sesión (Logout)
Cierra la sesión del usuario revocando el *refresh token* actual y eliminando la cookie correspondiente.

* **URL:** `/api/v1/auth/logout`
* **Método HTTP:** `POST`
* **Respuestas:**
  * `204 No Content`: Sesión cerrada exitosamente.


