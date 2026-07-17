# STUDER Frontend - Guía de Proyecto

## Stack Tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| **Angular** | ^19.2.0 | Framework standalone (sin NgModules) |
| **PrimeNG** | ^19.1.4 | Solo StepsModule usado hasta ahora |
| **PrimeIcons** | ^7.0.0 | Iconos |
| **Font Awesome Free** | ^7.2.0 | Iconos (sidebar) |
| **@ngx-translate/core** | ^17.0.0 | Internacionalización (i18n) |
| **RxJS** | ~7.8.0 | Programación reactiva |
| **uuid** | — | Generación de IDs |

---

## Arquitectura General

### Standalone Components (Angular 19)
- 100% standalone, sin NgModules.
- Bootstrap con `bootstrapApplication(AppComponent, appConfig)`.
- `app.config.ts` usa `ApplicationConfig` con provider functions.

### Estado
- **Sin librería externa** (no NgRx, ni signals store).
- Estado manejado con **BehaviorSubject + asObservable()**.
- Patrón `Subject<void>` + `takeUntil(this.destroy$)` para limpieza de subscripciones.

### HTTP Layer
5 interceptores funcionales (`HttpInterceptorFn`) en orden:
1. `caseConverterInterceptor` — snake_case ↔ camelCase
2. `refreshInterceptor` — 401 → refresh → retry
3. `authInterceptor` — Bearer token
4. `languageInterceptor` — Accept-Language header

### DI
- Servicios con `providedIn: 'root'`.
- Guards funcionales con `inject()`.
- `provideAppInitializer` para registro de content types al inicio.

---

## Estructura de Carpetas

```
src/
├── app/
│   ├── app.component.ts/.html/.css      # Root component
│   ├── app.config.ts                     # Config: interceptors, i18n, router
│   ├── app.routes.ts                     # Rutas
│   ├── config/
│   │   └── api.config.ts                 # API base URL y endpoints
│   ├── core/
│   │   ├── auth/                         # Auth system
│   │   ├── http/                         # case-converter interceptor
│   │   ├── i18n/                         # ngx-translate setup + language service
│   │   ├── notifications/               # Polling service (10s)
│   │   └── theme/                       # Theme service (light/dark)
│   ├── features/
│   │   ├── blocks/                      # Sistema de bloques (core)
│   │   ├── chats/                       # Mensajería
│   │   ├── discussions/                 # Foro de discusiones
│   │   ├── friends/                     # Amigos/seguidores
│   │   ├── home/                        # Dashboard (placeholder)
│   │   ├── landing/                     # Landing page (pública)
│   │   ├── notifications/              # Notificaciones + Strategy pattern
│   │   ├── search/                      # Búsqueda de usuarios
│   │   ├── test/                        # Componentes de prueba
│   │   └── users/                       # Usuarios, perfil, login/register
│   └── shared/
│       ├── components/                  # Componentes reutilizables
│       ├── pipes/                       # relative-time, safe-html
│       └── services/                    # entity-cache, modal, rich-text-parser
├── assets/
│   ├── i18n/en.json                     # ~375 keys
│   └── i18n/es.json                     # ~375 keys
└── styles.css                           # Variables CSS + temas
```

---

## Routing

```
/                  → LandingComponent   (pública)
/login             → LoginRegisterComponent (pública)
/test              → TestComponent      (dev)

(LayoutComponent - shell autenticado)
  /home                              → HomeComponent
  /discussions                       → DiscussionComponent
  /discussions/:id                   → DiscussionComponent
  /courses                           → HomeComponent (PLACEHOLDER)
  /calendar                          → HomeComponent (PLACEHOLDER)
  /messages                          → ChatComponent
  /messages/:chatId                  → ChatComponent
  /search                            → SearchComponent
  /account                           → redirect /user/me
  /user/me                           → UserProfileComponent
  /user/:identifier                  → UserProfileComponent
  /test/discussions                  → DiscussionsTestComponent (dev)
** → redirect /
```

> `authGuard` está definido pero **comentado** en las rutas.

---

## CSS / Theme System

### Variables CSS en `styles.css`
- Tema claro en `:root` / `body.theme-light`
- Tema oscuro en `body.theme-dark`
- Background images: `back-light.png` / `back-dark.png`

### Variables clave
```
--color-primary: #4285f4          (azul)
--color-correct: #00bf63          (verde)
--color-error: #ff3131            (rojo)
--color-warning: #ff751f          (naranja)
--color-info: #7E57C2            (púrpura)
--color-restriction: #2b2b2e     (gris oscuro)
--color-bg, --color-text-prim, --color-text-secu
--sidebar-width: 5rem
--header-height: 3.5rem
--content-max-width: 1900px
```

### Colores del sidebar (mapeo con logo STUDER)
```
S (azul)   → home
t (verde)  → courses
u (rojo)   → contest
d (naranja)→ discussions
e (púrpura)→ messages
r (gris)   → account
```

### Rich Text Entity Colors
```
--entity-user-color: #4285f4     (@username)
--entity-tag-color: #9c27b0     (#tag)
--entity-course-color: #00bf63  (&course)
--entity-contest-color: #ff3131 ($contest)
--entity-block-color: #ff9800   (%block)
```

### ThemeService
- Persiste en localStorage (`app-theme`).
- Usa View Transitions API para animación circle-clip.

---

## i18n

### Setup
- `@ngx-translate/core` v17 + `@ngx-translate/http-loader`
- Fallback: `'es'`
- Archivos: `/assets/i18n/{en,es}.json`
- `LanguageService`: tipo `'en' | 'es'`, persiste en localStorage (`preferred_language`)
- Interceptor agrega `Accept-Language` header

### Keys organizadas por feature
`common`, `home`, `system`, `login`, `register`, `landing`, `sidebar`, `header`, `search`, `messages`, `profile`, `footer`, `notifications`, `discussions`, `discussion`, `chat`, `blocks`, `difficulty`, `action`, `text_editor`, `activity`

### Uso en templates
```html
{{ 'key.name' | translate }}
```

---

## Sistema de Bloques (Core)

### Modelo de datos
Los bloques guardan su contenido como JSON en un campo `content` (jsonb en DB).
En el front se modela como array de `BlockContentItem`:

```typescript
interface BlockContentItem<T = unknown> {
  id: string;
  type: BlockType;  // 'text' | 'activity' | 'video' | 'gallery'
  data: T;
}
```

### Content Registry (Plugin System)
- `ContentRegistryService` — `Map<string, {creatorComponent, viewerComponent}>`
- Registrado en `app.config.ts` via `provideAppInitializer`
- Tipos registrados: `'text'`, `'activity'`
- Interfaces definen además: `'video'`, `'gallery'` (no implementados)

### BlockViewerComponent
Usa `*ngComponentOutlet` para renderizar dinámicamente:
```html
<ng-container *ngComponentOutlet="getComponentForType(item.type); inputs: { data: item.data }">
```

### Para agregar un nuevo tipo de contenido (ej: video, gallery)
1. Definir interfaz de datos en `content.interfaces.ts`
2. Crear CreatorComponent y ViewerComponent
3. Registrar en `content-registration.ts`
4. Crear un "intérprete" que lea el JSON y renderice

### BlockEditorComponent
Editor completo de bloques (metadatos + contenido). Se abre como modal.

### Operaciones de bloques
- `POST /blocks` — crear
- `POST /blocks/fork` — forkear
- `POST /blocks/version` — nueva versión
- `GET /blocks/search` — buscar (paginado, filtros)
- `GET /blocks/tree/{id}` — árbol (parent/child/son)
- `GET /blocks/user/{id}` — bloques de usuario

---

## Rich Text Entity System

### Parsing
`RichTextParserService` tokeniza texto con estos patrones:
- `@username` → tipo `user`
- `#tag` → tipo `tag`
- `&course` → tipo `course`
- `$contest` → tipo `contest`
- `%block` → tipo `block`

### Rendering
`RichTextComponent` renderiza como `<a>` links con:
- CSS classes: `entity entity--{type}`
- Hover popover via `EntityPopoverComponent`
- 5-min TTL cache via `EntityCacheService`

### Uso
```html
<studer-rich-text [text]="'@fulano hizo un &curso-de-python genial'"></studer-rich-text>
```

### Popovers actuales
- `user`: carga real desde API (`UserService.getByUsername`)
- `tag`, `course`, `contest`, `block`: datos mock (generan datos aleatorios)

---

## Sistema de Notificaciones

### Modelo
```typescript
LinkedType = 'COURSE' | 'DISCUSSION' | 'ACTIVITY' | 'MESSAGE' | 'USER' | 'SYSTEM' | 'EVENT'
```

### Strategy Pattern
`NotificationStrategyFactory` mapea `LinkedType` → estrategia que provee:
- `getRoute()` — ruta de navegación al hacer click
- `getIcon()` — icono a mostrar
- `getQueryParams()` — parámetros adicionales

Estrategias implementadas:
- `DiscussionNotificationStrategy`
- `MessageNotificationStrategy`
- `UserNotificationStrategy`
- `SystemNotificationStrategy`
- `DefaultNotificationStrategy`

### Polling
`NewNotificationService` cada 10s cuando hay usuario autenticado.

---

## Features: Estado Actual

### Completos (con detalles)
- **Mensajes/Chats**: Chat completo con lista de chats, amigos, envío de archivos. ⚠️ Problema con envío de imágenes.
- **Discusiones**: Foro completo con sidebar de categorías, filtros explore, creación, mensajes, likes, favoritos. ⚠️ Problema con envío de imágenes. Modal de creación debería ser inline.

### Placeholders / No implementados
- **Cursos** (`/courses` → HomeComponent): Nada implementado.
- **Feed** (`/home` → HomeComponent): Nada (será el home).
- **Contest** (`/contest` → no existe ruta): Nada.
- **Calendar** (`/calendar` → HomeComponent): Nada.

### Parciales
- **Blocks**: Creación, edición, versiones, fork, árbol OK. Faltan likes/unlikes, tree view visual.
- **User Profile**: Muestra datos, avatar, follow/unfollow, lista de bloques. Faltan: nivel/puntos, followers count, insignias, rich-text entities para nivel.
- **Rich Text Popovers**: Users OK (API real), tags/courses/contests/blocks son mock.

---

## Convenciones de Código

### Estilo
- **Sin comentarios** en código (salvo lo necesario).
- **Naming**: camelCase en TS, kebab-case en selectores (`studer-*`).
- **Selectores**: prefijo `studer-` (ej: `studer-block-card`, `studer-rich-text`).

### Templates
- Usar directivas de flujo de Angular 17+: `@if`, `@for`, `@switch`.
- Pipe `translate` para i18n.
- `track` en @for loops.
- `*ngComponentOutlet` para componentes dinámicos.

### Componentes
- `standalone: true` siempre.
- `ChangeDetectionStrategy.OnPush` en componentes de presentación.
- `inject()` en lugar de constructor DI en guards.

### Servicios
- `providedIn: 'root'`.
- Return tipos Observable tipados.
- HttpParams para query params.

### Modelos
- Interfaces exportadas, camelCase (caseConverterInterceptor convierte desde snake_case).
- DTOs separados por feature.

### Modal Service
```typescript
modalService.open(Component, {
  title: 'translation.key',
  inputs: { ... },
  outputs: { save: (data) => {...}, cancel: () => {...} }
});
```

---

## Reglas Importantes

1. **No modificar estilos globales** — todas las variables están en `styles.css`, usarlas via `var(--color-*)`.
2. **i18n siempre** — todo texto visible debe usar pipe `translate` o servicio `TranslateService`.
3. **No NgModules** — todo standalone.
4. **Interceptores existentes** ya manejan snake_case ↔ camelCase y auth — no duplicar.
5. **API base**: `http://localhost:1583/api/v1` (config en `api.config.ts`).
6. **Content types**: registrar nuevos en `content-registration.ts` con creator + viewer.
7. **Rich text entity tokens**: mantener consistencia en prefijos (`@`, `#`, `&`, `$`, `%`).
8. **Sidebar nav items**: agregar en `sidebar.component.ts` si se necesita nueva ruta principal.

---

## Próximos Pasos (Backlog)

1. **Cursos**: Pantalla descubrimiento + creación con drag & drop de bloques + progreso + reutilizar buscador de bloques.
2. **Feed**: Home con publicaciones, usar creators/interpreters de text, video, gallery.
3. **Contest**: Pantallas de concurso, leaderboard, submit, validate.
4. **Perfil de usuario**: Nivel, puntos, followers, insignias/medallas.
5. **Bloques**: Likes/unlikes, visualización de árbol de forks.
6. **Nuevos content types**: Video (YouTube embed), Gallery (carrusel/grid/masonry).
7. **Rich text popovers**: Reemplazar mocks con datos reales de API para courses, contests, blocks.
8. **Mensajes/Discusiones**: Fix en envío de imágenes.
9. **Discusiones**: Botón "Abrir nueva" inline en vez de modal.
10. **Admin**: Panel para crear/finalizar concursos.
