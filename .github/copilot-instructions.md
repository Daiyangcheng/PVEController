# PVEController - AI Coding Agent Instructions

## Project Overview
PVEController is a **Kotlin + Spring Boot 4.0** REST API for managing Proxmox Virtual Environment (PVE) infrastructure. It enables multi-datacenter management with database-backed PVE credentials, VM/node operations, and resource management.

**Key Stack:**
- Language: Kotlin 2.2.21
- Framework: Spring Boot 4.0.1 + Spring Data JPA
- Database: PostgreSQL
- External API: cv4pve-api-java 9.1.1 (Proxmox API client)
- Build: Gradle Kotlin DSL
- Java: 24 (Kotlin 2.2.21 limitation)

## Architecture Pattern

### Layered Structure
```
controller/ → service/ → repository/ → model/ + shared/
```

**Controller Layer** ([controller/](src/main/kotlin/cn/locyan/pvecontroller/controller/))
- REST endpoints using `@RestController`
- Dependency inject: `NodeService`, `ProcessPVEResult`, `ResponseBuilder`, `PVEClient`
- Always return `ResponseEntity<Response>` using `ResponseBuilder`

**Service Layer** ([service/jdbc/](src/main/kotlin/cn/locyan/pvecontroller/service/jdbc/))
- Interface + Implementation pattern (e.g., `NodeService` / `NodeServiceImpl`)
- CRUD operations and business logic
- Example: `NodeService.findByNameAndDcId(name, dcId)` with dc_id filtering

**Model Layer** ([model/](src/main/kotlin/cn/locyan/pvecontroller/model/))
- JPA `@Entity` with Lombok annotations (`@NoArgsConstructor`, `@AllArgsConstructor`)
- Uses `open` keyword for Kotlin classes (required for JPA lazy loading)
- Examples: `Node.kt` (node info), `User.kt` (authentication)
- All entities include `createdTime`, `updatedTime`, and multi-tenant `dc_id` (datacenter foreign key)

**Shared Utilities** ([shared/](src/main/kotlin/cn/locyan/pvecontroller/shared/))
- `pve/PVEClient.kt`: Single-instance wrapper around cv4pve-api-java's `PveClient`
- `pve/ProcessPVEResult.kt`: Validates API responses, returns error `ResponseEntity` if needed
- `response/ResponseBuilder.kt`: Fluent builder for standardized `Response` objects

### Data Model Pattern
All entities follow this structure:
```kotlin
@Entity
@Table(name = "entities")
open class Entity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null
    
    @Column(name = "name", nullable = false)
    open var name: String? = null
    
    @Column(name = "dc_id", nullable = false)  // Multi-tenant key
    open var dcId: Long? = null
    
    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null
    
    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
```

### Template Entity Pattern
```kotlin
@Entity @Table(name = "templates")
open class Template {
    open var id: Long? = null
    open var name: String? = null              // Template name (e.g., "Ubuntu 22.04")
    open var dcId: Long? = null                // Associated datacenter
    open var templateGroupId: Long? = null     // FK to TemplateGroup
    open var templateId: Long? = null          // PVE template ID (for cloning)
    open var osType: String? = null            // OS type (linux, windows, etc.)
    open var cloudInitEnabled: Boolean? = null // CloudInit support flag
    open var description: String? = null       // Template description
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

**TemplateGroup Entity** (grouping templates by category):
```kotlin
@Entity @Table(name = "template_groups")
open class TemplateGroup {
    open var id: Long? = null
    open var name: String? = null              // Group name (e.g., "Linux", "Windows")
    open var dcId: Long? = null                // Associated datacenter
    open var description: String? = null       // Group description
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

### IP Entity Patterns (IPv4 & IPv6)

**IPv4 Entity**:
```kotlin
@Entity @Table(name = "ipv4s")
open class IPv4 {
    open var id: Long? = null                  // Unique IPv4 ID (used by Server.ipId)
    open var dcId: Long? = null                // Associated datacenter
    open var ipAddress: String? = null         // IPv4 address (e.g., "192.168.1.100")
    open var gateway: String? = null           // Network gateway
    open var netmask: String? = null           // Network mask/CIDR
    open var macAddress: String? = null        // MAC address
    open var vmId: Long? = null                // PVE VMID (assigned during allocation)
    open var isAllocated: Boolean? = false     // Allocation status
    open var serverId: Long? = null            // FK to Server using this IP (nullable)
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

**IPv6 Range Entity** (for IPv6 block management):
```kotlin
@Entity @Table(name = "ipv6_ranges")
open class IPv6Range {
    open var id: Long? = null
    open var dcId: Long? = null                // Associated datacenter
    open var startAddress: String? = null      // IPv6 start (e.g., "2001:db8::1")
    open var endAddress: String? = null        // IPv6 end (e.g., "2001:db8::ffff")
    open var gateway: String? = null           // IPv6 gateway
    open var prefixLength: Int? = null         // /64, /48, etc.
    open var isActive: Boolean? = true         // Range active status
    open var allocatedCount: Long? = 0         // Track allocated IPs
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

**IPv6 Allocation Entity** (track individual IPv6 usage):
```kotlin
@Entity @Table(name = "ipv6_allocations")
open class IPv6Allocation {
    open var id: Long? = null
    open var dcId: Long? = null
    open var ipv6RangeId: Long? = null         // FK to IPv6Range
    open var assignedAddress: String? = null   // Allocated IPv6 address
    open var vmId: Long? = null                // PVE VMID (assigned during allocation)
    open var serverId: Long? = null            // FK to Server using this IPv6
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

## Core Patterns & Conventions

### 1. PVE API Integration Pattern
See [NodeController.updateNodesStatus()](src/main/kotlin/cn/locyan/pvecontroller/controller/NodeController.kt):
```kotlin
// 1. Get PVE data via client
val nodes = client.nodes.index()

// 2. Check for errors immediately
val check = processor.process(nodes)
if (check != null) return check

// 3. Extract and validate data
val data = nodes.data.toList()
data.forEach { node -> /* map PVE fields to local entity */ }

// 4. Sync with database (upsert pattern)
var dbNode = nodeService.findByNameAndDcId(name, dcId)
if (dbNode != null) {
    // Update existing with new values
    dbNode.apply { /* update fields */ }
} else {
    // Create new entity
    dbNode = Node().apply { /* populate all fields */ }
}
nodeService.update(dbNode)

// 5. Return standardized response
return builder.ok().build()
```

### 2. Multi-Datacenter Architecture (Dynamic Loading)
- Each resource (Node, Server, etc.) has `dc_id` foreign key
- **PVE credentials stored in database** (loaded dynamically per datacenter, NOT hardcoded)
- `DataCenter` entity ([model/DataCenter.kt](src/main/kotlin/cn/locyan/pvecontroller/model/DataCenter.kt)):
  ```kotlin
  open class DataCenter {
      open var id: Long? = null
      open var name: String? = null          // e.g., "pve-dc-01"
      open var host: String? = null          // PVE server IP
      open var port: Int? = null             // PVE port (default 8006)
      open var tokenId: String? = null       // root@pam!token-name
      open var tokenSecret: String? = null   // Token UUID
      open var status: Boolean? = null       // Connection status
      open var createdTime: LocalDateTime? = null
      open var updatedTime: LocalDateTime? = null
  }
  ```
- `DataCenterService` ([service/jdbc/DataCenterService.kt](src/main/kotlin/cn/locyan/pvecontroller/service/jdbc/)):
  - `findById(dcId: Long)` → fetch credentials from DB
  - `getAllDataCenters()` → list all connected PVE clusters
  - Caching via Redis for frequently accessed DC configs
- **Pattern**: Controller → `@RequestParam dcId` → `dataCenterService.findById(dcId)` → create PVEClient with DB credentials → call API
  ```kotlin
  val dataCenter = dataCenterService.findById(dcId) ?: return builder.exception().build()
  val client = pveClient.newClient(dataCenter.host, dataCenter.port, dataCenter.tokenId, dataCenter.tokenSecret)
  ```

### 3. Response Handling
All endpoints return:
```kotlin
ResponseEntity<Response> // Use ResponseBuilder
```
Error handling via `ProcessPVEResult.process()`:
```kotlin
if (check != null) return check  // Early exit if error
```

### 4. Naming Conventions
- **Controllers**: `{Resource}Controller` (e.g., `ServerController`, `TemplateController`)
- **Services**: `{Resource}Service` interface + `{Resource}ServiceImpl` implementation
- **Models**: `{Entity}.kt` matching database table names
- **Tables**: snake_case with plural form (e.g., `nodes`, `servers`, `templates`, `data_centers`)
- **Package structure**: `controller/`, `service/jdbc/`, `service/redis/`, `model/`, `repository/`

### 5. Server vs Node Relationship
- **Node**: Proxmox cluster node (physical/virtual host) - synced from PVE via `/nodes` API
- **Server**: Virtual machine (QEMU guest) - created, modified, deleted via management endpoints
- **Relationship**: Server has `node_name` FK to Node.name (multi-tenant scoped by `dc_id`)
  ```kotlin
  @Entity @Table(name = "servers")
  open class Server {
      open var id: Long? = null
      open var vmId: Long? = null              // PVE VMID (unique per cluster)
      open var name: String? = null            // Hostname
      open var nodeName: String? = null        // Which node hosts this VM
      open var dcId: Long? = null              // Which datacenter
      open var userId: Long? = null            // FK to User entity (VM owner)
      open var serverGroupId: Long? = null     // FK to ServerGroup (for grouping/billing)
      open var templateId: Long? = null        // FK to Template entity (CloudInit pre-configured)
      open var ipId: Long? = null              // FK to IPv4 entity for IPv4 management
      open var ipv6Id: Long? = null            // FK to IPv6Allocation entity for IPv6 management
      open var cpu: Int? = null
      open var memory: Long? = null            // In MB
      open var disk: Long? = null              // In GB
      open var status: String? = null          // "running", "stopped", "paused", etc.
      open var createdTime: LocalDateTime? = null
      open var updatedTime: LocalDateTime? = null
  }
  ```

**NodeGroup Entity** (grouping nodes for resource management):
```kotlin
@Entity @Table(name = "node_groups")
open class NodeGroup {
    open var id: Long? = null
    open var name: String? = null              // Group name (e.g., "Compute Cluster 1")
    open var dcId: Long? = null                // Associated datacenter
    open var description: String? = null       // Group description
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

**ServerGroup Entity** (grouping servers for users/projects):
```kotlin
@Entity @Table(name = "server_groups")
open class ServerGroup {
    open var id: Long? = null
    open var name: String? = null              // Group name (e.g., "Web Servers")
    open var dcId: Long? = null                // Associated datacenter
    open var userId: Long? = null              // FK to User (team/project owner)
    open var description: String? = null       // Group description
    open var createdTime: LocalDateTime? = null
    open var updatedTime: LocalDateTime? = null
}
```

### 6. Template & IP Management in VM Provisioning
- **Template**: Provides pre-configured VM image with CloudInit support
  - `templateId` in Server entity determines which template to clone from
  - Templates are grouped via `templateGroupId` → TemplateGroup entity
  - Templates include all necessary OS/software configurations
  - Pattern: `ServerController.createServer(templateId, ipId, nodeName, cpu, memory, disk, dcId, userId)`
  
- **IP Management**: 
  - **IPv4 Allocation**: 
    - One-by-one entry: `IPController.addIPv4(ipAddress, gateway, netmask, macAddress, dcId)`
    - Batch entry with start address: `IPController.addIPv4Range(startIp, count, gateway, netmask, dcId)`
    - Single IPv4 lookup: `ipv4Service.findById(ipId)` (cached 5 min)
    - `ipId` links Server to specific IPv4 entry in IP pool
  - **IPv6 Management**:
    - Define IPv6 block by start/end: `IPController.addIPv6Range(startAddr, endAddr, gateway, prefix, dcId)`
    - Allocation method: `sequential` (auto-incremented) or `custom` (user-specified)
    - When creating Server with IPv6: `ServerController.createServer(..., ipv6RangeId, allocationMethod)`
    - Track usage via IPv6Allocation entity
  - IPController manages all allocation/deallocation, Node/ServerGroup CRUD, TemplateGroup management

- **Provisioning Flow**:
  1. Validate template: `templateService.findById(templateId, dcId)` + check TemplateGroup
  2. For IPv4: `ipv4Service.allocateIP(dcId)` → returns ipId; For IPv6: `ipv6Service.allocate(rangeId, method)` → returns IPv6Allocation
  3. Find available node in NodeGroup: `nodeService.findByNameAndDcId(nodeName, dcId)`
  4. Create ServerGroup if needed: `serverGroupService.create(groupName, userId, dcId)`
  5. Create VM via PVE API: `client.nodes.{node}.qemu.create()` with template clone
  6. Attach IP with CloudInit: merge IPv4/IPv6 config into cloud-init user-data
  7. Persist Server entity with templateId + ipId/ipv6Id + userId + serverGroupId references

- **Component Responsibilities**:
  - **ServerController**: VM lifecycle (create with template, start, stop, delete, resize) + User-Server binding
  - **NodeController**: Node status sync from PVE; NodeGroup CRUD
  - **TemplateController**: Template CRUD + TemplateGroup management (organize templates by OS/type)
  - **IPController**: 
    - IPv4: Add single/batch, allocate, deallocate, list available, mark in-use
    - IPv6: Create ranges, allocate (sequential/custom), deallocate, track usage
  - **DiskController**: Storage management (list, create, delete, resize)
  - **ServerGroupController**: Group CRUD (create/read/update/delete server groups)

## Implementation Checklist for New Features

When implementing new endpoints (e.g., ServerController, TemplateController, IPController, DiskController):

1. **Create Entity Model** (`model/{Entity}.kt`)
   - Inherit timestamp + dc_id pattern
   - Use `@Entity`, `@Table`, Lombok annotations
   - All fields `open` (Kotlin + JPA requirement)
   - Include relationship fields (e.g., `nodeName` for Server, `templateId`/`ipId` for VM creation)
   - Foreign key references to dependent entities (Template, IP, User, ServerGroup, TemplateGroup, NodeGroup)
   - For user-owned resources: include `userId` FK to User entity

2. **Create Repository Interface + Implementation**
   - Interface in `service/jdbc/` (e.g., `ServerService`)
   - Implementation with `@Service` stereotype
   - Follow `NodeServiceImpl` pattern for CRUD
   - Add dc_id filtering: `fun findAllByDcId(dcId: Long): List<Entity>`
   - Add queries for FK lookups: `fun findByTemplateId(templateId: Long)`, `fun findByIpId(ipId: Long)`, `fun findByUserId(userId: Long)`
   - For groups: `fun findByUserId(userId: Long): List<ServerGroup>` (users own groups)

3. **Add Redis Caching** (if applicable)
   - Implement `{Entity}RedisService` in `service/redis/`
   - Cache frequently accessed queries with appropriate TTL
   - **Critical for Server creation**: Cache Template + IP availability checks
   - Implement cache invalidation on create/update/delete
   - Invalidate dependent caches: e.g., when IP is allocated, evict IP pool cache

4. **Create Controller**
   - Inject `{Entity}Service`, `DataCenterService`, `ProcessPVEResult`, `ResponseBuilder`, `PVEClient`
   - **For ServerController**: Also inject `TemplateService`, `IPv4Service`, `IPv6Service`, `NodeService`, `ServerGroupService`, `UserService`
   - Load DataCenter first: `val dc = dataCenterService.findById(dcId) ?: return builder.exception().build()`
   - Create PVEClient with dynamic credentials: `val client = pveClient.newClient(dc.host, dc.port, dc.tokenId, dc.tokenSecret)`
   - Use synchronous blocking API (not reactive - no `Mono`/`Flux`)
   - **For Server creation endpoint**: Validate all FK references (User, Template, Node) and IP availability before calling PVE API

5. **Handle Multi-PVE Delegation**
   - All endpoints must accept `@RequestParam dcId` for datacenter context
   - Validate dc_id exists before PVE API calls
   - Call `processor.process()` immediately after PVE API returns
   - Sync results with local database (upsert pattern)
   - **For Server creation**: Atomically allocate resources (IP, VM ID) before persisting
   - **For IPv4**: Support both single and batch add with optional start address for sequential allocation
   - **For IPv6**: Track ranges separately; allocate via sequential or custom method at Server creation time
   - **For User-owned resources**: Always validate `userId` exists in User table and belongs to requesting user

## External Dependencies
- **cv4pve-api-java 9.1.1**: Proxmox API client - use `client.nodes`, `client.nodes.{node}.{endpoint}`
- **Spring Data JPA**: Entity mapping and repositories
- **Spring Data Redis**: Caching layer for frequently accessed data
- **PostgreSQL**: Primary relational database

## Redis Caching Strategy

Use Redis ([service/redis/](src/main/kotlin/cn/locyan/pvecontroller/service/redis/)) for:

1. **DataCenter Configuration Cache** (most critical - loaded on every API call)
   ```kotlin
   // Cache key: "dc:{dcId}" TTL: 1 hour
   // Invalidate on DataCenter update
   @Cacheable(value = "dataCenters", key = "#dcId")
   fun findById(dcId: Long): DataCenter? { ... }
   ```

2. **Node Status Cache** (refreshed periodically via scheduled tasks)
   ```kotlin
   // Cache key: "node:{dcId}:{nodeName}" TTL: 5 minutes
   // Used by Server creation (validates node exists before provisioning)
   @Cacheable(value = "nodes", key = "#dcId.toString() + ':' + #nodeName")
   fun findByNameAndDcId(name: String, dcId: Long): Node? { ... }
   ```

3. **Template Metadata Cache** (rarely changes)
   ```kotlin
   // Cache key: "template:{dcId}:{templateId}" TTL: 24 hours
   // List all available templates for VM cloning
   @Cacheable(value = "templates", key = "#dcId")
   fun findAllByDcId(dcId: Long): List<Template>? { ... }
   ```

4. **IP Pool Cache** (synced with external IPAM)
   ```kotlin
   // Cache key: "ipv4_pool:{dcId}" TTL: 30 minutes
   // Track available/allocated IPv4 addresses for new servers
   @Cacheable(value = "ipv4Pools", key = "#dcId")
   fun getAvailableIPv4s(dcId: Long): List<IPv4>? { ... }
   
   // Cache key: "ipv4:{ipId}" TTL: 5 minutes
   // Single IPv4 lookup for Server creation
   @Cacheable(value = "ipv4s", key = "#ipId")
   fun findById(ipId: Long): IPv4? { ... }
   
   // Cache key: "ipv6_range:{rangeId}" TTL: 30 minutes
   // Track IPv6 block status and allocation counters
   @Cacheable(value = "ipv6Ranges", key = "#dcId")
   fun findAllByDcId(dcId: Long): List<IPv6Range>? { ... }
   ```

**Cache Invalidation Pattern:**
```kotlin
@CacheEvict(value = "dataCenters", key = "#dataCenter.id")
fun update(dataCenter: DataCenter): DataCenter { ... }

@CacheEvict(value = "dataCenters", allEntries = true)  // Clear all on critical changes
fun delete(id: Long) { ... }
```

**Redis Configuration:**
- Enable via `spring.data.redis.*` in `application.yml`
- Use `@EnableCaching` on main application class
- Set cache TTL per resource based on change frequency

## Environment Configuration
Critical properties in `application.yml`:
```yaml
pve:
  host: "PVE_SERVER_IP"
  port: 8006
  token-id: "root@pam!test"
  token-secret: "TOKEN_UUID"
datasource:
  url: "jdbc:postgresql://HOST:5432/DATABASE"
```
For multi-PVE: Move these to database, query per datacenter.

## Testing Notes
- PostgreSQL must be running on configured host
- PVE server credentials must be valid
- Use `@SpringBootTest` for integration tests
- Test services separately from controllers
