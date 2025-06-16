# 2PC Simulation - Functional Style with DB Actions
# Actors: Client, Transaction Coordinator, PaymentService, DriverService

from enum import Enum
import sqlite3

# Setup DB for state persistence
conn = sqlite3.connect(":memory:")
c = conn.cursor()
c.execute("CREATE TABLE tx_state (service TEXT, tx_id TEXT, state TEXT)")
conn.commit()

class Status(Enum):
    INIT = "INIT"
    PREPARED = "PREPARED"
    COMMITTED = "COMMITTED"
    ABORTED = "ABORTED"

# --- Payment Service ---
def payment_prepare(tx_id):
    if tx_id.endswith("3"):  # simulate failure for certain tx
        print("[PaymentService] Cannot prepare, rejecting")
        payment_abort(tx_id)
        return False
    print("[PaymentService] Prepared")
    c.execute("INSERT INTO tx_state VALUES (?, ?, ?)", ("PaymentService", tx_id, Status.PREPARED.value))
    conn.commit()
    return True

def payment_commit(tx_id):
    print("[PaymentService] Committing")
    c.execute("UPDATE tx_state SET state=? WHERE service=? AND tx_id=?", (Status.COMMITTED.value, "PaymentService", tx_id))
    conn.commit()

def payment_abort(tx_id):
    print("[PaymentService] Aborting")
    c.execute("INSERT INTO tx_state VALUES (?, ?, ?) ON CONFLICT DO UPDATE SET state=?", ("PaymentService", tx_id, Status.ABORTED.value, Status.ABORTED.value))
    conn.commit()

# --- Driver Service ---
def driver_prepare(tx_id):
    print("[DriverService] Prepared")
    c.execute("INSERT INTO tx_state VALUES (?, ?, ?)", ("DriverService", tx_id, Status.PREPARED.value))
    conn.commit()
    return True

def driver_commit(tx_id):
    print("[DriverService] Committing")
    c.execute("UPDATE tx_state SET state=? WHERE service=? AND tx_id=?", (Status.COMMITTED.value, "DriverService", tx_id))
    conn.commit()

def driver_abort(tx_id):
    print("[DriverService] Aborting")
    c.execute("INSERT INTO tx_state VALUES (?, ?, ?) ON CONFLICT DO UPDATE SET state=?", ("DriverService", tx_id, Status.ABORTED.value, Status.ABORTED.value))
    conn.commit()

# --- Transaction Coordinator ---
def begin_transaction(tx_id):
    print(f"[Coordinator] Begin TX {tx_id}")
    if not payment_prepare(tx_id):
        driver_abort(tx_id)
        return False
    if not driver_prepare(tx_id):
        payment_abort(tx_id)
        return False
    # All prepared
    payment_commit(tx_id)
    driver_commit(tx_id)
    print(f"[Coordinator] TX {tx_id} committed successfully")
    return True

# --- Client Simulation ---
def simulate_client():
    print("\n--- TX1001 (Happy Path) ---")
    begin_transaction("TX1001")

    print("\n--- TX1003 (Simulate Payment Failure) ---")
    begin_transaction("TX1003")

if __name__ == "__main__":
    simulate_client()
