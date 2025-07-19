import sqlite3
import os

shards = ["data/shard1", "data/shard2", "data/shard3"]
DIR_META_DB = "data/directory_meta"

def setup():
    os.makedirs("data", exist_ok=True)
    # SAAS solurtion for GITHUB type of solution
    for shard in shards:
        connection = sqlite3.connect(shard)
        cursor = connection.cursor()
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS projects(
                project_id INTEGER PRIMARY KEY AUTOINCREMENT,
                tenant_id TEXT,
                project_name TEXT,
                owner TEXT
            );
        """)
        connection.commit()
        connection.close()
    
    # Create directory for lookups - The directory can be in memory(Like redis) for faster reads
    connection = sqlite3.connect(DIR_META_DB)
    cursor = connection.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS metadata(
            tenant_id TEXT PRIMARY KEY,
            shard_id TEXT
        );
    """)

def get_shard_key(tenant_id):
    connection = sqlite3.connect(DIR_META_DB)
    cursor = connection.cursor()
    res = cursor.execute("SELECT shard_id from metadata where tenant_id=?",(tenant_id,)).fetchall()

    if len(res) == 0:
        # default routing strategy
        shard_id = shards[hash(tenant_id) % len(shards)]
        cursor.execute("INSERT INTO metadata values (?,?)", (tenant_id, shard_id))
        connection.commit()
        connection.close()
        return shard_id 
    else:
        connection.close()
        return res[0][0]


def create_project(tenant_id, project_name, owner):
    shard_key = get_shard_key(tenant_id)
    print(shard_key)
    connection = sqlite3.connect(shard_key)
    cursor = connection.cursor()
    cursor.execute("INSERT INTO projects (tenant_id, project_name, owner) VALUES(?,?,?)", (tenant_id, project_name, owner))
    connection.commit()
    connection.close()
    

def view_all_projects_by_shard_level():
    for shard in shards:
        connection = sqlite3.connect(shard)
        cursor = connection.cursor()
        res = cursor.execute("SELECT * from projects").fetchall()
        print(shard, res)
        connection.close()

if __name__ == '__main__':
    setup()
    create_project("t1", "demo-3", "Harsha")
    view_all_projects_by_shard_level()