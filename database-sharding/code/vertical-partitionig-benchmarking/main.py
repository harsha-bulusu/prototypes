import sqlite3
import os
import time


def connect_db():
    return sqlite3.connect("data/users.db")

def disconnect(conn):
    conn.close()

def setup(conn):
    cursor = conn.cursor()
    cursor.execute("BEGIN TRANSACTION")
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users_fat (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            age INTEGER,
            username TEXT,
            password TEXT,
            language TEXT,
            currency TEXT,
            theme TEXT,
            time_format TEXT,
            default_redirect_url TEXT           
        )
    """)
    
    users = []
    for i in range(1,10000):
        users.append((
        f'User_{i}',           
        20 + (i % 30),          
        f'user{i}',             
        f'pass{i}',             
        f'lang{i % 5}',         
        f'CUR{i % 3}',          
        'dark' if i % 2 == 0 else 'light',
        '24h' if i % 2 == 0 else '12h',
        f'/redirect{i % 4}'
    ))
        
    cursor.executemany("""
        INSERT INTO users_fat (name, age, username, password, language, currency, theme, time_format, default_redirect_url) VALUES (?,?,?,?,?,?,?,?,?)
    """, users)
    conn.commit()

def get_count(conn):
    cursor = conn.cursor()
    res = cursor.execute("""SELECT COUNT(*) from user_auth""").fetchall()
    print(res)

def authenticate(conn, username, password):
    # user tuple format [(1, 'User_1', 21, 'user1', 'pass1', 'lang1', 'CUR1', 'light', '12h', '/redirect1')]
    cursor = conn.cursor()
    start = time.perf_counter()
    res = cursor.execute("""SELECT username, password FROM users_fat WHERE username=? AND password=?""", (username, password)).fetchall()
    end = time.perf_counter()
    print(f"Query took {end - start:.6f} seconds")
    print(res)

def setup_tables_by_vertical_sharding(conn):
    cursor = conn.cursor()
    cursor.execute("BEGIN TRANSACTION")
    cursor.execute("""
        CREATE TABLE user_auth(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT,
            password TEXT
        )
    """)

    users = []
    for i in range(1,10000):
        users.append((                   
        f'user{i}',             
        f'pass{i}',        
        ))
    
    cursor.executemany("""
        INSERT INTO user_auth (username, password) values(?,?)
    """, (users))
    conn.commit()


def authenticate_partitioned_data(conn, username, password):
    cursor = conn.cursor()
    start = time.perf_counter()
    res = cursor.execute("SELECT username, password FROM user_auth where username=? AND password=?", (username, password)).fetchall()
    end = time.perf_counter()
    print(f"Query took {end - start:.6f} seconds")
    print(res)

if __name__ == '__main__':
    os.makedirs("data", exist_ok=True)
    conn = connect_db()
    setup(conn)
    authenticate(conn, "user1", "pass1")

    setup_tables_by_vertical_sharding(conn)
    authenticate_partitioned_data(conn, "user9999", "pass9999")
    disconnect(conn)