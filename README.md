# STUDER

> Open collaborative platform for reusable and evolving educational knowledge.

Final Integrative Project — UTN FRC — 2026  
Facundo Rocha Seret — Student ID 412108

---

🇺🇸 English  
🇦🇷 [Versión en Español](#español)

---

# About the Project

STUDER is a collaborative web platform focused on asynchronous learning, inspired by software engineering concepts such as modularity, version control, reuse, and open-source collaboration.

Instead of treating educational content as static and isolated, the platform proposes an ecosystem where knowledge can continuously evolve through community participation.

Users can create reusable educational blocks such as:
- explanations,
- activities,
- challenges,
- quizzes,
- practical resources,
- and interactive learning content.

These blocks can later be:
- reused,
- forked,
- adapted,
- versioned,
- and validated collaboratively by the community.

Courses are not treated as rigid structures, but as dynamic compositions of reusable knowledge blocks.

The platform also introduces social and collaborative mechanics such as:
- public profiles,
- followers,
- reputation systems,
- user levels,
- rankings,
- thematic contests,
- and community validation.

The objective is to encourage an active ecosystem where users gain recognition through contribution, collaboration, and content quality.

---

# Motivation

Current digital learning environments often suffer from:
- duplicated content,
- fragmented resources,
- poor maintainability,
- low collaboration,
- and isolated educational ecosystems.

Educational materials frequently depend entirely on their original authors and become difficult to evolve over time.

STUDER aims to provide a more open, collaborative, and sustainable alternative, where learning and contributing become part of the same process.

---

# Main Concepts

## Reusable Educational Blocks

Independent educational units that can represent:
- theory,
- exercises,
- quizzes,
- multimedia resources,
- or challenges.

## Versioning & Forks

Users can:
- reuse existing blocks,
- fork them,
- improve them,
- and create alternative versions while preserving authorship and history.

## Collaborative Validation

Content quality emerges through:
- community interaction,
- likes,
- usage,
- participation,
- and real-world adoption.

## Social & Community Layer

The platform includes:
- profiles,
- followers,
- discussions,
- rankings,
- contests,
- reputation,
- and contribution systems.

---

# Technologies

## Backend
- Java
- Spring Boot
- Spring Security
- JWT Authentication

## Frontend
- Angular
- TypeScript
- PrimeNG

## Database
- PostgreSQL

## External APIs
- YouTube Data API
- Email Service API

## Infrastructure
- Git & GitHub
- Jira
- Nginx

---

# Development Methodology

The project follows an agile methodology using Scrum and Jira for sprint planning, backlog management, and task tracking.

Development is organized into:
- Sprint 0 (project setup),
- followed by 4 iterative development sprints.

---

# Sprint Planning

## Sprint 0 — Project Foundation

- Backend setup
- Frontend setup
- PostgreSQL configuration
- Project architecture
- Theme system
- Internationalization (i18n)
- Base interceptors
- Initial Jira backlog

---

## Sprint 1 — Authentication & Social Core

- JWT authentication
- Refresh tokens
- Roles & permissions
- Notifications
- Discussions
- Messaging
- Followers system
- Tags & mentions

---

## Sprint 2 — Educational Content System

- Educational blocks
- Learning paths/courses
- Statistics system
- Content permissions

---

## Sprint 3 — Reuse & Versioning

- Fork system
- Block versioning
- Reuse system
- Search engine
- Tags filtering

---

## Sprint 4 — Community & Gamification

- Contests
- Rankings
- Reputation system
- Community levels
- User profiles
- Main feed/news



---

# Español

# Sobre el Proyecto

STUDER es una plataforma web colaborativa orientada al aprendizaje asincrónico, inspirada en conceptos del mundo del desarrollo de software como la modularidad, reutilización, control de versiones y filosofía open source.

La propuesta busca que el conocimiento no sea cerrado ni estático, sino que pueda evolucionar continuamente a través de la participación de la comunidad.

Los usuarios podrán crear bloques educativos reutilizables como:
- explicaciones,
- actividades,
- desafíos,
- cuestionarios,
- recursos prácticos,
- y contenido interactivo.

Estos bloques podrán:
- reutilizarse,
- modificarse,
- bifurcarse,
- versionarse,
- y validarse colectivamente.

Los cursos existirán como recorridos construidos a partir de bloques reutilizables, dejando de ser estructuras rígidas para convertirse en configuraciones dinámicas de conocimiento.

La plataforma también incorporará herramientas sociales y comunitarias como:
- perfiles públicos,
- seguidores,
- reputación,
- niveles de usuario,
- rankings,
- desafíos temáticos,
- y validación comunitaria.

El objetivo es fomentar un ecosistema activo donde los usuarios ganen reconocimiento a través de sus contribuciones, participación y calidad de contenido.

---

# Problemática

Muchos entornos educativos digitales actuales presentan:
- contenido duplicado,
- recursos fragmentados,
- poca colaboración,
- dificultades de mantenimiento,
- y materiales que dependen exclusivamente de sus autores originales.

STUDER busca ofrecer una alternativa más abierta, colaborativa y sostenible, donde aprender y contribuir formen parte del mismo proceso.

---

# Conceptos Principales

## Bloques Educativos Reutilizables

Unidades independientes de conocimiento que pueden representar:
- teoría,
- ejercicios,
- cuestionarios,
- recursos multimedia,
- o desafíos.

## Versionado y Forks

Los usuarios podrán:
- reutilizar bloques existentes,
- crear forks,
- mejorarlos,
- y generar nuevas versiones manteniendo historial y autoría.

## Validación Comunitaria

La calidad del contenido emergerá mediante:
- interacción,
- valoraciones,
- uso real,
- participación,
- y adopción dentro de la comunidad.

## Capa Social y Comunitaria

La plataforma incluirá:
- perfiles,
- seguidores,
- discusiones,
- rankings,
- contests,
- reputación,
- y sistemas de contribución.

---

# Tecnologías

## Backend
- Java
- Spring Boot
- Spring Security
- JWT

## Frontend
- Angular
- TypeScript
- PrimeNG

## Base de Datos
- PostgreSQL

## APIs Externas
- YouTube Data API
- API de envío de mails

## Infraestructura
- Git & GitHub
- Jira
- Nginx

---

# Metodología de Trabajo

El proyecto seguirá una metodología ágil basada en Scrum utilizando Jira para planificación de sprints, backlog y seguimiento de tareas.

El desarrollo estará organizado en:
- Sprint 0 de preparación,
- y 4 sprints iterativos principales.

---

# Planificación General

## Sprint 0 — Base del Proyecto

- Configuración backend
- Configuración frontend
- Configuración PostgreSQL
- Arquitectura inicial
- Sistema de temas
- Internacionalización
- Interceptores base
- Backlog inicial en Jira

---

## Sprint 1 — Autenticación y Base Social

- JWT
- Refresh tokens
- Roles y permisos
- Notificaciones
- Discusiones
- Mensajería
- Seguidores
- Tags y menciones

---

## Sprint 2 — Sistema Educativo

- Bloques educativos
- Recorridos/cursos
- Estadísticas
- Permisos de contenido

---

## Sprint 3 — Reutilización y Versionado

- Sistema de forks
- Versionado
- Reutilización
- Búsquedas
- Filtros por tags

---

## Sprint 4 — Comunidad y Gamificación

- Contests
- Rankings
- Sistema de reputación
- Niveles de comunidad
- Perfiles
- Feed principal


