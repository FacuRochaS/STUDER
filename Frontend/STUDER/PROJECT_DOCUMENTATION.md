
# Documentación del Proyecto STUDER Frontend

## 1. Descripción General

Este documento describe la arquitectura, tecnologías, convenciones y estructura del proyecto frontend de STUDER. El objetivo es mantener un código limpio, escalable y fácil de mantener.

## 2. Tecnologías y Paquetes Clave

- **Framework**: **Angular 19.2.0**. Se deben aprovechar las últimas características del framework, como las nuevas directivas de control de flujo (`@if`, `@for`, `@switch`).
- **Componentes UI**: **PrimeNG 19.1.4**. Es la biblioteca principal para componentes de UI. Se debe priorizar el uso de sus componentes (botones, tablas, modales, etc.) para mantener la consistencia visual.
- **Iconografía**:
    - **PrimeIcons 7.0.0**: La biblioteca de iconos por defecto de PrimeNG.
    - **Font Awesome 7.2.0**: Se utiliza como fuente secundaria de iconos.
- **Internacionalización (i18n)**: **@ngx-translate/core**. Permite la traducción de la aplicación a múltiples idiomas.

## 3. Estructura del Proyecto

La estructura de carpetas dentro de `src/app` sigue un enfoque modular y semántico:

- **`src/app/core`**: Contiene servicios singleton, guardias de ruta (`guards`), interceptores (`interceptors`) y modelos de datos que son fundamentales para toda la aplicación.
- **`src/app/features`**: Cada carpeta dentro de `features` representa una funcionalidad principal de la aplicación (un módulo de negocio). Ejemplos actuales:
    - `home`: Dashboard principal.
    - `users`: Gestión de usuarios.
    - `discussions`: Foros o secciones de debate.
    - `notifications`: Centro de notificaciones.
    - `landing`: Página de aterrizaje inicial.
- **`src/app/shared`**: Incluye componentes, directivas, pipes y servicios que se reutilizan en múltiples `features`. Por ejemplo, un componente de `rich-text` o un popover de entidad.
- **`src/app/config`**: Almacena archivos de configuración específicos de la aplicación.

## 4. Reglas y Convenciones

### 4.1. Estilos y Colores

- **Archivo Central de Estilos**: Todos los colores, fuentes y variables CSS globales se definen exclusivamente en `src/styles.css`.
- **Uso de Variables CSS**: Para aplicar colores o estilos globales en los componentes, **siempre se deben usar las variables CSS definidas** en `styles.css`. No se deben "hardcodear" colores en los archivos `.css` de los componentes.

  ```css
  /* Ejemplo en styles.css */
  :root {
    --primary-color: #007bff;
    --text-color: #333;
  }

  /* Ejemplo en un componente.css */
  .mi-componente {
    color: var(--text-color);
    background-color: var(--primary-color);
  }
  ```

### 4.2. Internacionalización (i18n)

- **Archivos de Traducción**: Los textos se gestionan en archivos JSON ubicados en `src/assets/i18n/`. Existe un archivo por cada idioma soportado (ej. `en.json`, `es.json`).
- **Uso en el Template**: Se utiliza el pipe `translate` o la directiva `[translate]` de `ngx-translate`.

  ```html
  <!-- Usando el pipe -->
  <h1>{{ 'HOME.TITLE' | translate }}</h1>

  <!-- Usando la directiva -->
  <button [translate]="'BUTTONS.SAVE'"></button>
  ```

### 4.3. Directivas de Angular 19

- **Control de Flujo**: Se debe utilizar la nueva sintaxis de directivas de control de flujo en lugar de `*ngIf`, `*ngFor` y `ngSwitch`.

  ```html
  <!-- En lugar de *ngIf -->
  @if (isLoggedIn) {
    <span>Bienvenido</span>
  } @else {
    <span>Acceder</span>
  }

  <!-- En lugar de *ngFor -->
  @for (item of items; track item.id) {
    <li>{{ item.name }}</li>
  }
  ```

## 5. Componentes Principales

- **Rich Text Editor**: Ubicado en `src/app/shared/components/rich-text`, es un editor de texto enriquecido que probablemente se usa en secciones como `discussions`.
- **Entity Popover**: Componente en `src/app/shared/components/rich-text/entity-popover` que muestra información adicional sobre una "entidad" específica dentro del texto.
- **Componentes de Features**: Cada carpeta en `src/app/features` contiene los componentes que construyen esa funcionalidad específica (ej. `HomeComponent`, `UserListComponent`, etc.).
