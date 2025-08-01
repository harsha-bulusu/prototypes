from psycopg2 import pool,connect, Error
import random
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
import redis
import threading

mutex = threading.Lock()

redis_client = redis.Redis(host="localhost", port=6379)

db_config = {
        "dbname": "harsha",
        "user": "postgres",
        "password": "harsha",
        "host": "localhost",
        "port": 5432
    }

def connect_db():
    try:
        conn = connect(**db_config)
        print("✅ Connected to PostgreSQL successfully!")
        return conn
    except Error as e:
        print("❌ Error connecting to PostgreSQL:", e)

connection_pool = None

def init_connection_pool():
    global connection_pool
    connection_pool = pool.SimpleConnectionPool(
        minconn=5,
        maxconn=20,
        **db_config
    )
    print("✅ Connection pool initialized")


def populate_students():
    create_users_table = """
        CREATE TABLE IF NOT EXISTS students (
            id SERIAL PRIMARY KEY,
            name VARCHAR NOT NULL,
            marks real
        );
    """
    conn = connect_db()
    cursor = conn.cursor()
    cursor.execute(create_users_table)
    conn.commit()

    users = []
    for i in range(1,1_000_001):
        users.append((f"user-{i}", random.randrange(30,100)))
    cursor.executemany("INSERT INTO students (name, marks) values (%s,%s)", users)
    conn.commit()    
    conn.close()

def find_avg_marks():
    start_time = time.perf_counter()
    avg = redis_client.get("avg")
    if avg is not None:
        print("✅ Cache Hit")
        duration = (time.perf_counter() - start_time) * 1000
        return duration, float(avg)

    with mutex:
        avg = redis_client.get("avg")
        if avg is not None:
            print("✅ Cache Hit on double check")
            duration = (time.perf_counter() - start_time) * 1000
            return duration, float(avg)
        conn = connection_pool.getconn()
        try:
            print("❌ Cache miss, reading from DB")
            cursor = conn.cursor()
            start_time = time.perf_counter()
            cursor.execute("SELECT AVG(marks) FROM students;")
            avg = cursor.fetchone()[0]
            duration = (time.perf_counter() - start_time) * 1000
            cursor.close()
            redis_client.setex("avg", 60, avg)
            return duration, avg
        finally:
            connection_pool.putconn(conn)
    

def benchmark(concurrent_threads=10, runs_per_thread=5):
    durations = []
    with ThreadPoolExecutor(max_workers=concurrent_threads) as executor:
        futures = [executor.submit(find_avg_marks) for _ in range(concurrent_threads * runs_per_thread)]

        for future in as_completed(futures):
            duration, avg = future.result()
            durations.append(duration)
            print(f"⏱ {duration:.2f} ms | AVG Marks: {avg}")

    print("\n🔁 Benchmark Summary")
    print(f"Total Requests: {len(durations)}")
    print(f"Average Time: {sum(durations)/len(durations):.2f} ms")
    print(f"Min Time: {min(durations):.2f} ms")
    print(f"Max Time: {max(durations):.2f} ms")

if __name__ == "__main__":
    # populate_students()
    init_connection_pool()
    conn = connect_db()
    benchmark()
    pass