import random
import unicodedata
from datetime import date, datetime, timedelta

nombres_mujeres = [
    "Sofia", "Valentina", "Isabella", "Camila", "Valeria", "Mariana", "Luciana", "Daniela", "Gabriela", "Victoria",
    "Martina", "Lucia", "Ximena", "Sara", "Samantha", "Maria", "Fernanda", "Paula", "Natalia", "Catalina",
    "Mia", "Andrea", "Antonella", "Alejandra", "Emilia", "Julia", "Agustina", "Josefina", "Julieta", "Emma",
    "Laura", "Renata", "Clara", "Elena", "Blanca", "Margarita", "Alicia", "Rosa", "Carmen", "Teresa",
    "Luisa", "Ana", "Beatriz", "Gloria", "Silvia", "Patricia", "Diana", "Paola", "Veronica", "Raquel",
    "Carolina", "Lorena", "Monica", "Adriana", "Florencia", "Rocio", "Micaela", "Constanza", "Belen", "Lourdes",
    "Milagros", "Guadalupe", "Fatima", "Consuelo", "Pilar", "Ines", "Rosario", "Mercedes", "Celia", "Estela",
    "Aurora", "Paloma", "Lola", "Alba", "Olivia", "Alma", "Luna", "Zoe", "Chloe", "Aitana",
    "Noa", "Lara", "Marta", "Irene", "Eva", "Angela", "Rafaela", "Marisol", "Cecilia", "Barbara",
    "Jimena", "Ivana", "Mireya", "Elisa", "Amalia", "Esther", "Lidia", "Nuria", "Cristina", "Marina"
]

nombres_hombres = [
    "Santiago", "Mateo", "Sebastian", "Alejandro", "Matias", "Diego", "Samuel", "Nicolas", "Daniel", "Martin",
    "Tomas", "Joaquin", "Lucas", "Gabriel", "Emilio", "Andres", "Ignacio", "Benjamin", "Leonardo", "Felipe",
    "Bautista", "Eduardo", "Fernando", "Carlos", "Francisco", "Juan", "Luis", "Jose", "Miguel", "Angel",
    "Pedro", "Antonio", "Pablo", "Ricardo", "Roberto", "Enrique", "Javier", "Jorge", "Guillermo", "Raul",
    "Alberto", "Victor", "Hector", "Manuel", "Arturo", "Gerardo", "Armando", "Ruben", "Gustavo", "Oscar",
    "Hugo", "Rodrigo", "Cesar", "Mario", "Mauricio", "Marcos", "Gonzalo", "Bruno", "Thiago", "Ian",
    "Dylan", "Gael", "Axel", "Leo", "Enzo", "Romeo", "Ciro", "Milo", "Noah", "Liam",
    "Ethan", "Oliver", "Alexander", "Simon", "Facundo", "Lautaro", "Agustin", "Maximiliano", "Valentin", "Jeronimo",
    "Lorenzo", "Vicente", "Cristobal", "Alonso", "Patricio", "Marcelo", "Alvaro", "Ramon", "Julio", "Omar",
    "Ivan", "Dario", "Esteban", "Federico", "Braian", "Kevin", "Alan", "Ariel", "Ezequiel", "Damian"
]

apellidos = [
    "Garcia", "Gonzalez", "Rodriguez", "Fernandez", "Lopez", "Martinez", "Sanchez", "Perez", "Gomez", "Martin",
    "Jimenez", "Ruiz", "Hernandez", "Diaz", "Moreno", "Munoz", "Alvarez", "Romero", "Alonso", "Gutierrez",
    "Navarro", "Torres", "Dominguez", "Vazquez", "Ramos", "Gil", "Ramirez", "Serrano", "Blanco", "Molina",
    "Morales", "Suarez", "Ortega", "Delgado", "Castro", "Ortiz", "Rubio", "Marin", "Sanz", "Nunez",
    "Iglesias", "Medina", "Garrido", "Cortes", "Castillo", "Santos", "Lozano", "Guerrero", "Cano", "Prieto",
    "Mendez", "Cruz", "Calvo", "Gallego", "Vidal", "Leon", "Marquez", "Herrera", "Pena", "Flores",
    "Cabrera", "Campos", "Vega", "Fuentes", "Carrasco", "Diez", "Reyes", "Cabello", "Aguilar", "Pascual",
    "Santana", "Herrero", "Lorenzo", "Montero", "Hidalgo", "Gimenez", "Ibanez", "Ferrer", "Duran", "Santiago",
    "Benitez", "Mora", "Vargas", "Arias", "Carmona", "Vicente", "Rojas", "Soto", "Crespo", "Roman",
    "Pastor", "Velasco", "Saez", "Moya", "Soler", "Parra", "Bravo", "Gallardo", "Acosta", "Mendoza"
]

end_dt = datetime(2026, 7, 24, 16, 0, 0)
start_dt = end_dt - timedelta(days=365)


def rand_dt():
    return start_dt + timedelta(seconds=random.randint(0, int((end_dt - start_dt).total_seconds())))


def remove_accents(input_str):
    nfkd_form = unicodedata.normalize('NFKD', input_str)
    return u"".join([c for c in nfkd_form if not unicodedata.combining(c)])


class UserEntity:
    def __init__(self, first_name, last_name, profile_url):
        self.first_name = first_name
        self.last_name = last_name

        clean_first = remove_accents(first_name).lower()
        clean_last = remove_accents(last_name).lower()

        n = random.randint(1, 100)
        self.username = f"{clean_first}{clean_last}{n}".replace(" ", "")
        self.email = f"{clean_last}{clean_first}{n}@gmail.com"

        start_date = date(1990, 1, 1)
        end_date = date(2006, 12, 31)
        delta = end_date - start_date
        self.birth_date = start_date + timedelta(days=random.randrange(delta.days))

        self.password = "$2a$10$DUMMYHASHFORTESTINGPURPOSESONLY.1234567890abcdef"

        self.created_datetime = rand_dt()
        self.last_updated_datetime = self.created_datetime
        self.last_connection_time = self.created_datetime + timedelta(days=random.randint(0, 180))
        if self.last_connection_time > end_dt:
            self.last_connection_time = end_dt

        self.profile_picture_original_url = profile_url
        self.profile_picture_avatar_url = profile_url
        self.profile_picture_webp_url = profile_url
        self.profile_picture_thumbnail_url = profile_url

        self.points = self._gen_points()
        self.role = "ADMIN" if random.random() < 0.03 else "USER"
        self.is_active = True

    def _gen_points(self):
        r = random.random()
        if r < 0.10:
            return random.randint(0, 50)
        elif r < 0.30:
            return random.randint(50, 200)
        elif r < 0.60:
            return random.randint(200, 800)
        elif r < 0.85:
            return random.randint(800, 3000)
        else:
            return random.randint(3000, 15000)

    def to_sql(self):
        return (f"INSERT INTO users (first_name, last_name, username, birth_date, email, password, "
                f"role, points, last_connection_time, profile_picture_original_url, profile_picture_avatar_url, "
                f"profile_picture_webp_url, profile_picture_thumbnail_url, created_datetime, "
                f"last_updated_datetime, is_active) VALUES ('{self.first_name}', '{self.last_name}', "
                f"'{self.username}', '{self.birth_date.strftime('%Y-%m-%d')}', '{self.email}', '{self.password}', "
                f"'{self.role}', {self.points}, "
                f"'{self.last_connection_time.strftime('%Y-%m-%d %H:%M:%S')}', '{self.profile_picture_original_url}', "
                f"'{self.profile_picture_avatar_url}', '{self.profile_picture_webp_url}', "
                f"'{self.profile_picture_thumbnail_url}', '{self.created_datetime.strftime('%Y-%m-%d %H:%M:%S')}', "
                f"'{self.last_updated_datetime.strftime('%Y-%m-%d %H:%M:%S')}', TRUE);")


users = []
cantidad_usuarios = 100

for i in range(cantidad_usuarios):
    if i % 2 == 0:
        name = random.choice(nombres_mujeres)
        url = f"https://randomuser.me/api/portraits/women/{random.randint(0, 99)}.jpg"
    else:
        name = random.choice(nombres_hombres)
        url = f"https://randomuser.me/api/portraits/men/{random.randint(0, 99)}.jpg"

    last_name = random.choice(apellidos)
    users.append(UserEntity(name, last_name, url))

with open('inserts_users.sql', 'w', encoding='utf-8') as f:
    f.write("-- Generador de Inserts para tabla 'users'\n")
    for u in users:
        f.write(u.to_sql() + "\n")

print(f"Se generaron {cantidad_usuarios} inserts exitosamente.")
print("Incluye: puntos (distribucion realista), roles (3% ADMIN), fechas variadas.")
