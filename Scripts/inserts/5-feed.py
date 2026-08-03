import json
import random
from datetime import datetime, timedelta


def escape(text):
    return text.replace("'", "''")


def make_run(text, bold=False, italic=False, underline=False, strikethrough=False,
             size="medium", color="primary", link=None, image_url=None):
    return {
        "bold": bold, "italic": italic, "underline": underline,
        "strikethrough": strikethrough, "size": size, "color": color,
        "text": text, "link": link, "imageUrl": image_url
    }


def make_paragraph(runs, align="left"):
    return {"runs": runs, "align": align}


def make_gallery(images, layout="grid"):
    return {"gallery": {"images": images, "layout": layout}}


def post_to_json(paragraphs, gallery=None):
    obj = {"paragraphs": paragraphs}
    if gallery:
        obj["gallery"] = gallery["gallery"]
    return json.dumps(obj, ensure_ascii=False)


end_dt = datetime(2026, 7, 24, 16, 0, 0)
start_dt = end_dt - timedelta(days=35)


def rand_dt(span_days=35):
    return start_dt + timedelta(seconds=random.randint(0, int((end_dt - start_dt).total_seconds())))


tags_pool = [
    (1, "programacion"), (2, "javascript"), (3, "angular"), (4, "java"),
    (5, "spring"), (6, "aprendizaje"), (7, "logros"), (8, "comunidad"),
    (9, "tutorial"), (10, "typescript"), (11, "python"), (12, "react"),
    (13, "docker"), (14, "devops"), (15, "frontend"), (16, "backend"),
    (17, "ui-ux"), (18, "matematicas"), (19, "algoritmos"), (20, "ia")
]

num_users = 100

# ===== POST TEMPLATES =====
post_templates = [
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("¡", size="xlarge"), make_run("Logro desbloqueado", bold=True, size="xlarge"),
                make_run("!", size="xlarge")
            ], "center"),
            make_paragraph([
                make_run(random.choice([
                    "Terminé mi curso de ", "Acabo de completar ", "Finalmente dominé "
                ])),
                make_run(random.choice([
                    "Spring Boot", "Angular Moderno", "TypeScript Avanzado",
                    "Estructuras de Datos", "React con Hooks", "Docker y DevOps"
                ]), bold=True, color="blue"),
                make_run(random.choice([
                    ". ¡Qué satisfacción aprender algo nuevo cada día!",
                    ". Fueron semanas intensas pero valió la pena.",
                    ". Gracias a la comunidad por tanto contenido útil."
                ]))
            ]),
            make_paragraph([
                make_run("¿Cuál fue su último logro de aprendizaje? ¡Los leo!",
                         italic=True, color="purple")
            ])
        ]),
        "tags": [7, 6, 8]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("Tips de ", bold=True, size="large"),
                make_run(random.choice(["programación", "desarrollo web", "backend"]),
                         bold=True, size="large", color="green")
            ]),
            make_paragraph([
                make_run("Después de varios años codeando, estos son mis consejos:"),
            ]),
            make_paragraph([
                make_run("1. ", bold=True, color="green"),
                make_run(random.choice([
                    "Siempre escribí tests", "Mantené el código simple",
                    "Documentá lo importante", "Aprendé a debugear bien"
                ]))
            ]),
            make_paragraph([
                make_run("2. ", bold=True, color="green"),
                make_run(random.choice([
                    "No te cases con una tecnología", "Leé código de otros",
                    "Practicá todos los días", "Hacé proyectos personales"
                ]))
            ]),
            make_paragraph([
                make_run("3. ", bold=True, color="green"),
                make_run(random.choice([
                    "Entendé los fundamentos primero", "Compartí lo que aprendés",
                    "Participá en comunidades", "No le tengas miedo al refactor"
                ]))
            ]),
            make_paragraph([
                make_run("¿Agregarían algo más?", italic=True, color="purple")
            ])
        ]),
        "tags": [9, 6, 1]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("¡", size="xlarge"), make_run(random.choice([
                    "Nuevo curso publicado", "Acabo de lanzar un curso", "Tengo curso nuevo"
                ]), bold=True, size="xlarge", color="red"), make_run("!", size="xlarge")
            ], "center"),
            make_paragraph([
                make_run(f"Se trata de {random.choice(['programación en Java', 'Angular desde cero', 'TypeScript para principiantes', 'Spring Boot en profundidad'])}. "
                         "Incluye bloques con videos, ejercicios y ejemplos prácticos."),
            ]),
            make_paragraph([
                make_run("Lo encuentran en mi perfil. ", color="primary"),
                make_run("¡Espero que les sirva!", italic=True, color="green")
            ])
        ]),
        "tags": [7, 8, 9]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run(random.choice([
                    "¿Qué están estudiando hoy?", "¿En qué proyecto están trabajando?",
                    "Compartan su setup de estudio"
                ]), bold=True, size="large", color="blue")
            ]),
            make_paragraph([
                make_run(f"Yo estoy con {random.choice(['Java 21', 'Angular Signals', 'Docker Compose', 'React Server Components', 'Python para IA'])}. "
                         "Es increíble la cantidad de recursos que hay en STUDER."),
            ]),
            make_paragraph([
                make_run("Los leo en comentarios ", italic=True, color="purple"),
                make_run("👇", size="large")
            ])
        ]),
        "tags": [8, 6]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("Buenos días comunidad ☀️", size="large", color="orange")
            ]),
            make_paragraph([
                make_run(f"Hoy toca estudiar {random.choice(['algoritmos', 'patrones de diseño', 'bases de datos', 'API REST', 'microservicios'])}. "
                         "Un café y a darle con todo."),
            ]),
            make_paragraph([
                make_run("¿Ustedes cómo arrancan la mañana?", italic=True, color="purple")
            ])
        ]),
        "tags": [8, 6]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("Demo de rich text en STUDER", bold=True, size="large", color="purple")
            ]),
            make_paragraph([
                make_run("negrita, ", bold=True), make_run("cursiva, ", italic=True),
                make_run("subrayado, ", underline=True),
                make_run("tachado", strikethrough=True, bold=True)
            ]),
            make_paragraph([
                make_run("pequeño, ", size="small"),
                make_run("normal, ", size="medium"),
                make_run("grande, ", size="large"),
                make_run("extra grande", size="xlarge")
            ]),
            make_paragraph([
                make_run("colores: ", color="primary"),
                make_run("principal, ", color="primary"),
                make_run("secundario, ", color="secondary"),
                make_run("azul, ", color="blue"),
                make_run("verde, ", color="green"),
                make_run("naranja, ", color="orange"),
                make_run("rojo, ", color="red"),
                make_run("morado", color="purple")
            ]),
            make_paragraph([
                make_run("¡Así de fácil es crear contenido rico en STUDER!"),
            ])
        ]),
        "tags": [9, 1]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("Actualización importante", bold=True, size="large", color="orange")
            ]),
            make_paragraph([
                make_run(f"Actualicé mi bloque de {random.choice(['Java', 'Angular', 'Spring', 'TypeScript'])} "
                         "con nuevos ejemplos y ejercicios prácticos."),
            ]),
            make_paragraph([
                make_run("Versión nueva ya disponible. ¡Pásense a verla!", color="green")
            ])
        ]),
        "tags": [7, 1, 9]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run(random.choice([
                    "Reflexión del día", "Pensamiento random", "Algo que aprendí hoy"
                ]), bold=True, size="large", color="purple")
            ]),
            make_paragraph([
                make_run(random.choice([
                    "Aprender a programar no es memorizar sintaxis, es desarrollar una forma de pensar.",
                    "El mejor código es el que no se escribe. Simplificar siempre es la respuesta.",
                    "No importa cuánto sepas, siempre hay algo nuevo para aprender.",
                    "La comunidad de desarrolladores es una de las más generosas que conozco.",
                    "Fallar es parte del proceso. Cada error te hace mejor programador."
                ]), italic=True),
            ]),
            make_paragraph([
                make_run("¿Coinciden? Los leo ", color="purple")
            ])
        ]),
        "tags": [8, 6]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("¡", size="xlarge"), make_run("Resultados del desafío", bold=True, size="xlarge", color="red"),
                make_run("!", size="xlarge")
            ], "center"),
            make_paragraph([
                make_run(f"Terminó el desafío de programación. ¡Felicitaciones a {random.choice(['@juancito', '@laura_dev', '@coder_pro', '@maria_js'])} "
                         "por ganar con un curso increíble!"),
            ]),
            make_paragraph([
                make_run("Ya pueden ver todos los cursos participantes en la sección de desafíos.", color="green")
            ])
        ]),
        "tags": [8, 7]
    },
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("Pregunta para la comunidad", bold=True, size="large", color="blue")
            ]),
            make_paragraph([
                make_run(f"¿Qué opinan de {random.choice(['microservicios vs monolitos', 'TypeScript sobre JavaScript', 'Spring Boot vs Node.js', 'Docker para desarrollo local', 'testing automatizado'])}?"),
            ]),
            make_paragraph([
                make_run("Quiero leer opiniones con fundamentos. ¡Debatan con respeto!", color="orange")
            ])
        ]),
        "tags": [8, 1]
    },
    # Rich text full demo (same structure user showed)
    {
        "content": lambda: post_to_json([
            make_paragraph([
                make_run("negrita, ", bold=True, size="medium"),
                make_run("italica, ", bold=True, italic=True, size="medium"),
                make_run("subrayado, ", italic=True, underline=True, size="medium"),
                make_run("tachado", bold=True, italic=True, strikethrough=True, size="medium")
            ]),
            make_paragraph([
                make_run("pequeño, ", italic=True, strikethrough=True, size="small"),
                make_run("Normal, ", italic=True, strikethrough=True, size="medium"),
                make_run("grande, ", italic=True, strikethrough=True, size="large"),
                make_run("Extra grande", italic=True, size="xlarge")
            ]),
            make_paragraph([
                make_run("principal, ", italic=True, color="primary"),
                make_run("secundario, ", italic=True, color="secondary"),
                make_run("azul, ", italic=True, color="blue"),
                make_run("verde, ", italic=True, color="green"),
                make_run("naranja, ", italic=True, color="orange"),
                make_run("rojo, ", italic=True, color="red"),
                make_run("morado", italic=True, color="purple"),
                make_run("", color="purple", italic=True,
                         image_url="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400")
            ])
        ], make_gallery([
            {"alt": "Code", "url": "https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=600", "caption": None},
            {"alt": "Dev", "url": "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=600", "caption": None}
        ], "grid")),
        "tags": [9, 1, 2]
    },
]

# ===== GENERATE =====
posts = []
post_id = 1

for _ in range(80):
    tmpl = random.choice(post_templates)
    content_json = tmpl["content"]()
    owner_id = random.randint(1, num_users)
    dt = rand_dt()

    base_tags = list(tmpl["tags"])
    extra_tags = random.sample([t[0] for t in tags_pool if t[0] not in base_tags],
                               k=random.randint(0, 2))
    post_tags = list(set(base_tags + extra_tags))

    posts.append({
        "id": post_id,
        "user_id": owner_id,
        "content": content_json,
        "tags": post_tags,
        "created_datetime": dt,
        "last_updated_datetime": dt,
    })
    post_id += 1

# ===== POST LIKES =====
likes = []
like_id = 1
for p in posts:
    num_likes = random.choices([0, 1, 2, 3, 4, 5, 8, 10, 15],
                               weights=[10, 15, 12, 10, 8, 5, 3, 2, 1])[0]
    users_liked = random.sample(range(1, num_users + 1), min(num_likes, num_users))
    for u in users_liked:
        likes.append({
            "id": like_id,
            "user_id": u,
            "post_id": p["id"],
            "created_datetime": p["created_datetime"] + timedelta(
                minutes=random.randint(5, 2880)),
            "last_updated_datetime": p["created_datetime"]
        })
        like_id += 1

# ===== WRITE SQL =====
with open('inserts_feed.sql', 'w', encoding='utf-8') as f:
    f.write("-- ==========================================\n")
    f.write("-- GENERADOR DE DATOS: FEED (POSTS + LIKES + TAGS)\n")
    f.write(f"-- Posts: {len(posts)}, Likes: {len(likes)}\n")
    f.write("-- ==========================================\n\n")

    f.write("-- 1. POSTS\n")
    for p in posts:
        dt_str = p["created_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO posts (user_id, content, created_datetime, last_updated_datetime, is_active) VALUES "
            f"({p['user_id']}, '{escape(p['content'])}', '{dt_str}', '{dt_str}', TRUE);\n")
    f.write("\n")

    f.write("-- 2. POST_TAGS\n")
    for p in posts:
        for tag_id in p["tags"]:
            f.write(f"INSERT INTO post_tags (post_id, tag_id) VALUES ({p['id']}, {tag_id});\n")
    f.write("\n")

    f.write("-- 3. POST LIKES\n")
    for lk in likes:
        dt_str = lk["created_datetime"].strftime('%Y-%m-%d %H:%M:%S')
        f.write(
            f"INSERT INTO post_likes (user_id, post_id, created_datetime, last_updated_datetime, is_active) VALUES "
            f"({lk['user_id']}, {lk['post_id']}, '{dt_str}', '{dt_str}', TRUE);\n")

print(f"Generados {len(posts)} posts, {len(likes)} likes en 'inserts_feed.sql'.")
