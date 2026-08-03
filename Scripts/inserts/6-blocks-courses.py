import json
import random
import uuid
from datetime import datetime, timedelta


def escape(text):
    return text.replace("'", "''")


def rand_id():
    return str(uuid.uuid4())


# ===== DATES =====
end_dt = datetime(2026, 7, 24, 16, 0, 0)
start_dt = end_dt - timedelta(days=180)


def rand_dt(span_days=180):
    return start_dt + timedelta(seconds=random.randint(0, int((end_dt - start_dt).total_seconds())))


def fmt_dt(dt):
    return dt.strftime('%Y-%m-%d %H:%M:%S.%f')


# ===== NAMES POOL =====
block_names_templates = [
    # Programacion general
    ("Introduccion a {}", ["Programacion", "Algoritmos", "POO", "Estructuras de Datos"]),
    ("{} desde Cero", ["Java", "Python", "JavaScript", "TypeScript", "C++", "Go"]),
    ("{} Avanzado", ["Java", "Python", "TypeScript", "Spring Boot", "React"]),
    ("Patrones de Diseño en Java", ["Singleton", "Factory", "Observer", "Strategy"]),
    ("Patrones de Arquitectura", ["MVC", "Microservicios", "Event-Driven", "CQRS"]),
    # Java / Spring
    ("Colecciones en Java: {}", ["ArrayList y LinkedList", "HashMap y TreeMap", "Streams y Lambdas"]),
    ("Spring Boot: {}", ["Controllers REST", "JPA y Repositories", "Seguridad con JWT", "Testing con JUnit"]),
    ("{} en Java", ["Manejo de Excepciones", "Hilos y Concurrencia", "Genéricos", "Anotaciones"]),
    # Angular / Frontend
    ("Angular: {}", ["Componentes Standalone", "Signals y Computed", "Formularios Reactivos", "Routing Avanzado"]),
    ("TypeScript: {}", ["Tipos Avanzados", "Genéricos", "Decorators", "Utility Types"]),
    ("CSS Moderno: {}", ["Flexbox", "Grid Layout", "Animaciones", "Variables y Temas"]),
    # DB / DevOps
    ("Bases de Datos: {}", ["SQL Avanzado", "Índices y Optimización", "Transacciones", "PostgreSQL"]),
    ("Docker: {}", ["Dockerfiles Multi-stage", "Docker Compose", "Redes y Volúmenes"]),
    # Matematicas / Algoritmos
    ("Algoritmos: {}", ["Ordenamiento", "Búsqueda", "Grafos", "Programación Dinámica"]),
    ("Matemáticas: {}", ["Álgebra Lineal", "Cálculo Diferencial", "Estadística", "Probabilidad"]),
]

level_subjects = {
    "EASY": ["Introduccion", "desde Cero", "Conceptos Básicos", "Fundamentos"],
    "NORMAL": ["Intermedio", "Aplicado", "Casos de Uso", "Guía Práctica"],
    "HARD": ["Avanzado", "a Profundidad", "Optimización", "Arquitectura"],
    "EXPERT": ["Experto", "Masterclass", "a Escala", "Producción"]
}

difficulties = ["EASY", "NORMAL", "HARD", "EXPERT"]
difficulty_weights = [30, 40, 22, 8]

num_users = 100

# ===== CONTENT GENERATORS =====

def make_text_content():
    """Generate TextContentData as BlockContentItem array"""
    paragraphs = []
    num_paras = random.randint(1, 4)

    titles = ["Introducción", "Conceptos clave", "Desarrollo del tema", "Ejemplos prácticos",
              "Resumen", "Para tener en cuenta", "Caso de uso", "Implementación"]

    for i in range(num_paras):
        runs = []
        if i == 0:
            runs.append(make_run(random.choice(titles), bold=True, size="large"))
        else:
            lines = [
                "En esta sección exploramos los conceptos fundamentales del tema.",
                "Veamos un ejemplo práctico para entender mejor cómo funciona.",
                "Es importante recordar que cada tecnología tiene sus particularidades.",
                "A continuación, analizamos los casos de uso más comunes.",
                "La práctica constante es la clave para dominar cualquier concepto.",
                "Recomendamos complementar este bloque con ejercicios prácticos.",
                "Este patrón se utiliza ampliamente en el desarrollo profesional.",
                "Una buena práctica es siempre documentar el código que escribimos.",
            ]
            text = random.choice(lines)
            # Mix some styled words
            parts = text.split(" ")
            for j, word in enumerate(parts):
                if j % 4 == 0 and random.random() < 0.3:
                    runs.append(make_run(word + " ", bold=True, color="blue"))
                elif j % 5 == 0 and random.random() < 0.2:
                    runs.append(make_run(word + " ", italic=True, color="green"))
                else:
                    runs.append(make_run(word + " "))
        paragraphs.append(make_paragraph(runs))

    return [make_block_item("text", {"paragraphs": paragraphs})]


def make_run(text="", bold=False, italic=False, underline=False, strikethrough=False,
             size="medium", color="primary", link=None, image_url=None):
    return {"bold": bold, "italic": italic, "underline": underline,
            "strikethrough": strikethrough, "size": size, "color": color,
            "text": text, "link": link, "imageUrl": image_url}


def make_paragraph(runs, align="left"):
    return {"runs": runs, "align": align}


def make_block_item(block_type, data):
    return {"id": rand_id(), "type": block_type, "data": data}


def make_activity_content():
    """Generate ActivityContentData as BlockContentItem"""
    activity_types = ["multiple_choice", "matching", "ordering", "drag_and_drop"]
    atype = random.choice(activity_types)

    questions = {
        "multiple_choice": [
            ("¿Cuál es la respuesta correcta?", [
                ("Opción A - Incorrecta", False), ("Opción B - Correcta", True),
                ("Opción C - Incorrecta", False), ("Opción D - Incorrecta", False)]),
            ("Selecciona la afirmación verdadera:", [
                ("Afirmación falsa 1", False), ("Afirmación verdadera", True),
                ("Afirmación falsa 2", False), ("Todas son falsas", False)]),
            ("¿Qué valor retorna el método?", [
                ("null", False), ("0", False), ("42", True),
                ("undefined", False)]),
        ],
        "matching": [
            ("Unir conceptos con sus definiciones:", [
                ("Clase", "Plantilla para crear objetos"),
                ("Objeto", "Instancia de una clase"),
                ("Método", "Función dentro de una clase"),
                ("Atributo", "Variable de instancia")]),
            ("Relacionar tecnologías:", [
                ("Angular", "Framework frontend"),
                ("Spring Boot", "Framework backend"),
                ("PostgreSQL", "Base de datos"),
                ("Docker", "Contenedores")]),
        ],
        "ordering": [
            ("Ordena los pasos correctamente:", [
                ("Paso 1: Análisis de requisitos", 1),
                ("Paso 2: Diseño de la solución", 2),
                ("Paso 3: Implementación", 3),
                ("Paso 4: Testing", 4),
                ("Paso 5: Deploy", 5)]),
            ("Ordena de menor a mayor complejidad:", [
                ("EASY", 1), ("NORMAL", 2), ("HARD", 3), ("EXPERT", 4)]),
        ],
        "drag_and_drop": [
            ("Arrastra cada elemento a su categoría:", [
                ("Java", "Lenguajes compilados"),
                ("Python", "Lenguajes interpretados"),
                ("HTML", "Lenguajes de marcado"),
                ("SQL", "Lenguajes de consulta")]),
        ],
    }

    if atype not in questions:
        atype = "multiple_choice"

    question, raw_options = random.choice(questions[atype])
    options = []
    for opt_text, opt_val in raw_options:
        opt = {"id": rand_id(), "text": opt_text, "imageUrl": None,
               "isCorrect": None, "matchText": None, "orderIndex": None}
        if atype == "multiple_choice":
            opt["isCorrect"] = opt_val
        elif atype == "matching":
            opt["matchText"] = str(opt_val)
        elif atype == "ordering":
            opt["orderIndex"] = opt_val
        elif atype == "drag_and_drop":
            opt["matchText"] = str(opt_val)
        options.append(opt)

    return [make_block_item("activity", {
        "activityType": atype,
        "statement": [{"type": "text", "value": question}],
        "options": options,
        "allowRetry": True,
        "showFeedback": True
    })]


def make_video_content():
    """Generate VideoContentData as BlockContentItem"""
    urls = [
        ("https://www.youtube.com/watch?v=8F4AoHbAFVY", "youtube"),
        ("https://www.youtube.com/watch?v=dQw4w9WgXcQ", "youtube"),
        ("https://www.youtube.com/watch?v=jNQXAC9IVRw", "youtube"),
        ("https://vimeo.com/148751763", "vimeo"),
        ("https://vimeo.com/253989945", "vimeo"),
    ]
    url, platform = random.choice(urls)
    return [make_block_item("video", {
        "url": url, "platform": platform,
        "autoplay": False, "controls": True, "startTime": None
    })]


def make_gallery_content():
    """Generate GalleryContentData as BlockContentItem"""
    img_pool = [
        "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800",
        "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800",
        "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800",
        "https://images.unsplash.com/photo-1504639725590-34d0984388bd?w=800",
        "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?w=800",
        "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
        "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800",
        "https://images.unsplash.com/photo-1515879218367-8466d910aecc?w=800",
        "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800",
        "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800",
        "https://images.unsplash.com/photo-1499951360447-b19be8fe80f5?w=800",
        "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800",
        "https://images.unsplash.com/photo-1483058712412-4245e9b90334?w=800",
        "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800",
        "https://images.unsplash.com/photo-1504893524553-b855bce32c67?w=800",
    ]
    selected = random.sample(img_pool, min(len(img_pool), random.randint(1, 3)))
    return [make_block_item("gallery", {
        "images": [{"url": u, "alt": "Gallery image", "caption": None} for u in selected],
        "layout": random.choice(["grid", "carousel", "masonry"])
    })]


content_generators = [make_text_content, make_text_content, make_text_content,
                       make_activity_content, make_video_content, make_gallery_content]


def slugify(name):
    return name.lower().replace(" ", "-").replace("á", "a").replace("é", "e").replace("í", "i") \
        .replace("ó", "o").replace("ú", "u").replace("ñ", "n").replace(":", "").replace("(", "") \
        .replace(")", "").replace(",", "")


# ===== GENERATE =====
blocks = []
versions = []
ver_id = 1

num_blocks = 200
used_slugs = set()

for bid in range(1, num_blocks + 1):
    owner_id = random.randint(1, num_users)
    diff = random.choices(difficulties, weights=difficulty_weights, k=1)[0]
    tmpl, subjects = random.choice(block_names_templates)
    subject = random.choice(subjects)
    name = tmpl.format(subject)
    slug_base = slugify(name)
    slug = slug_base
    counter = 1
    while slug in used_slugs:
        slug = f"{slug_base}-{counter}"
        counter += 1
    used_slugs.add(slug)
    dt = rand_dt()

    blocks.append({
        "id": bid,
        "name": name,
        "slug": slug,
        "difficulty": diff,
        "owner_id": owner_id,
        "created_datetime": dt,
        "last_updated_datetime": dt,
        "parent_block_id": None,
        "root_block_id": None,
    })

    # Generate 1-3 versions per block
    num_vers = random.choices([1, 2, 3], weights=[65, 25, 10], k=1)[0]
    for vn in range(1, num_vers + 1):
        v_dt = dt + timedelta(days=random.randint(0, 30))
        if v_dt > end_dt:
            v_dt = end_dt
        gen = random.choice(content_generators)
        content = json.dumps(gen(), ensure_ascii=False)
        published = random.random() < 0.8
        desc = "Versión original" if vn == 1 else random.choice(
            ["Correcciones menores", "Agregados ejemplos", "Mejoras de contenido",
             "Fix de typos", "Versión actualizada"])

        versions.append({
            "id": ver_id,
            "block_id": bid,
            "version_number": vn,
            "content": content,
            "published": published,
            "change_description": desc,
            "created_datetime": v_dt,
            "last_updated_datetime": v_dt,
        })
        ver_id += 1

    # Set current_version to latest published, or first version
    block_vers = [v for v in versions if v["block_id"] == bid]
    published_vers = [v for v in block_vers if v["published"]]
    cv = published_vers[-1] if published_vers else block_vers[0]
    blocks[-1]["current_version_id"] = cv["id"]

# ===== FORKS =====
fork_count = 0
for i in range(num_blocks):
    if random.random() < 0.15 and blocks[i]["parent_block_id"] is None:
        forker_id = random.randint(1, num_users)
        if forker_id == blocks[i]["owner_id"]:
            continue
        orig = blocks[i]
        name = orig["name"] + " (fork)"
        slug_base = orig["slug"] + "-fork"
        slug = slug_base
        counter = 1
        while slug in used_slugs:
            slug = f"{slug_base}-{counter}"
            counter += 1
        used_slugs.add(slug)
        dt = rand_dt()
        bid = num_blocks + fork_count + 1

        blocks.append({
            "id": bid,
            "name": name,
            "slug": slug,
            "difficulty": orig["difficulty"],
            "owner_id": forker_id,
            "created_datetime": dt,
            "last_updated_datetime": dt,
            "parent_block_id": orig["id"],
            "root_block_id": orig["root_block_id"] or orig["id"],
        })

        v_dt = dt
        gen = random.choice(content_generators)
        content = json.dumps(gen(), ensure_ascii=False)
        versions.append({
            "id": ver_id,
            "block_id": bid,
            "version_number": 1,
            "content": content,
            "published": random.random() < 0.7,
            "change_description": "Fork de " + orig["name"],
            "created_datetime": v_dt,
            "last_updated_datetime": v_dt,
        })
        blocks[-1]["current_version_id"] = ver_id
        ver_id += 1
        fork_count += 1

# ===== BLOCK LIKES =====
block_likes = []
like_id = 1
for b in blocks:
    n = random.choices([0, 1, 2, 3, 5, 8, 12], weights=[20, 25, 18, 12, 8, 5, 2], k=1)[0]
    likers = random.sample(range(1, num_users + 1), min(n, num_users))
    for u in likers:
        block_likes.append({
            "id": like_id, "user_id": u, "block_id": b["id"],
            "created_datetime": b["created_datetime"] + timedelta(days=random.randint(0, 60)),
            "last_updated_datetime": b["created_datetime"]
        })
        like_id += 1

# ===== COURSES =====
courses = []
course_id = 1

course_templates = [
    "{}: De Cero a Experto", "{} Completo", "{} en la Práctica",
    "Aprendiendo {}", "{} para Principiantes", "Curso de {}", "Masterclass de {}",
    "{} Paso a Paso", "Domina {}", "{} Profesional"
]

course_subjects = [
    "Java", "Spring Boot", "Angular", "TypeScript", "Desarrollo Web",
    "Programación Orientada a Objetos", "Algoritmos y Estructuras de Datos",
    "Bases de Datos SQL", "Docker y DevOps", "React desde Cero",
    "Python para Ciencia de Datos", "Microservicios", "APIs REST",
    "Testing Automatizado", "Git y Control de Versiones", "Arquitectura de Software",
    "JavaScript Moderno", "Desarrollo Frontend", "Backend con Node.js",
    "Seguridad Informática"
]

# Each course gets 3-7 blocks
available_blocks = [b for b in blocks if b["parent_block_id"] is None]
random.shuffle(available_blocks)
used_blocks = set()

for _ in range(60):
    owner_id = random.randint(1, num_users)
    tmpl = random.choice(course_templates)
    subject = random.choice(course_subjects)
    name = tmpl.format(subject)
    slug_base = slugify(name)
    slug = slug_base
    counter = 1
    while slug in used_slugs:
        slug = f"{slug_base}-{counter}"
        counter += 1
    used_slugs.add(slug)
    dt = rand_dt()

    num_blocks = random.randint(3, 7)
    course_blocks = []
    for b in available_blocks:
        if len(course_blocks) >= num_blocks:
            break
        if b["id"] not in used_blocks or random.random() < 0.3:
            course_blocks.append(b["id"])
            used_blocks.add(b["id"])

    if len(course_blocks) < 2:
        continue

    courses.append({
        "id": course_id,
        "name": name,
        "slug": slug,
        "owner_id": owner_id,
        "published": random.random() < 0.85,
        "rating_sum": random.randint(0, 50),
        "rating_count": random.randint(0, 12),
        "created_datetime": dt,
        "last_updated_datetime": dt,
        "blocks": course_blocks,
    })
    course_id += 1

# ===== WRITE SQL =====
with open('inserts_blocks.sql', 'w', encoding='utf-8') as f:
    f.write("-- ==========================================\n")
    f.write(f"-- GENERADOR: BLOQUES, VERSIONES, FORKS, CURSOS\n")
    f.write(f"-- Bloques: {len(blocks)}, Versiones: {len(versions)}, Cursos: {len(courses)}\n")
    f.write("-- ==========================================\n\n")

    f.write("-- 1. BLOCKS (current_version_id = NULL, actualizado luego)\n")
    for b in blocks:
        parent = b['parent_block_id'] or 'NULL'
        root = b['root_block_id'] or 'NULL'
        f.write(
            f"INSERT INTO blocks (id, name, slug, difficulty, owner_id, current_version_id, "
            f"parent_block_id, root_block_id, created_datetime, last_updated_datetime, is_active) VALUES ("
            f"{b['id']}, '{escape(b['name'])}', '{escape(b['slug'])}', '{b['difficulty']}', "
            f"{b['owner_id']}, NULL, {parent}, {root}, "
            f"'{fmt_dt(b['created_datetime'])}', '{fmt_dt(b['last_updated_datetime'])}', TRUE);\n")
    f.write("\n")

    f.write("-- 2. BLOCKS_VERSIONS\n")
    for v in versions:
        pub = 'TRUE' if v['published'] else 'FALSE'
        f.write(
            f"INSERT INTO blocks_versions (id, block_id, version_number, content, published, "
            f"change_description, created_datetime, last_updated_datetime, is_active) VALUES ("
            f"{v['id']}, {v['block_id']}, {v['version_number']}, "
            f"'{escape(v['content'])}', {pub}, '{escape(v['change_description'])}', "
            f"'{fmt_dt(v['created_datetime'])}', '{fmt_dt(v['last_updated_datetime'])}', TRUE);\n")
    f.write("\n")

    f.write("-- 2b. UPDATE current_version_id\n")
    for b in blocks:
        cv = b.get('current_version_id')
        if cv:
            f.write(f"UPDATE blocks SET current_version_id = {cv} WHERE id = {b['id']};\n")
    f.write("\n")

    f.write("-- 3. BLOCK LIKES\n")
    for lk in block_likes:
        f.write(
            f"INSERT INTO block_likes (id, user_id, block_id, created_datetime, "
            f"last_updated_datetime, is_active) VALUES ("
            f"{lk['id']}, {lk['user_id']}, {lk['block_id']}, "
            f"'{fmt_dt(lk['created_datetime'])}', '{fmt_dt(lk['last_updated_datetime'])}', TRUE);\n")
    f.write("\n")

    f.write("-- 4. COURSES\n")
    for c in courses:
        f.write(
            f"INSERT INTO courses (id, owner_id, name, slug, published, link, rating_sum, "
            f"rating_count, contest_hidden, created_datetime, last_updated_datetime, is_active) VALUES ("
            f"{c['id']}, {c['owner_id']}, '{escape(c['name'])}', '{escape(c['slug'])}', "
            f"{'TRUE' if c['published'] else 'FALSE'}, '', {c['rating_sum']}, {c['rating_count']}, "
            f"FALSE, '{fmt_dt(c['created_datetime'])}', '{fmt_dt(c['last_updated_datetime'])}', TRUE);\n")
    f.write("\n")

    f.write("-- 5. COURSE_BLOCKS\n")
    cb_id = 1
    for c in courses:
        for order, b_id in enumerate(c["blocks"], 1):
            f.write(
                f"INSERT INTO course_blocks (id, course_id, block_id, block_order, "
                f"created_datetime, last_updated_datetime, is_active) VALUES ("
                f"{cb_id}, {c['id']}, {b_id}, {order}, "
                f"'{fmt_dt(c['created_datetime'])}', '{fmt_dt(c['last_updated_datetime'])}', TRUE);\n")
            cb_id += 1
    f.write("\n")

    f.write("-- 6. COURSE FAVORITES\n")
    fav_id = 1
    for c in courses:
        n = random.choices([0, 1, 2, 3], weights=[15, 30, 20, 10], k=1)[0]
        favers = random.sample(range(1, num_users + 1), min(n, num_users))
        for u in favers:
            f.write(
                f"INSERT INTO user_course_favs (id, user_id, course_id, created_datetime, "
                f"last_updated_datetime, is_active) VALUES ("
                f"{fav_id}, {u}, {c['id']}, '{fmt_dt(c['created_datetime'])}', "
                f"'{fmt_dt(c['last_updated_datetime'])}', TRUE);\n")
            fav_id += 1

    f.write("\n-- 7. SYNC SEQUENCES (evita conflictos con nuevos inserts)\n")
    f.write("SELECT setval('blocks_id_seq', (SELECT COALESCE(MAX(id), 1) FROM blocks));\n")
    f.write("SELECT setval('blocks_versions_id_seq', (SELECT COALESCE(MAX(id), 1) FROM blocks_versions));\n")
    f.write("SELECT setval('block_likes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM block_likes));\n")
    f.write("SELECT setval('courses_id_seq', (SELECT COALESCE(MAX(id), 1) FROM courses));\n")
    f.write("SELECT setval('course_blocks_id_seq', (SELECT COALESCE(MAX(id), 1) FROM course_blocks));\n")
    f.write("SELECT setval('user_course_favs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM user_course_favs));\n")

print(f"Generados {len(blocks)} bloques ({fork_count} forks), {len(versions)} versiones, "
      f"{len(courses)} cursos, {len(block_likes)} likes.")
print("Archivo: inserts_blocks.sql")
