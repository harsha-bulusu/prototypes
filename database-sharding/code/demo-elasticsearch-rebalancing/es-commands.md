1. Setup

Check Nodes - Health, Resources

```
  GET /_cluster/health?pretty=true


  GET /_cat/nodes?v=true&h=ip,nodeRole,master,name,cpu,heapPercent,ramPercent,diskAvail
```

2. Define watermarks

```
  GET /_cluster/settings?include_defaults=true&filter_path=**.disk.watermark*

  PUT /_cluster/settings
  {
    "persistent": {
      "cluster.routing.allocation.disk.watermark.low": "80%",
      "cluster.routing.allocation.disk.watermark.high": "80%",
      "cluster.routing.allocation.disk.watermark.flood_stage": "95%",
      "cluster.routing.allocation.disk.watermark.low.max_headroom": "30mb",
      "cluster.routing.allocation.disk.watermark.high.max_headroom": "20mb",
      "cluster.routing.allocation.disk.watermark.flood_stage.max_headroom": "5mb"
    }
  }
```

3. Enable Rebalancing if disabled

```
  GET /_cluster/settings?include_defaults=true&flat_settings=true

  "cluster.routing.rebalance.enable": "all",
```


🧮 Rebalancing Options

| Value       | Meaning                                                             |
| ----------- | ------------------------------------------------------------------- |
| `all`       | 🔄 Rebalancing is enabled for **primaries and replicas**. (Default) |
| `primaries` | Only **primary shards** are rebalanced.                             |
| `replicas`  | Only **replica shards** are rebalanced.                             |
| `none`      | ❌ Rebalancing is completely disabled.                               |


3. Create index

```
  PUT /skew-logs-demo
  {
    "settings": {
      "number_of_shards": 4,
      "number_of_replicas": 1
    }
  }
```


4. Check shards distribution

```
  GET /_cat/shards/skew-logs-demo?v=true
  GET /_cat/shards/skew-logs-demo?v&h=index,shard,prirep,state,docs,node
```

5. Check count of documents in an index

```
  GET /skew-logs-demo/_count
```

---
## Misc

Triggering a Manual rerouting
```
  POST /_cluster/reroute
  {
    "commands": [
      {
        "allocate_stale_primary": {
          "index": "skew-logs-demo",
          "shard": 1,
          "node": "es02",
          "accept_data_loss": true
        }
      }
    ]
  }
```

Update replica count
```
  PUT skew-logs-demo/_settings
  {
    "index": {
      "number_of_replicas": 0
    }
  }
```