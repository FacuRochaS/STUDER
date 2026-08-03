import random
from datetime import datetime, timedelta

end_dt = datetime(2026, 7, 24, 16, 0, 0)
start_dt = end_dt - timedelta(days=180)


def rand_dt():
    return start_dt + timedelta(seconds=random.randint(0, int((end_dt - start_dt).total_seconds())))


class ChatEntity:
    def __init__(self, chat_id, user_1, user_2):
        self.chat_id = chat_id
        self.user_1 = user_1
        self.user_2 = user_2

        self.created_datetime = rand_dt()
        self.last_updated_datetime = self.created_datetime

    def to_sql(self):

        return (f"INSERT INTO chats (user_1_id, user_2_id, created_datetime, last_updated_datetime, is_active) "
                f"VALUES ({self.user_1}, {self.user_2}, "
                f"'{self.created_datetime.strftime('%Y-%m-%d %H:%M:%S')}', "
                f"'{self.last_updated_datetime.strftime('%Y-%m-%d %H:%M:%S')}', TRUE);")


class MessageEntity:
    def __init__(self, msg_id, chat_id, sender_id, content, link, reply_to_id, is_read, dt):
        self.msg_id = msg_id
        self.chat_id = chat_id
        self.sender_id = sender_id
        self.content = content
        self.link = link
        self.reply_to_id = reply_to_id if reply_to_id else "NULL"
        self.is_read = is_read
        self.created_datetime = dt
        self.last_updated_datetime = dt

    def to_sql(self):

        safe_content = self.content.replace("'", "''")
        return (f"INSERT INTO messages (chat_id, sender_id, content, link, reply_to_id, is_read, "
                f"created_datetime, last_updated_datetime, is_active) VALUES "
                f"({self.chat_id}, {self.sender_id}, '{safe_content}', '{self.link}', "
                f"{self.reply_to_id}, {'TRUE' if self.is_read else 'FALSE'}, "
                f"'{self.created_datetime.strftime('%Y-%m-%d %H:%M:%S')}', "
                f"'{self.last_updated_datetime.strftime('%Y-%m-%d %H:%M:%S')}', TRUE);")


num_users = 100
chat_id_counter = 1
msg_id_counter = 1

chats = []
messages = []

existing_chats = set()
user_chat_count = {i: 0 for i in range(1, num_users + 1)}


mock_contents = [
    "Hola, ¿cómo estás?", "¿Qué onda?", "Todo bien, ¿y vos?",
    "Nos vemos luego.", "¿Pudiste revisar el código?",
    "Genial, gracias.", "Dale, un abrazo.", "Te hablo más tarde.",
    "¿Sale juntada el finde?", "Mañana te confirmo.",
    "Jaja sí, tal cual.", "Pasame el link cuando puedas.",
    "Che, vi tu bloque nuevo, ¡está buenísimo!",
    "¿Me ayudas con un ejercicio de Álgebra? No entiendo nada.",
    "Gracias por likear mi curso!",
    "¿Viste el desafío nuevo de Spring Boot?",
    "Dale, después lo miro. Estoy a full con el laburo.",
    "Te mandé DM para coordinar el TP.",
    "¿Conocés algún tutorial bueno de Docker?",
    "Sí, en STUDER hay uno de @juancito que es excelente.",
    "Felicitaciones por los 100 seguidores!!",
    "Jajajajaja noooo, me pasó lo mismo.",
    "Estoy armando un grupo de estudio, ¿te prendés?",
    "Ayer estuve hasta las 3am debuggeando un error boludo.",
    "Oka, ahí te paso el repo por privado.",
    "Chequé el PR, dejé un par de comentarios.",
]

for i in range(1, num_users + 1):
    target = random.randint(3, 6)
    attempts = 0

    while user_chat_count[i] < target and attempts < 50:
        j = random.randint(1, num_users)


        if i != j:
            pair = tuple(sorted((i, j)))
            if pair not in existing_chats:
                existing_chats.add(pair)
                user_chat_count[i] += 1
                user_chat_count[j] += 1

                chat = ChatEntity(chat_id_counter, i, j)
                chats.append(chat)


                num_msgs = random.choices([1, 2, 3, 4, 5, 8, 12], weights=[5, 15, 20, 15, 10, 5, 2], k=1)[0]
                first_sender = random.choice([i, j])
                dt = chat.created_datetime + timedelta(minutes=random.randint(1, 60))


                msg1 = MessageEntity(msg_id_counter, chat_id_counter, first_sender, random.choice(mock_contents), "",
                                     None, True, dt)
                messages.append(msg1)

                if num_msgs == 2:

                    second_sender = j if first_sender == i else i
                    dt2 = dt + timedelta(minutes=random.randint(1, 10))
                    is_read = random.choice([True, False])

                    msg2 = MessageEntity(msg_id_counter + 1, chat_id_counter, second_sender,
                                         random.choice(mock_contents), "", msg1.msg_id, is_read, dt2)
                    messages.append(msg2)
                    msg_id_counter += 1

                msg_id_counter += 1
                chat_id_counter += 1
        attempts += 1

with open('inserts_chats_messages.sql', 'w', encoding='utf-8') as f:
    f.write("-- Inserts para la tabla 'chats'\n")
    for c in chats:
        f.write(c.to_sql() + "\n")

    f.write("\n-- Inserts para la tabla 'messages'\n")
    for m in messages:
        f.write(m.to_sql() + "\n")

print(f"Se generaron {len(chats)} chats y {len(messages)} mensajes.")