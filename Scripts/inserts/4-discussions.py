import random
from datetime import datetime, timedelta



def escape(text):
    return text.replace("'", "''")



end_date = datetime(2026, 6, 25)
start_date = end_date - timedelta(days=30)


def random_date(start, end):
    delta = end - start
    random_seconds = random.randint(0, int(delta.total_seconds()))
    return start + timedelta(seconds=random_seconds)



tags_nombres = [
    "java", "spring-boot", "angular", "docker", "ui-ux", "frontend", "backend",
    "matematicas", "algoritmos", "gaming", "minecraft", "mario-kart", "anime",
    "universidad", "utn", "unc", "ayuda", "debate", "pokemon", "arquitectura"
]

tags = {name: i + 1 for i, name in enumerate(tags_nombres)}


categorias_discusiones = [
    # Frontend / UI-UX
    {"t": "¿Cómo armar una sidebar fija y responsiva en Angular?",
     "d": "Estoy armando el layout principal de mi app y necesito una sidebar con iconos a la izquierda que se oculte en mobile. ¿Qué recomiendan usar?",
     "tags": ["angular", "ui-ux", "frontend", "ayuda"]},
    {"t": "Problema pasando datos entre componentes en Angular",
     "d": "Tengo un componente padre y un hijo, usando @Input y @Output pero a veces el valor llega undefined. Dejo el código de mi clase TypeScript.",
     "tags": ["angular", "frontend", "ayuda"]},
    {"t": "Debate: ¿Tailwind o Bootstrap para paneles de administración?",
     "d": "Para un proyecto de la facu tenemos que armar un dashboard, ¿qué dicen que es más rápido de implementar hoy en día para UI?",
     "tags": ["ui-ux", "frontend", "debate"]},
    # Backend / Java / Spring
    {"t": "Ayuda con DTOs y Mappers en Java Spring Boot",
     "d": "Tengo una entidad gigante y no quiero devolverla entera al front. ¿Usan MapStruct o arman los mappers a mano?",
     "tags": ["java", "spring-boot", "backend", "arquitectura"]},
    {"t": "Error al levantar contenedor Docker con app en Spring",
     "d": "Mi Dockerfile compila perfecto, pero cuando hago docker-compose up se cae diciendo que no encuentra la BD. ¿Ideas?",
     "tags": ["docker", "spring-boot", "backend", "ayuda"]},
    {"t": "¿Cómo estructurar bien la seguridad con JWT en Java?",
     "d": "Estoy implementando Spring Security y la verdad es un dolor de cabeza. ¿Algún repo de ejemplo que tengan a mano?",
     "tags": ["java", "spring-boot", "backend"]},
    # Matematicas / Universidad (UTN/UNC)
    {"t": "Duda urgente: Identidad de Bézout",
     "d": "Mañana rindo y estoy trabado. ¿Cómo aplico el algoritmo de Euclides hacia atrás para sacar los coeficientes? Siempre me mareo en el último paso.",
     "tags": ["matematicas", "algoritmos", "universidad", "ayuda"]},
    {"t": "Resumen Unidad 1 y 2 para el parcial",
     "d": "Gente, armé un PDF con todos los temas del práctico y los ejemplos de parciales de la Unidad 1 y 2. Les dejo el link por si a alguien le sirve.",
     "tags": ["universidad", "matematicas"]},
    {"t": "¿Alguien cursó en la UTN FRC con este profe?",
     "d": "Me tocó cursar a la noche y quería saber si es muy exigente con los finales prácticos o si toma más teoría.",
     "tags": ["utn", "universidad", "debate"]},
    {"t": "Inscripciones a materias UNC",
     "d": "¿Alguien sabe cuándo abren las inscripciones a través del sistema? El cuatrimestre pasado se colapsó todo.",
     "tags": ["unc", "universidad"]},
    # Gaming / Ocio
    {"t": "Mejores shaders oscuros para Minecraft",
     "d": "Estoy armando un server de supervivencia y busco shaders que le den un estilo más sombrío, tipo terror, pero que no maten la PC.",
     "tags": ["minecraft", "gaming", "debate"]},
    {"t": "Atajos rotos en Mario Kart",
     "d": "Ayer descubrí un atajo en la pista del volcán que te salta media vuelta. ¿Qué otros atajos así de rotos conocen?",
     "tags": ["mario-kart", "gaming", "nintendo"]},
    {"t": "Ideas para pixel art de Pokémon",
     "d": "Quiero armar un Gyarados negro gigante en mi mundo de Minecraft. ¿Tienen alguna web que pase imágenes a bloques?",
     "tags": ["pokemon", "minecraft", "gaming", "ayuda"]},
    {"t": "Recomendación de Anime de esta temporada",
     "d": "Acabo de terminar un par de series y me quedé sin nada para ver. ¿Qué están siguiendo ahora que valga la pena?",
     "tags": ["anime", "debate"]},
]

discusiones_generadas = []


titulos_variados = [
    "Duda con {t}", "Problema: {t}", "¿Qué opinan de {t}?", "Ayuda urgente: {t}", "Comparto recurso sobre {t}",
    "Discusión oficial: {t}", "Mi experiencia con {t}", "¿Alternativas a {t}?", "Consulta rápida sobre {t}"
]
conceptos = [
    ("Java 21", ["java", "backend"]), ("Angular 17", ["angular", "frontend"]), ("Docker Compose", ["docker"]),
    ("Algoritmo de Euclides", ["matematicas", "algoritmos"]), ("Congruencias Lineales", ["matematicas", "universidad"]),
    ("Diseño de interfaces", ["ui-ux", "frontend"]), ("Servidores de Minecraft", ["minecraft", "gaming"]),
    ("Torneos de Mario Kart", ["mario-kart", "gaming"]), ("Estrenos de Anime", ["anime"])
]

for i in range(60):

    if i < len(categorias_discusiones):
        base = categorias_discusiones[i]
        titulo = base["t"]
        desc = base["d"]
        tags_disc = base["tags"]

    else:
        formato = random.choice(titulos_variados)
        concepto, tags_c = random.choice(conceptos)
        titulo = formato.format(t=concepto)
        desc = f"Hola a todos, abro este hilo porque he estado dándole vueltas al tema de {concepto} y me gustaría saber sus opiniones o si me pueden dar una mano. ¡Gracias!"
        tags_disc = tags_c + [random.choice(["ayuda", "debate"])]

    owner_id = random.randint(1, 100)
    dt = random_date(start_date, end_date - timedelta(days=5))

    tags_disc = list(set(tags_disc))

    discusiones_generadas.append({
        "id": i + 1,
        "title": titulo,
        "description": desc,
        "owner_id": owner_id,
        "tags": [tags[t] for t in tags_disc if t in tags],
        "created_datetime": dt,
        "last_updated_datetime": dt,
        "message_count": 0,
    })


respuestas_base = [
    "A mí me pasó lo mismo, la solución fue revisar la configuración.",
    "¡Excelente aporte! Me sirve un montón para el parcial.",
    "Yo prefiero hacerlo de la otra forma, es más escalable.",
    "Jaja sí, tal cual. Ayer estuve 3 horas renegando con eso.",
    "Fijate en la documentación oficial, hay un apartado que explica justo este caso.",
    "¿Podrías pasar más detalles del error que te tira por consola?",
    "¡Qué buen atajo! Lo voy a probar hoy a la noche.",
    "Te recomiendo usar la herramienta que sacaron en la última versión, te simplifica todo el código.",
    "Si aplicas el algoritmo hacia atrás paso por paso, sale solo. Anotate los restos en una tabla.",
    "Uff, yo cursé con ese profe. Es exigente pero aprendés un montón.",
    "Yo usé flexbox para la sidebar y quedó perfecta en mobile.",
    "Dejo mi estrellita en el hilo para leerlo más tarde, me interesa."
]

mensajes_insert = []
msg_id_counter = 1

for disc in discusiones_generadas:

    num_mensajes = random.choice([0, 0, 1, 2, 3, 4, 5, 8, 12])
    disc["message_count"] = num_mensajes

    mensajes_locales = []
    msg_dt = disc["created_datetime"]

    for _ in range(num_mensajes):
        sender_id = random.randint(1, 100)
        content = random.choice(respuestas_base)


        msg_dt = msg_dt + timedelta(minutes=random.randint(5, 600))
        if msg_dt > end_date: msg_dt = end_date


        parent_id = "NULL"
        if mensajes_locales and random.choice([True, False]):
            parent_id = random.choice(mensajes_locales)

        mensajes_insert.append({
            "id": msg_id_counter,
            "discussion_id": disc["id"],
            "sender_id": sender_id,
            "parent_message_id": parent_id,
            "content": content,
            "link": "",
            "created_datetime": msg_dt,
            "last_updated_datetime": msg_dt
        })
        mensajes_locales.append(msg_id_counter)
        msg_id_counter += 1


        disc["last_updated_datetime"] = msg_dt


likes_insert = []
like_id_counter = 1
for m in mensajes_insert:
    num_likes = random.randint(0, 4)
    users_liked = random.sample(range(1, 101), num_likes)
    for u in users_liked:
        likes_insert.append({
            "id": like_id_counter,
            "user_id": u,
            "message_id": m["id"],
            "created_datetime": m["created_datetime"] + timedelta(minutes=random.randint(1, 60))
        })
        like_id_counter += 1


favs_insert = []
fav_id_counter = 1
for u in range(1, 101):
    num_favs = random.randint(0, 3)
    fav_discs = random.sample(range(1, 61), num_favs)
    for d_id in fav_discs:
        favs_insert.append({
            "id": fav_id_counter,
            "user_id": u,
            "discussion_id": d_id,
            "created_datetime": end_date - timedelta(days=random.randint(1, 10))
        })
        fav_id_counter += 1


with open('inserts_forum.sql', 'w', encoding='utf-8') as f:
    f.write("-- ==========================================\n")
    f.write("-- GENERADOR DE DATOS: FORO Y DISCUSIONES\n")
    f.write("-- ==========================================\n\n")

    f.write("-- 1. TAGS\n")
    for name, tag_id in tags.items():
        dt_str = start_date.strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO tags (name, created_datetime, last_updated_datetime, is_active) VALUES ('{name}', '{dt_str}', '{dt_str}', TRUE);\n")
    f.write("\n")

    f.write("-- 2. DISCUSSIONS\n")
    for d in discusiones_generadas:
        dt_str = d["created_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        up_str = d["last_updated_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO discussions (title, description, owner_id, message_count, created_datetime, last_updated_datetime, is_active) VALUES "
            f"('{escape(d['title'])}', '{escape(d['description'])}', {d['owner_id']}, {d['message_count']}, '{dt_str}', '{up_str}', TRUE);\n")
    f.write("\n")

    f.write("-- 3. DISCUSSION_TAGS\n")
    for d in discusiones_generadas:
        for tag_id in d["tags"]:
            f.write(f"INSERT INTO discussion_tags (discussion_id, tag_id) VALUES ({d['id']}, {tag_id});\n")
    f.write("\n")

    f.write("-- 4. DISCUSSION MESSAGES\n")
    for m in mensajes_insert:
        dt_str = m["created_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO discussion_messages (discussion_id, sender_id, parent_message_id, content, link, created_datetime, last_updated_datetime, is_active) VALUES "
            f"({m['discussion_id']}, {m['sender_id']}, {m['parent_message_id']}, '{escape(m['content'])}', '{escape(m['link'])}', '{dt_str}', '{dt_str}', TRUE);\n")
    f.write("\n")

    f.write("-- 5. MESSAGE LIKES\n")
    for l in likes_insert:
        dt_str = l["created_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO message_likes (user_id, message_id, created_datetime, last_updated_datetime, is_active) VALUES "
            f"({l['user_id']}, {l['message_id']}, '{dt_str}', '{dt_str}', TRUE);\n")
    f.write("\n")

    f.write("-- 6. USER DISCUSSION FAVS\n")
    for fav in favs_insert:
        dt_str = fav["created_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO user_discussion_favs (user_id, discussion_id, created_datetime, last_updated_datetime, is_active) VALUES "
            f"({fav['user_id']}, {fav['discussion_id']}, '{dt_str}', '{dt_str}', TRUE);\n")
    f.write("\n")

print("¡Archivo 'inserts_forum.sql' generado exitosamente con datos realistas!")