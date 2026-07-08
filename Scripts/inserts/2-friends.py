import random
from datetime import datetime


class FriendEntity:
    def __init__(self, sender_id, receiver_id):
        self.sender_id = sender_id
        self.receiver_id = receiver_id
        self.created_datetime = datetime.utcnow()
        self.last_updated_datetime = self.created_datetime

    def to_sql(self):

        return (f"INSERT INTO friends (sender_id, sender_accept, receiver_id, receiver_accept, "
                f"created_datetime, last_updated_datetime, is_active) VALUES "
                f"({self.sender_id}, TRUE, {self.receiver_id}, TRUE, "
                f"'{self.created_datetime.strftime('%Y-%m-%d %H:%M:%S')}', "
                f"'{self.last_updated_datetime.strftime('%Y-%m-%d %H:%M:%S')}', TRUE);")


num_users = 100

adj = {i: set() for i in range(1, num_users + 1)}


target = {i: random.randint(5, 10) for i in range(1, num_users + 1)}

friendships = []

for i in range(1, num_users + 1):
    attempts = 0

    while len(adj[i]) < target[i] and attempts < 100:
        j = random.randint(1, num_users)


        if i != j and j not in adj[i] and len(adj[j]) < 10:
            adj[i].add(j)
            adj[j].add(i)


            if random.choice([True, False]):
                friendships.append(FriendEntity(i, j))
            else:
                friendships.append(FriendEntity(j, i))

        attempts += 1


with open('inserts_friends.sql', 'w', encoding='utf-8') as f:
    f.write("-- Generador de Inserts para tabla 'friends'\n")
    for friend in friendships:
        f.write(friend.to_sql() + "\n")

print(f"Se generaron {len(friendships)} relaciones de amistad exitosamente.")