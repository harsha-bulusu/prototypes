import sys

import psycopg2



def connect_db(db_config):
    try:
        conn = psycopg2.connect(**db_config)
        print("✅ Connected to PostgreSQL successfully!")
        return conn
    except psycopg2.Error as e:
        print("❌ Error connecting to PostgreSQL:", e)

def insert(queue_name, message):
    query = "insert into messages (queue_name, body) values (%s, %s);"
    cursor = conn.cursor()
    cursor.execute(query, (queue_name, message))
    conn.commit()


def read(queue_name, poll_count, visibility_timeout, consumer_name):
    query = """
    UPDATE messages
    SET
      visibility_timeout = NOW() + INTERVAL '%s minutes',
      receive_count = receive_count + 1,
      locked_by = %s,
      status = 'inflight'
    WHERE id = (SELECT id from messages where queue_name=%s
    and (visibility_timeout IS NULL OR visibility_timeout < NOW())
    and status = 'queued'
    ORDER BY sent_at LIMIT %s
    FOR UPDATE SKIP LOCKED)
    RETURNING *;
    """
    cursor = conn.cursor()
    cursor.execute(query, (visibility_timeout, consumer_name, queue_name, poll_count))
    rows = cursor.fetchall()
    conn.commit()
    print(rows)


def commit(message_id):
    query = """
    UPDATE messages
    SET
        status = 'processed'
    WHERE id = %s
    RETURNING *
    """
    cursor = conn.cursor()
    cursor.execute(query, (message_id,))
    rows = cursor.fetchall()
    conn.commit()
    print("committed rows", rows)


if __name__ == '__main__':
    db_config = {
        "dbname": "harsha",
        "user": "postgres",
        "password": "harsha",
        "host": "localhost",
        "port": 5432
    }
    conn = connect_db(db_config)
    if conn is None:
        sys.exit()
    # insert('email-notifications', "sriharsha16208@gmail.com")
    # read('email-notifications', 1, 1, 'harsha')
    commit(2)