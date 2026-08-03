import subprocess
import sys
import os

os.chdir(os.path.dirname(os.path.abspath(__file__)))

scripts = [
    ("1-users.py", "Usuarios"),
    ("2-friends.py", "Amistades"),
    ("3-chats.py", "Chats y mensajes"),
    ("4-discussions.py", "Foro y discusiones"),
    ("5-feed.py", "Feed (posts, likes, tags)"),
    ("6-blocks-courses.py", "Bloques, versiones, forks y cursos"),
]

print("=" * 50)
print("  STUDER - Generador de datos")
print("=" * 50)
print(f"  Se ejecutaran {len(scripts)} scripts\n")

for script, name in scripts:
    print(f"[{name}] Ejecutando {script}...")
    result = subprocess.run([sys.executable, script], capture_output=True, text=True)
    if result.returncode == 0:
        print(f"  OK - {result.stdout.strip()}")
    else:
        print(f"  ERROR - {result.stderr.strip()}")
    print()

print("=" * 50)
print("  Generacion completada.")
print("  Archivos SQL generados en este directorio.")
print("=" * 50)
