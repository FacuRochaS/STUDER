import random
import unicodedata
from datetime import date, datetime, timedelta

nombres_mujeres = [
    "Sofía", "Valentina", "Isabella", "Camila", "Valeria", "Mariana", "Luciana", "Daniela", "Gabriela", "Victoria",
    "Martina", "Lucía", "Ximena", "Sara", "Samantha", "María", "Fernanda", "Paula", "Natalia", "Catalina",
    "Mía", "Andrea", "Antonella", "Alejandra", "Emilia", "Julia", "Agustina", "Josefina", "Julieta", "Emma",
    "Laura", "Renata", "Clara", "Elena", "Blanca", "Margarita", "Alicia", "Rosa", "Carmen", "Teresa",
    "Luisa", "Ana", "Beatriz", "Gloria", "Silvia", "Patricia", "Diana", "Paola", "Verónica", "Raquel",
    "Carolina", "Lorena", "Mónica", "Adriana", "Florencia", "Rocío", "Micaela", "Constanza", "Belén", "Lourdes",
    "Milagros", "Guadalupe", "Fátima", "Consuelo", "Pilar", "Inés", "Rosario", "Mercedes", "Celia", "Estela",
    "Aurora", "Paloma", "Lola", "Alba", "Olivia", "Alma", "Luna", "Zoe", "Chloe", "Aitana",
    "Noa", "Lara", "Marta", "Irene", "Eva", "Ángela", "Rafaela", "Marisol", "Cecilia", "Bárbara",
    "Jimena", "Ivana", "Mireya", "Elisa", "Amalia", "Esther", "Lidia", "Nuria", "Cristina", "Marina"
]

nombres_hombres = [
    "Santiago", "Mateo", "Sebastián", "Alejandro", "Matías", "Diego", "Samuel", "Nicolás", "Daniel", "Martín",
    "Tomás", "Joaquín", "Lucas", "Gabriel", "Emilio", "Andrés", "Ignacio", "Benjamín", "Leonardo", "Felipe",
    "Bautista", "Eduardo", "Fernando", "Carlos", "Francisco", "Juan", "Luis", "José", "Miguel", "Ángel",
    "Pedro", "Antonio", "Pablo", "Ricardo", "Roberto", "Enrique", "Javier", "Jorge", "Guillermo", "Raúl",
    "Alberto", "Víctor", "Héctor", "Manuel", "Arturo", "Gerardo", "Armando", "Rubén", "Gustavo", "Oscar",
    "Hugo", "Rodrigo", "César", "Mario", "Mauricio", "Marcos", "Gonzalo", "Bruno", "Thiago", "Ian",
    "Dylan", "Gael", "Axel", "Leo", "Enzo", "Romeo", "Ciro", "Milo", "Noah", "Liam",
    "Ethan", "Oliver", "Alexander", "Simón", "Facundo", "Lautaro", "Agustín", "Maximiliano", "Valentín", "Jerónimo",
    "Lorenzo", "Vicente", "Cristóbal", "Alonso", "Patricio", "Marcelo", "Álvaro", "Ramón", "Julio", "Omar",
    "Iván", "Darío", "Esteban", "Federico", "Braian", "Kevin", "Alan", "Ariel", "Ezequiel", "Damián"
]

apellidos = [
    "García", "González", "Rodríguez", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Martín",
    "Jiménez", "Ruiz", "Hernández", "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero", "Alonso", "Gutiérrez",
    "Navarro", "Torres", "Domínguez", "Vázquez", "Ramos", "Gil", "Ramírez", "Serrano", "Blanco", "Molina",
    "Morales", "Suárez", "Ortega", "Delgado", "Castro", "Ortiz", "Rubio", "Marín", "Sanz", "Núñez",
    "Iglesias", "Medina", "Garrido", "Cortés", "Castillo", "Santos", "Lozano", "Guerrero", "Cano", "Prieto",
    "Méndez", "Cruz", "Calvo", "Gallego", "Vidal", "León", "Márquez", "Herrera", "Peña", "Flores",
    "Cabrera", "Campos", "Vega", "Fuentes", "Carrasco", "Diez", "Reyes", "Cabello", "Aguilar", "Pascual",
    "Santana", "Herrero", "Lorenzo", "Montero", "Hidalgo", "Giménez", "Ibáñez", "Ferrer", "Durán", "Santiago",
    "Benítez", "Mora", "Vargas", "Arias", "Carmona", "Vicente", "Rojas", "Soto", "Crespo", "Román",
    "Pastor", "Velasco", "Sáez", "Moya", "Soler", "Parra", "Bravo", "Gallardo", "Acosta", "Mendoza"
]


def remove_accents(input_str):

    nfkd_form = unicodedata.normalize('NFKD', input_str)
    return u"".join([c for c in nfkd_form if not unicodedata.combining(c)])


class UserEntity:
    def __init__(self, first_name, last_name, profile_url):
        self.first_name = first_name
        self.last_name = last_name

        clean_first = remove_accents(first_name).lower()
        clean_last = remove_accents(last_name).lower()

        self.username = f"{first_name}{last_name}".replace(" ", "")
        self.email = f"{clean_last}{clean_first}@gmail.com"


        start_date = date(1990, 1, 1)
        end_date = date(2006, 12, 31)
        delta = end_date - start_date
        self.birth_date = start_date + timedelta(days=random.randrange(delta.days))


        self.password = "$2a$10$DUMMYHASHFORTESTINGPURPOSESONLY.1234567890abcdef"
        self.last_connection_time = datetime.utcnow()


        self.profile_picture_original_url = profile_url
        self.profile_picture_avatar_url = profile_url
        self.profile_picture_webp_url = profile_url
        self.profile_picture_thumbnail_url = profile_url


        self.created_datetime = datetime.utcnow()
        self.last_updated_datetime = self.created_datetime
        self.is_active = True

    def to_sql(self):

        return (f"INSERT INTO users (first_name, last_name, username, birth_date, email, password, "
                f"last_connection_time, profile_picture_original_url, profile_picture_avatar_url, "
                f"profile_picture_webp_url, profile_picture_thumbnail_url, created_datetime, "
                f"last_updated_datetime, is_active) VALUES ('{self.first_name}', '{self.last_name}', "
                f"'{self.username}', '{self.birth_date.strftime('%Y-%m-%d')}', '{self.email}', '{self.password}', "
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