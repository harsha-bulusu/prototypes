"""
    This program splits the data into multiple shards based on users first letter
"""
import sqlite3
import os

shards = {"data/users_a_j.db", "data/users_k_s.db", "data/users_t_z.db"}

# creates User scheme
def setup():
    for shard in shards:
        conn = sqlite3.connect(shard)
        cursor = conn.cursor()

        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id Integer PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                age INTEGER
            )
        """)

        conn.commit()
        conn.close()
        
        print("Successfully Created Scheme for Users.")

def get_shard(shard_key: str) -> str:
    # username as shard_key
    char = shard_key[0]
    if 'a' <= char <= 'j':
        return "data/users_a_j.db"
    elif 'k' <= char <= 's':
        return "data/users_k_s.db"
    else:
        return "data/users_t_z.db"


def add_user(username: str, age: int) -> None:
    username = username.lower()
    shard = get_shard(username)
    conn = sqlite3.connect(shard)
    cursor = conn.cursor()
    res = cursor.execute("INSERT INTO users (name, age) VALUES (?, ?)", (username, age))
    if res.rowcount == 1:
        print("Inserted successfully into shard:", shard)
    conn.commit()
    conn.close()
    
def get_all_users() -> None:
    results = []
    for shard in shards:
        conn = sqlite3.connect(shard)
        cursor = conn.cursor()
        res = cursor.execute("SELECT * from users").fetchall()
        results.extend(res)
        conn.close()
    print(results)


def find_user(username: str) -> None:
    username = username.lower()
    shard = get_shard(username)
    conn = sqlite3.connect(shard)
    cursor = conn.cursor()
    res = cursor.execute("SELECT * from users where name=?", (username,)).fetchone()
    print(res, "found in shard", shard)

if __name__ == "__main__":
    os.makedirs("data", exist_ok=True)
    setup()
    add_user("Virat Kohli", 36)
    get_all_users()
    find_user("Virat Kohli")