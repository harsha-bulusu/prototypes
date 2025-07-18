import sqlite3
import os
import time

def connect_db():
    return sqlite3.connect("data/orders.db")

def setup():
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS orders (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            order_id INT,
            shipping_type TEXT,
            status TEXT,
            country TEXT          
        )
    """)

    # Insert in batches
    TOTAL_ROWS = 10_000_000
    BATCH_SIZE = 10_000

    for start in range(1, TOTAL_ROWS + 1, BATCH_SIZE):
        end = min(start + BATCH_SIZE, TOTAL_ROWS + 1)
        batch = generate_batch(start, end)
        
        cursor.execute("BEGIN TRANSACTION")
        cursor.executemany("""
            INSERT INTO orders (
                name, order_id, shipping_type, status, country
            ) VALUES (?, ?, ?, ?, ?)
        """, batch)
        conn.commit()

        # print(f"✅ Inserted rows {start} to {end - 1}")

    print("✅ All 10 million orders inserted.")

def generate_batch(start, end, country = None):
    shipping_types = ['Standard', 'Express']
    statuses = ['Pending', 'Shipped', 'Delivered', 'Cancelled']
    countries = ['IND', 'US', 'EU']
    return [
        (
            f'Order_{i}',
            i,
            shipping_types[i % 2],
            statuses[i % 4],
            country if country else countries[i % 3]
        )
        for i in range(start, end)
    ]

def get_count():
    cursor = conn.cursor()
    res = cursor.execute("""SELECT count(*) from orders""").fetchall()
    print(res)

def get_orders_by(country):
    # try by country level, order level and with other filters to check query performance
    cursor = conn.cursor()
    start = time.perf_counter()
    res = cursor.execute("SELECT * from orders WHERE country=?", (country,)).fetchall()
    end = time.perf_counter()
    print(f"Query took {end - start:.6f} seconds")
    print("Total records queried: ", len(res))


def setup_sharding():
    # create table and store connections
    for shard in shards:
        connection = sqlite3.connect(shard)
        # extract region
        region = shard.split("_")[1].split(".")[0].upper()

        cursor = connection.cursor()
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                order_id INT,
                shipping_type TEXT,
                status TEXT,
                country TEXT          
            )
        """)

        TOTAL_ROWS = 3_333_333
        BATCH_SIZE = 10_000

        for start in range(1, TOTAL_ROWS + 1, BATCH_SIZE):
            end = min(start + BATCH_SIZE, TOTAL_ROWS + 1)
            batch = generate_batch(start, end, region)
            
            if not connection.in_transaction:
                cursor.execute("BEGIN")
            cursor.executemany("""
                INSERT INTO orders (
                    name, order_id, shipping_type, status, country
                ) VALUES (?, ?, ?, ?, ?)
            """, batch)
            connection.commit()

            # print(f"✅ Inserted rows {start} to {end - 1}")

        print("✅ All 3.3 million orders inserted in region: ", region)
        connection.close()

def get_orders_by_shard(shard_key):
    shard = get_shard(shard_key)
    if shard is None:
        raise ValueError(f"No shard found for key: {shard_key}")
    print(shard)
    shard_conn = sqlite3.connect(shard)
    shard_cursor = shard_conn.cursor()
    start = time.perf_counter()
    res = shard_cursor.execute("SELECT * from orders").fetchall()
    end = time.perf_counter()
    print(f"Query took {end - start:.6f} seconds")
    print("Total records queried: ", len(res))
    


# Routing logic: region as shard_key
def get_shard(shard_key):
    if shard_key == 'IND':
        return "data/orders_ind.db"
    elif shard_key == 'EU':
        return "data/orders_eu.db"
    else:
        return "data/orders_us.db"

if __name__ == '__main__':
    os.makedirs("data", exist_ok=True)
    conn = connect_db()
    shards = ["data/orders_ind.db", "data/orders_us.db", "data/orders_eu.db"]

    setup()
    get_orders_by(country='IND')

    setup_sharding()
    get_orders_by_shard("IND")

    conn.close()