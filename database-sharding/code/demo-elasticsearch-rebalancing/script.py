import requests
import json
import random
from datetime import datetime, timezone
from concurrent.futures import ThreadPoolExecutor, as_completed

ES_URL = "http://localhost:9200"
INDEX = "skew-logs-demo"
services = ["service-a", "service-b", "service-c"]

TOTAL_DOCS = 1000000
THREADS = 10
BATCH_SIZE = TOTAL_DOCS // THREADS


def send_logs(thread_id, count):
    for i in range(count):
        service = random.choices(services, weights=[90, 5, 5])[0]
        doc = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "service": service,
            "message": f"Log from {service}"
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

        if i % 1000 == 0:
            print(f"[Thread-{thread_id}] Indexed {i} docs")


if __name__ == "__main__":
    with ThreadPoolExecutor(max_workers=THREADS) as executor:
        futures = [
            executor.submit(send_logs, thread_id, BATCH_SIZE)
            for thread_id in range(THREADS)
        ]
        for future in as_completed(futures):
            future.result()
