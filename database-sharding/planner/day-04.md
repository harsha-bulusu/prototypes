## Finding a Wrong Shard key

## ES cluster
    - Test Shard rebalancing
    - How replicas work on a node failure
---

## 🧠 What Are Watermarks in Elasticsearch?

Watermarks are **disk space thresholds** used by Elasticsearch to control how shards are allocated or moved based on **available disk space** on each node. There are three main types:

| Watermark       | Purpose                                  | Action Elasticsearch Takes                                      |
| --------------- | ---------------------------------------- | --------------------------------------------------------------- |
| **Low**         | Early warning threshold                  | Avoid putting **new shards** on the node.                       |
| **High**        | Critical level to take action            | Try to **move existing shards** away from the node.             |
| **Flood Stage** | Emergency level, index becomes read-only | Elasticsearch sets **index to read-only** to prevent data loss. |

---

## 🧪 Example Config

```json
PUT /_cluster/settings
{
  "persistent": {
    "cluster.routing.allocation.disk.watermark.low": "85%",
    "cluster.routing.allocation.disk.watermark.high": "90%",
    "cluster.routing.allocation.disk.watermark.flood_stage": "95%",
    "cluster.routing.allocation.disk.watermark.low.max_headroom": "50mb",
    "cluster.routing.allocation.disk.watermark.high.max_headroom": "30mb",
    "cluster.routing.allocation.disk.watermark.flood_stage.max_headroom": "10mb"
  }
}
```

This tells Elasticsearch:

* LOW watermark is **85% of disk used**, **unless less than 50MB is free**.
* HIGH watermark is **90% of disk used**, **unless less than 30MB is free**.
* FLOOD\_STAGE watermark is **95% of disk used**, **unless less than 10MB is free**.

---

## 📏 Calculation Logic (How ES Decides)

Elasticsearch evaluates **both percent used** and **remaining space** (via `max_headroom`). Whichever threshold is triggered **first**, that watermark applies.

Let’s say a node has **100MB disk space** total:

| Watermark   | % Used | Used Space | Free Required (`max_headroom`) | Triggered? |
| ----------- | ------ | ---------- | ------------------------------ | ---------- |
| Low         | 85%    | 85 MB      | 15 MB < 50MB (threshold)       | ✅ Yes      |
| High        | 90%    | 90 MB      | 10 MB < 30MB                   | ✅ Yes      |
| Flood Stage | 95%    | 95 MB      | 5 MB < 10MB                    | ✅ Yes      |

→ **Flood stage** is the most severe — that will apply.

---

## 💡 When and Why Each is Used

### 1. 🔽 **Low Watermark**

* **When?** Node’s disk usage crosses low threshold.
* **Action?** New shards are **not** allocated to this node.
* **Why?** Prevents pressure from increasing.

### 2. 🔼 **High Watermark**

* **When?** Disk usage goes beyond high threshold.
* **Action?** Tries to **relocate existing shards** elsewhere.
* **Why?** Keeps system healthy under higher stress.

### 3. 🚨 **Flood Stage Watermark**

* **When?** Node is critically full.
* **Action?**

  * Indexes on that node are marked as **read-only**.
  * Prevents further indexing to **avoid corruption or data loss**.
* **Why?** Last line of defense.

---

## 🧠 Summary Mental Model

```text
             Disk Usage
                 ▲
   Flood Stage ──┴── 95% (or <10MB free)
       High ─────┴── 90% (or <30MB free)
        Low ─────┴── 85% (or <50MB free)
```

When the **lowest of two conditions** (percentage **or** `max_headroom`) is met, the corresponding watermark gets triggered.

---

## ❓ Common Questions

### ✅ Q: Do these rules apply to all nodes?

**Yes.** These rules apply **globally** unless you override them with [per-node attributes](https://www.elastic.co/guide/en/elasticsearch/reference/current/shard-allocation-filtering.html).

