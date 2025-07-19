### Various Routing Techniques
How do you chose a routing strategy

---

### 🔹 **1. Range-Based Sharding**

**Best when:**

* Your shard key has a **natural order** (e.g., `user_id`, `timestamp`, `order_id`)
* You want **range queries** to be efficient (e.g., date filters, ID scans)
* You need predictable shard assignment

**Used for:**

* Time-series data
* Log/event ingestion
* E-commerce orders
* Applications with **sequential IDs**

**Pros:**

* Efficient for range queries
* Easy to assign new ranges during scaling (e.g., time windows or ID splits)

**Cons:**

* Prone to **hotspots** if the data is unevenly distributed (e.g., some users write more)
* Rebalancing requires **splitting and migrating ranges**

**🔥 Enhanced Note:**

> Even though range-based sharding is simple and intuitive, it can lead to **imbalanced shard sizes** unless the growth of your range key is uniformly distributed. You may need **active monitoring** and **dynamic splitting**.

---

### 🔹 **2. Hash-Based Sharding**

**Best when:**

* Your data distribution is **unpredictable or skewed**
* You want **uniform load across shards**
* Range queries are rare or unnecessary

**Used for:**

* High-volume write-heavy systems
* Systems where **fair write distribution** matters more than range scans
* Chat messages, sensor data, short user transactions

**Pros:**

* Naturally balances data across shards
* Avoids **hotspots** from "heavy" keys (e.g., active users)

**Cons:**

* Hard/impossible to do **range queries**
* Rebalancing means **rehashing**, which can affect multiple shards
* Adds complexity in **maintaining hash functions and slots**

**🔥 Enhanced Note:**

> Hash-based routing shines when you want to **firehose writes evenly**, but sacrifices efficiency for queries that require scanning ranges or ordering results.

---

### 🔹 **3. Directory-Based Sharding**

**Best when:**

* You have **logical entities or tenants** that should be isolated (e.g., per-customer data)
* You want **flexible and dynamic shard assignment**
* You anticipate **custom placement**, **manual overrides**, or **tenant migrations**

**Used for:**

* Multi-tenant SaaS platforms
* Applications where some tenants are **heavy users** and need dedicated shards
* Cases where tenant lifecycle (onboarding/offboarding) needs custom routing

**Pros:**

* Complete flexibility in assigning records to shards
* Easy to move a tenant by updating directory mapping
* Supports **custom policies** per tenant (e.g., premium tenants on faster shards)

**Cons:**

* Requires a **directory service** with lookup latency
* Adds a **metadata layer** to manage
* Write path for a new tenant needs initial **placement logic**

**🔥 Enhanced Note:**

> Directory-based routing is **ideal when the unit of sharding is a business object (like a tenant)** and not just a value. It trades off lookup complexity for **control and customizability**.

---

### ✅ Summary Table

| Strategy            | When to Use                                            | Pros                                     | Cons                                  |
| ------------------- | ------------------------------------------------------ | ---------------------------------------- | ------------------------------------- |
| **Range-Based**     | Ordered keys, range queries, append-heavy data         | Simple to scale, efficient range scans   | Risk of hotspots, manual balancing    |
| **Hash-Based**      | Uniform write load, random access patterns             | Even distribution, avoids hot shards     | No range scans, rebalancing is costly |
| **Directory-Based** | Multi-tenant, custom placement, heavy tenant isolation | Full control, flexible, reassign tenants | Metadata overhead, lookup cost        |

---


### Rebalancing (Adding/Removing Shards)