1. Core Concepts
Objects and Namespaces
Each resource type (like doc, folder, photo) is a namespace.

Objects are instances of a namespace, e.g., doc:readme.

Tuple Format
A tuple encodes:
<object>#<relation>@<user or userset>

Example:

text
Copy
Edit
doc:readme#owner@user:10       →  user 10 is an owner of doc:readme  
group:eng#member@user:11       →  user 11 is a member of group:eng  
doc:readme#viewer@group:eng#member  →  All members of group:eng are viewers of doc:readme  
2. User and Permissions Modeling
Valid After #
These are relations defined in the namespace config (e.g., owner, viewer, editor, member, parent, etc.)

Valid After @
Either a direct user (user:10) or a userset (like group:eng#member)

3. Namespace Configuration
Example:
hcl
Copy
Edit
namespace "doc" {
  relation "owner": ...
  relation "editor": union(this, owner)
  relation "viewer": union(this, editor, tuple_to_userset(parent, viewer))
}
Userset Rewrite Types:
this: Return direct relation tuples

computed_userset: e.g., viewer inherits from editor

tuple_to_userset: e.g., doc inherits permissions from folder

4. Zookie and Snapshots
Zookie = Snapshot token

Ensures external consistency

Returned during writes, sent back during reads/checks to guarantee snapshot freshness

Stored client-side, validated server-side

5. Client Responsibilities
Define and upload namespace schema

Write relation tuples (ACLs)

Call Check, Read, Expand APIs with zookie if needed

Decide when to define permissions (for delegation logic)

6. Zanzibar APIs
API	Purpose
Check	Is user part of obj#relation?
Read	Get direct tuples for an object
Expand	Expand and resolve the full set
Write	Insert/Delete tuples
Watch	Stream tuple changes

7. Architecture
Frontends handle API requests

Backend = Spanner (stores tuples, configs, changelogs)

Leopard index: Optimized for large/deep group memberships

Caching, hedging, lock tables used for latency/hotspot mitigation

8. Leopard Indexing
Used for:

Deep/complex nested group memberships

Flattened GROUP2GROUP, MEMBER2GROUP sets

Workflow:

Offline builds index shards from snapshot

Real-time layer tracks updates from Watch API

Query = Set theory on GROUP2GROUP and MEMBER2GROUP

9. Hotspot Handling
Use distributed in-memory caches and lock tables

Prefetch tuples for hot objects

Delay cancellation of pending sub-checks if concurrent

10. Performance Insights
Safe requests (older zookies): ~3–10ms

Recent requests (fresh zookies): 60ms+

Writes: ~100–400ms (due to TX coordination)

11. Lessons Learned
Flexibility crucial due to diverse client use-cases

Caching + sharding + hedging = latency wins

Performance isolation is mandatory (quotas, lock tables)