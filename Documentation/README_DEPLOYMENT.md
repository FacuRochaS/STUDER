# Documentación de Despliegue y Arquitectura - Proyecto STUDER

## Arquitectura General
El sistema está compuesto por cuatro servicios principales, orquestados mediante Docker Compose:

- **Frontend**: Aplicación Angular servida por Nginx (puerto 4200 interno, expuesto en el host como 4200).
- **Backend**: API REST Spring Boot (puerto 8081 interno y externo).
- **Base de Datos**: PostgreSQL (puerto 5432).
- **Proxy Reverso**: Nginx (puerto 1583), que enruta peticiones entre frontend y backend.

### Diagrama Simplificado

```
[ Navegador ]
     |
     v
[ Nginx Proxy (1583) ]
   |             |
   v             v
[Frontend]   [Backend]
                  |
                  v
              [PostgreSQL]
```

## Puertos y Servicios

| Servicio   | Contenedor         | Puerto Interno | Puerto Host | Descripción                                 |
|------------|--------------------|----------------|-------------|---------------------------------------------|
| Frontend   | studer_frontend    | 80             | 4200        | Angular servido por Nginx                   |
| Backend    | studer_app_be      | 8081           | 8081        | API REST Spring Boot                        |
| DB         | studer_db          | 5432           | 5432        | PostgreSQL                                  |
| Proxy      | studer_nginx       | 1583           | 1583        | Nginx como proxy reverso y punto de entrada  |

- El **proxy reverso** (studer_nginx) es el único servicio expuesto al exterior (puerto 1583). Todas las peticiones del frontend y del usuario pasan por él.
- El **frontend** se expone en el puerto 4200 para desarrollo, pero en producción se recomienda acceder solo por el proxy.
- El **backend** y la **base de datos** solo son accesibles desde la red interna de Docker.

## Redes Docker

- `frontend_net`: conecta el proxy reverso y el frontend. El usuario accede al frontend a través del proxy.
- `backend_net`: conecta el proxy reverso, el backend y la base de datos. El frontend nunca accede directamente al backend ni a la base de datos.
- El proxy reverso (`studer_nginx`) es el único servicio presente en ambas redes, permitiendo la comunicación controlada entre frontend y backend.

## Configuración de CORS
- El backend permite orígenes: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:1583`.
- El frontend realiza peticiones API a través del proxy reverso (`http://localhost:1583/api/...`).
- El proxy enruta `/api/` al backend y el resto al frontend.

## Variables de Entorno Importantes
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: credenciales de la base de datos.
- `JWT_SECRET`: clave secreta para autenticación JWT en el backend.
- `SPRING_PROFILES_ACTIVE`: perfil de Spring Boot (por defecto `prod` en despliegue).

## Flujo de Peticiones
1. El usuario accede a `http://localhost:1583` (proxy reverso).
2. Nginx sirve la SPA de Angular o enruta `/api/` al backend.
3. El backend responde y, si es necesario, accede a la base de datos.

## Capas del Sistema
- **Presentación**: Frontend Angular (SPA).
- **API/Negocio**: Backend Spring Boot (servicios REST, lógica de negocio, seguridad).
- **Persistencia**: PostgreSQL (almacenamiento de datos).
- **Infraestructura**: Nginx como proxy reverso, redes Docker, orquestación con Docker Compose.

## Despliegue
1. Configura las variables de entorno necesarias en un archivo `.env` en la carpeta `Docker`.
2. Ejecuta:
   ```
   docker-compose -f Docker/docker-compose.yml up --build
   ```
3. Accede a la aplicación en: [http://localhost:1583](http://localhost:1583)

---

**Notas:**
- Para cambiar el nombre del proyecto, modifica el campo `name:` en el `docker-compose.yml`.
- Para producción, se recomienda exponer solo el proxy reverso y usar HTTPS.
- Puedes consultar los logs de cada servicio con `docker-compose logs <servicio>`.

