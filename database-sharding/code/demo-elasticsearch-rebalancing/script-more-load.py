import requests
import json
import random
import string
from datetime import datetime, timezone
from concurrent.futures import ThreadPoolExecutor, as_completed

ES_URL = "http://localhost:9200"
INDEX = "skew-logs-demo"
services = ["service-a", "service-b", "service-c"]

TOTAL_DOCS = 5000
THREADS = 10
BATCH_SIZE = TOTAL_DOCS // THREADS


# Generate a ~2KB random string
def generate_large_message(service):
    base = f"Log from {service} "
    random_blob = ''.join(random.choices(string.ascii_letters + string.digits, k=2000))
    return base + random_blob


def send_logs(thread_id, count):
    for i in range(count):
        service = random.choices(services, weights=[90, 5, 5])[0]
        doc = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "service": service,
            "message": generate_large_message(service),
            "meta": {
                "thread": thread_id,
                "level": random.choice(["INFO", "DEBUG", "WARN", "ERROR"]),
                "env": random.choice(["prod", "staging", "dev"]),
                "tags": random.sample(["api", "db", "auth", "cache", "metrics", "search"], k=3)
            }
        }

        try:
            response = requests.post(
                f"{ES_URL}/{INDEX}/_doc?routing={service}",
                headers={"Content-Type": "application/json"},
                data=json.dumps(doc)
            )
            if response.status_code >= 300:
                print(f"[Thread-{thread_id}] Failed: {response.status_code}, {response.text}")
        except Exception as e:
            print(f"[Thread-{thread_id}] Error: {e}")
    print(f"[Thread-{thread_id}] Indexed {i} docs")


if __name__ == "__main__":
    with ThreadPoolExecutor(max_workers=THREADS) as executor:
        futures = [
            executor.submit(send_logs, thread_id, BATCH_SIZE)
            for thread_id in range(THREADS)
        ]
        for future in as_completed(futures):
            future.result()
