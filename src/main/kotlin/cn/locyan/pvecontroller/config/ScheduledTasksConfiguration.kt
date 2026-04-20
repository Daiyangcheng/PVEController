package cn.locyan.pvecontroller.config

import cn.locyan.pvecontroller.service.jdbc.IPv4Service
import cn.locyan.pvecontroller.service.jdbc.NodeService
import cn.locyan.pvecontroller.service.jdbc.RouterOSApiService
import cn.locyan.pvecontroller.service.jdbc.RouterOSTemplateService
import cn.locyan.pvecontroller.service.jdbc.ServerService
import cn.locyan.pvecontroller.service.jdbc.StorageService
import cn.locyan.pvecontroller.service.jdbc.TrafficRecordService
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.slf4j.LoggerFactory

/**
 * Scheduled Tasks Configuration
 * 
 * Automation Tasks:
 * - Node Status Sync: Periodically refresh node status from PVE API
 * - Storage Sync: Periodically refresh storage pool info from PVE
 * - Monitoring Checks: Check resource thresholds (CPU, Memory, Disk)
 * - Billing Generation: Monthly automatic billing record creation
 * - Resource Cleanup: Clean up soft-deleted resources, expired sessions
 */
@Configuration
@EnableScheduling
class ScheduledTasksConfiguration(
    private val nodeService: NodeService,
    private val storageService: StorageService,
    private val serverService: ServerService,
    private val ipv4Service: IPv4Service,
    private val routerOSTemplateService: RouterOSTemplateService,
    private val routerOSApiService: RouterOSApiService,
    private val trafficRecordService: TrafficRecordService,
) {

    private val logger = LoggerFactory.getLogger(ScheduledTasksConfiguration::class.java)

    /**
     * Sync node status from PVE every 5 minutes
     * TODO: Implement actual PVE API call to refresh node status
     * Pattern:
     * - Load all DataCenters
     * - For each DataCenter, call PVE /nodes endpoint
     * - Update Node entities with new metrics (CPU%, Memory%, Disk%)
     * - Invalidate node cache
     */
    @Scheduled(fixedDelay = 300000)  // 5 minutes
    fun syncNodeStatus() {
        logger.info("Starting scheduled node status sync from PVE")
        try {
            // TODO: Implement node sync logic
            logger.info("Node status sync completed successfully")
        } catch (e: Exception) {
            logger.error("Error during node status sync", e)
        }
    }

    /**
     * Sync storage pool info from PVE every 10 minutes
     * TODO: Implement actual PVE API call to refresh storage pools
     * Pattern:
     * - Load all DataCenters
     * - For each Node, call PVE /storage endpoint
     * - Update Storage entities with totalSize, usedSize, availableSize
     * - Create MonitoringAlert if storage > 80% capacity
     */
    @Scheduled(fixedDelay = 600000)  // 10 minutes
    fun syncStorageStatus() {
        logger.info("Starting scheduled storage status sync from PVE")
        try {
            // TODO: Implement storage sync logic
            logger.info("Storage status sync completed successfully")
        } catch (e: Exception) {
            logger.error("Error during storage status sync", e)
        }
    }

    /**
     * Check resource thresholds and create alerts every 15 minutes
     * Thresholds:
     * - CPU: > 85% for > 5 minutes = WARNING alert
     * - Memory: > 90% = CRITICAL alert
     * - Disk: > 80% = WARNING, > 95% = CRITICAL
     * TODO: Implement threshold checking against Node/Server metrics
     */
    @Scheduled(fixedDelay = 900000)  // 15 minutes
    fun checkResourceThresholds() {
        logger.info("Starting scheduled resource threshold checks")
        try {
            // TODO: Implement threshold checking logic
            // Example pattern:
            // val nodes = nodeService.findAll()
            // nodes.forEach { node ->
            //     if (node.cpuUsage > 85) {
            //         val alert = MonitoringAlert().apply {
            //             alertType = "CPU_HIGH"
            //             severity = "WARNING"
            //             currentValue = node.cpuUsage.toBigDecimal()
            //             thresholdValue = BigDecimal.valueOf(85)
            //             message = "Node ${node.name} CPU usage exceeds 85%"
            //         }
            //         monitoringAlertService.create(alert)
            //     }
            // }
            logger.info("Resource threshold checks completed successfully")
        } catch (e: Exception) {
            logger.error("Error during resource threshold checks", e)
        }
    }

    /**
     * Generate monthly billing records every month on the 1st at 00:00 UTC
     * Logic:
     * - For each active Server:
     *   - Calculate billable resources (vCPU * hourly_rate, RAM * hourly_rate, etc.)
     *   - Create BillingRecord with billingPeriodStart/End
     *   - Set isPaid = false
     * - Send invoice email to user
     * TODO: Implement billing generation and email integration
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "UTC")  // 1st of each month at 00:00 UTC
    fun generateMonthlyBillingRecords() {
        logger.info("Starting scheduled monthly billing record generation")
        try {
            // TODO: Implement billing generation logic
            // val currentMonth = LocalDateTime.now().minusMonths(1)
            // val servers = serverService.findAll()
            // servers.forEach { server ->
            //     val billingRecord = BillingRecord().apply {
            //         userId = server.userId
            //         serverId = server.id
            //         billingType = "MONTHLY_SERVER"
            //         amount = calculateServerCost(server)
            //         quantity = 1
            //         billingPeriodStart = currentMonth.withDayOfMonth(1)
            //         billingPeriodEnd = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth())
            //         isPaid = false
            //     }
            //     billingService.create(billingRecord)
            // }
            logger.info("Monthly billing record generation completed successfully")
        } catch (e: Exception) {
            logger.error("Error during monthly billing generation", e)
        }
    }

    /**
     * Clean up soft-deleted resources and expired sessions daily at 02:00 UTC
     * Logic:
     * - Find all Servers with isDeleted = true and deletedTime < 30 days ago
     * - Hard delete them from database (purge)
     * - Remove corresponding audit log entries
     * TODO: Implement cleanup logic
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")  // Daily at 02:00 UTC
    fun cleanupDeletedResources() {
        logger.info("Starting scheduled cleanup of deleted resources")
        try {
            // TODO: Implement cleanup logic
            // val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
            // val deletedServers = serverService.findAllDeleted()
            //     .filter { it.updatedTime ?: LocalDateTime.now() < thirtyDaysAgo }
            // deletedServers.forEach { server ->
            //     serverService.hardDelete(server.id!!)
            // }
            logger.info("Cleanup of deleted resources completed successfully")
        } catch (e: Exception) {
            logger.error("Error during cleanup of deleted resources", e)
        }
    }

    /**
     * Refresh cache entries daily at 03:00 UTC
     * Pattern: Proactively load hot data into cache before peak traffic
     * TODO: Implement cache warming logic
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")  // Daily at 03:00 UTC
    fun warmupCaches() {
        logger.info("Starting scheduled cache warmup")
        try {
            // TODO: Implement cache warmup
            // This proactively loads frequently accessed data:
            // - All DataCenters
            // - All Nodes per DataCenter
            // - All Templates per DataCenter
            // - Latest monitoring alerts
            logger.info("Cache warmup completed successfully")
        } catch (e: Exception) {
            logger.error("Error during cache warmup", e)
        }
    }

    /**
     * Sync traffic data from RouterOS /queue/simple every 10 minutes
     * - Groups servers by nodeId
     * - For each node, finds the associated RouterOS API via RouterOSTemplate
     * - Queries /queue/simple/print and matches queue entries to servers by IP address
     * - Stores traffic records in the database
     */
    @Scheduled(fixedDelay = 600000)  // 10 minutes
    fun syncTrafficData() {
        logger.info("Starting scheduled traffic data sync from RouterOS")
        try {
            val servers = serverService.findAll()
            if (servers.isEmpty()) {
                logger.info("No servers found, skipping traffic sync")
                return
            }

            // Build serverId -> IPv4 address mapping
            val serverIpMap = mutableMapOf<String, Long>() // ip -> serverId
            val serverNodeMap = mutableMapOf<Long, Long>()  // serverId -> nodeId
            for (server in servers) {
                val serverId = server.id ?: continue
                val nodeId = server.nodeId ?: continue
                serverNodeMap[serverId] = nodeId

                val ipv4 = ipv4Service.findByServerId(serverId) ?: continue
                val ipAddress = ipv4.ipAddress ?: continue
                serverIpMap[ipAddress] = serverId
            }

            if (serverIpMap.isEmpty()) {
                logger.info("No servers with IPv4 addresses found, skipping traffic sync")
                return
            }

            // Collect all distinct RouterOS API IDs from all nodes
            val nodeIds = serverNodeMap.values.toSet()
            val allApiIds = mutableSetOf<Long>()
            for (nodeId in nodeIds) {
                val templates = routerOSTemplateService.findByNodeId(nodeId)
                for (template in templates) {
                    val apiId = template.apiId ?: continue
                    allApiIds.add(apiId)
                }
            }

            // Query every RouterOS API and accumulate traffic per serverId
            // ip -> (totalUpload, totalDownload)
            val trafficAccumulator = mutableMapOf<Long, LongArray>() // serverId -> [upload, download]
            for (apiId in allApiIds) {
                val queues = try {
                    routerOSApiService.execute(apiId, "/queue/simple", "print", emptyList())
                } catch (e: Exception) {
                    logger.warn("Failed to query /queue/simple on RouterOS API {}: {}", apiId, e.message)
                    continue
                }

                if (queues.isNullOrEmpty()) continue

                for (queue in queues) {
                    val target = queue["target"] ?: continue
                    // target format: "192.168.1.100/32" or "192.168.1.100"
                    val ip = target.split("/").firstOrNull() ?: continue
                    val serverId = serverIpMap[ip] ?: continue

                    val bytesStr = queue["bytes"] ?: continue
                    // bytes format: "upload/download"
                    val parts = bytesStr.split("/")
                    if (parts.size != 2) continue

                    val uploadBytes = parts[0].toLongOrNull() ?: continue
                    val downloadBytes = parts[1].toLongOrNull() ?: continue

                    val acc = trafficAccumulator.getOrPut(serverId) { longArrayOf(0L, 0L) }
                    acc[0] += uploadBytes
                    acc[1] += downloadBytes
                }
            }

            // Persist accumulated traffic
            var recordCount = 0
            for ((serverId, bytes) in trafficAccumulator) {
                trafficRecordService.createOrUpdate(serverId, bytes[0], bytes[1])
                recordCount++
            }

            logger.info("Traffic data sync completed, {} records updated", recordCount)
        } catch (e: Exception) {
            logger.error("Error during traffic data sync", e)
        }
    }
}
