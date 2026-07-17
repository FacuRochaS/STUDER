# Nuevos Endpoints y Cambios en la API

## 1. Blocks - Like / Unlike

### POST `/api/v1/blocks/{id}/like`
Likea un bloque.

**Response:**
```json
{
  "success": true,
  "message": "block.like_added"
}
```

### DELETE `/api/v1/blocks/{id}/like`
Saca el like de un bloque.

**Response:**
```json
{
  "success": true,
  "message": "block.like_removed"
}
```

---

## 2. Courses

### POST `/api/v1/courses`
Crea un curso con sus bloques. Si `version_id` es null, se usa la `currentVersion` del bloque.

**Request:**
```json
{
  "name": "Curso de Python",
  "slug": "curso-de-python",
  "link": "url de foto (opcional)",
  "tags": ["python", "programacion"],
  "blocks": [
    {
      "block_id": 1,
      "version_id": null,
      "order": 1
    },
    {
      "block_id": 2,
      "version_id": 5,
      "order": 2
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "owner": { "id": 1, "username": "fulano", "first_name": "Fulano", "last_name": "..." },
  "name": "Curso de Python",
  "slug": "curso-de-python",
  "tags": ["python", "programacion"],
  "link": "...",
  "published": true,
  "created_datetime": "2026-07-10T...",
  "last_updated_datetime": "2026-07-10T...",
  "favourite": false,
  "favourite_count": 0,
  "rating_sum": 0,
  "rating_count": 0,
  "blocks": [
    {
      "id": 1,
      "block_id": 1,
      "block_name": "Nombre del bloque",
      "version": { "id": 1, "content": "...", "version_number": 1 },
      "order": 1
    }
  ]
}
```

### GET `/api/v1/courses?page=0&filter=recent`
Lista cursos publicados. Filters: `recent` (ordenado por fecha) | `popular` (ordenado por rating_count).

### GET `/api/v1/courses/me?page=0`
Cursos del usuario autenticado.

### GET `/api/v1/courses/favourites?page=0`
Cursos favoritos del usuario autenticado.

### GET `/api/v1/courses/{id}`
Detalle del curso con todos sus bloques.

### PUT `/api/v1/courses/{id}`
Edita el curso. Solo el owner. Todos los campos son opcionales (si no se envía un campo, no se modifica).

**Request (misma estructura que create):**
```json
{
  "name": "Nuevo nombre",
  "slug": "nuevo-slug",
  "link": "nueva foto",
  "tags": ["nuevos", "tags"],
  "blocks": [
    { "block_id": 3, "version_id": null, "order": 1 }
  ]
}
```

### POST `/api/v1/courses/{id}/favourite`
Marca curso como favorito. Puntos: +5 al owner del curso.

### DELETE `/api/v1/courses/{id}/favourite`
Desmarca favorito. Puntos: -5 al owner.

### POST `/api/v1/courses/blocks/interaction`
Guarda la interacción del usuario con un bloque del curso (progreso).

**Request:**
```json
{
  "course_block_id": 1,
  "completed": true,
  "duration": 120,
  "attempts": 2
}
```

**Response:**
```json
{
  "id": 1,
  "course_block_id": 1,
  "completed": true,
  "duration": 120,
  "attempts": 2
}
```

---

## 3. Feed

### POST `/api/v1/feed`
Crea un post. El `content` es un JSON libre (puede tener cualquier estructura).

**Request:**
```json
{
  "content": {
    "text": "Mi primer post!",
    "image_url": "https://..."
  },
  "tags": ["python", "programacion"]
}
```

**Response:**
```json
{
  "id": 1,
  "user": { "id": 1, "username": "fulano", ... },
  "content": { "text": "Mi primer post!", "image_url": "https://..." },
  "tags": ["python", "programacion"],
  "created_datetime": "2026-07-10T...",
  "like_count": 0,
  "liked_by_current_user": false
}
```

### GET `/api/v1/feed?page=0&filter=recent`
Feed de posts. Filters:
- `recent` - todos, ordenados por fecha
- `following` - solo posts de amigos confirmados + propios
- `popular` - ordenados por cantidad de likes

### GET `/api/v1/feed/user/{userId}?page=0`
Posts de un usuario específico.

### POST `/api/v1/feed/{id}/like`
Likea un post. Puntos: +1 al autor del post.

### DELETE `/api/v1/feed/{id}/like`
Saca el like. Puntos: -1 al autor.

---

## 4. Contest (Concursos)

### GET `/api/v1/contests/{id}`
Obtiene los datos del concurso.

**Response:**
```json
{
  "id": 1,
  "title": "Concurso de Python",
  "content": { "consigna": "Crear un curso de Python..." },
  "tags": ["python"],
  "status": "PREPARATION",
  "start_date": "2026-07-10T00:00:00",
  "change_date": "2026-07-17T00:00:00",
  "end_date": "2026-07-24T00:00:00",
  "created_datetime": "2026-07-10T..."
}
```

### POST `/api/v1/contests/{id}/submit`
Envía un curso al concurso (solo durante PREPARATION). Crea el curso con `published=false`. El usuario debe tener nivel alto (se valida por `points`).

**Request:** (mismo body que crear curso)
```json
{
  "name": "Mi curso del concurso",
  "slug": "mi-curso-concurso",
  "link": "",
  "tags": ["python"],
  "blocks": [
    { "block_id": 1, "version_id": null, "order": 1 }
  ]
}
```

**Response:** CourseResponseDTO con `published: false`.

### GET `/api/v1/contests/{id}/validate/random`
Obtiene un curso aleatorio del concurso para validar (solo durante VALIDATION).

**Comportamiento:**
- Excluye cursos que el usuario ya valoró
- Excluye cursos con promedio < 1.5 estrellas
- Da prioridad a cursos sin valoraciones
- En los últimos 3 días del concurso, da prioridad a los mejores valorados
- **Oculta al creador** y datos de likes del curso

**Response:**
```json
{
  "id": 1,
  "name": "Mi curso del concurso",
  "slug": "mi-curso-concurso",
  "tags": ["python"],
  "link": "",
  "rating_sum": 15,
  "rating_count": 3,
  "average_rating": 5.0,
  "created_datetime": "2026-07-10T...",
  "blocks": [
    {
      "id": 1,
      "block_id": 1,
      "block_name": "Nombre del bloque",
      "version": { "id": 1, "content": "...", "version_number": 1 },
      "order": 1
    }
  ]
}
```

### POST `/api/v1/contests/validate`
Valora un curso del concurso (solo durante VALIDATION). Puntos: +5 al creador del curso.

**Request:**
```json
{
  "course_id": 1,
  "rating": 5
}
```

**Response:**
```json
{
  "success": true,
  "message": "contest.rating_added"
}
```

### GET `/api/v1/contests/{id}/leaderboard`
Leaderboard del concurso. Ordenado por score descendente.

**Response:**
```json
[
  {
    "course_id": 1,
    "course_name": "Curso Python",
    "average_rating": 4.5,
    "rating_count": 10,
    "like_count": 25,
    "score": 3.22
  }
]
```
> Score = (avg_rating * 0.7) + (min(likes, 100) * 0.003)

---

## 5. Admin

### POST `/api/v1/admin/contests`
Crea un concurso. Solo usuarios con `role = "ADMIN"`.

**Request:**
```json
{
  "title": "Concurso de Python",
  "content": { "consigna": "Crear un curso de Python..." },
  "tags": ["python"],
  "start_date": "2026-07-10T00:00:00"
}
```

El sistema calcula automáticamente:
- `change_date` = start_date + 7 días (inicio de validación)
- `end_date` = start_date + 14 días (fin del concurso)
- `status` = "PREPARATION"

### POST `/api/v1/admin/contests/{id}/finish`
Finaliza el concurso. Solo ADMIN. Publica todos los cursos participantes (`published=true`, `contest_hidden=false`) y desconecta el contest.

**Response:**
```json
{
  "success": true,
  "message": "contest.finished"
}
```

---

## 6. User Levels (Puntos)

Se agregó el campo `role` al User (`"USER"` por defecto, `"ADMIN"` para admins).

### Tabla de puntos por acción:

| Acción | Puntos | Lugar |
|--------|--------|-------|
| Like en bloque (recibido) | +3 | BlockService.likeBlock |
| Unlike en bloque | -3 | BlockService.unlikeBlock |
| Like en mensaje de discusión (recibido) | +1 | DiscussionServiceImpl.like |
| Unlike en mensaje de discusión | -1 | DiscussionServiceImpl.unlike |
| Valorar un curso (recibido) | +5 | ContestServiceImpl.rateCourse |
| Crear un bloque | +1 | BlockServiceImpl.create |
| Forkear un bloque | +1 | BlockServiceImpl.fork |
| Crear un curso | +3 | CourseServiceImpl.create |
| Marcar como fav una discusión tuya | +5 | DiscussionServiceImpl.addFavourite |
| Desmarcar fav de discusión tuya | -5 | DiscussionServiceImpl.removeFavourite |
| Marcar como fav un curso tuyo | +5 | CourseServiceImpl.addFavourite |
| Desmarcar fav de curso tuyo | -5 | CourseServiceImpl.removeFavourite |
| Ganar un follower | +10 | FriendServiceImpl.followUser |
| Like en un post (recibido) | +1 | FeedServiceImpl.likePost |
| Unlike en un post | -1 | FeedServiceImpl.unlikePost |

---

## 7. Nuevos valores en LinkedType (para notificaciones)

Se agregaron:
- `BLOCK`
- `COURSE`
- `POST`
- `CONTEST`

---

## 8. Nuevas entidades / columnas en DB

### User
- `role` VARCHAR NOT NULL (default: "USER")

### Course
- `contest_id` BIGINT (FK -> contest, nullable)
- `contest_hidden` BOOLEAN NOT NULL (default: false)
- `rating_sum` BIGINT NOT NULL (default: 0)
- `rating_count` INTEGER NOT NULL (default: 0)

### Contest
- `status` VARCHAR NOT NULL (PREPARATION | VALIDATION | FINISHED)

### course_ratings (nueva tabla)
- `id` BIGSERIAL PK
- `user_id` BIGINT NOT NULL (FK -> users)
- `course_id` BIGINT NOT NULL (FK -> courses)
- `rating` INTEGER NOT NULL (1-5)
- `created_datetime`, `last_updated_datetime`, `is_active`
- UNIQUE(user_id, course_id)
