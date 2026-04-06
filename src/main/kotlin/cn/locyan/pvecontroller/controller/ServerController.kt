package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.IPv4
import cn.locyan.pvecontroller.model.IPv6Allocation
import cn.locyan.pvecontroller.model.IPv6Range
import cn.locyan.pvecontroller.model.Server
import cn.locyan.pvecontroller.service.jdbc.DataCenterService
import cn.locyan.pvecontroller.service.jdbc.IPv4Service
import cn.locyan.pvecontroller.service.jdbc.IPv6AllocationService
import cn.locyan.pvecontroller.service.jdbc.IPv6RangeService
import cn.locyan.pvecontroller.service.jdbc.NodeService
import cn.locyan.pvecontroller.service.jdbc.ServerService
import cn.locyan.pvecontroller.service.jdbc.TemplateGroupService
import cn.locyan.pvecontroller.service.jdbc.TemplateService
import cn.locyan.pvecontroller.shared.pve.PVEClient
import cn.locyan.pvecontroller.shared.pve.ProcessPVEResult
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import kotlin.math.ceil

@RestController
@RequestMapping("/servers")
class ServerController(
    private val serverService: ServerService,
    private val templateService: TemplateService,
    private val templateGroupService: TemplateGroupService,
    private val ipv4Service: IPv4Service,
    private val ipv6AllocationService: IPv6AllocationService,
    private val ipv6RangeService: IPv6RangeService,
    private val nodeService: NodeService,
    private val dataCenterService: DataCenterService,
    private val pveClient: PVEClient,
    private val processor: ProcessPVEResult,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun createServer(
        @RequestParam("node_id") nodeId: Long,
        @RequestParam("user_id") userId: Long,
        @RequestParam("template_id") templateId: Long,
        @RequestParam("cpu") cpu: Long,
        @RequestParam("memory") memory: Long,
        @RequestParam(value = "sshkeys", required = false) sshKeys: String? = null,
        @RequestParam("disk") disk: Long,
        @RequestParam(value = "ip_id", required = false) ipId: Long? = null,
        @RequestParam(value = "auto_allocate_ipv4", required = false, defaultValue = "false") autoAllocateIpv4: Boolean,
        @RequestParam(value = "ipv6_range_id", required = false) ipv6RangeId: Long? = null,
        @RequestParam(value = "auto_allocate_ipv6", required = false, defaultValue = "false") autoAllocateIpv6: Boolean,
        @RequestParam(value = "server_group_id", required = false) serverGroupId: Long? = null,
        @RequestParam(value = "name", required = false) name: String? = null,
    ): ResponseEntity<Response> {
        if (cpu <= 0 || memory <= 0 || disk <= 0) {
            return builder.badRequest().message("CPU, memory and disk must be positive").build()
        }
        if (ipId != null && autoAllocateIpv4) {
            return builder.badRequest().message("IPv4 cannot be both fixed and auto allocated").build()
        }
        if (ipv6RangeId != null && autoAllocateIpv6) {
            return builder.badRequest().message("IPv6 cannot be both fixed and auto allocated").build()
        }

        val node = nodeService.findById(nodeId) ?: return builder.notFound().message("Node not found").build()
        val nodeName = node.name ?: return builder.notFound().message("Node name is missing").build()
        val dcId = node.dcId ?: return builder.notFound().message("Datacenter not found").build()
        val dc = dataCenterService.findById(dcId) ?: return builder.notFound().message("Datacenter not found").build()
        val client = pveClient.newClient(dc.id!!) ?: return builder.exception().message("Unable to connect to PVE").build()
        val normalizedName = name?.trim()?.takeIf { it.isNotEmpty() } ?: "server-${System.currentTimeMillis()}"
        val encodedSshKeys = encodeCloudInitSshKeys(sshKeys)

        val template = templateService.findById(templateId)
            ?: return builder.exception().message("Template not found").build()
        val templateGroupId = template.templateGroupId
            ?: return builder.exception().message("Template group is missing").build()
        val templateGroup = templateGroupService.findById(templateGroupId)
            ?: return builder.notFound().message("Template group not found").build()
        if (templateGroup.nodeId != nodeId) {
            return builder.forbidden().message("Template does not belong to the selected node").build()
        }

        val requestedIpv4 = if (ipId != null) {
            val address = ipv4Service.findById(ipId) ?: return builder.exception().message("IPv4 not found").build()
            if (address.nodeId != nodeId) {
                return builder.forbidden().message("The selected IPv4 does not belong to the requested node").build()
            }
            if (address.isAllocated == true) {
                return builder.exception().message("The selected IPv4 is already allocated").build()
            }
            address
        } else {
            null
        }

        val ipv6Range = if (ipv6RangeId != null) {
            val range = ipv6RangeService.findById(ipv6RangeId)
                ?: return builder.exception().message("IPv6 range not found").build()
            if (range.nodeId != nodeId) {
                return builder.forbidden().message("The selected IPv6 range does not belong to the requested node").build()
            }
            if (range.isActive != true) {
                return builder.badRequest().message("The selected IPv6 range is not active").build()
            }
            range
        } else if (autoAllocateIpv6) {
            ipv6RangeService.findActiveByNodeId(nodeId).firstOrNull()
                ?: return builder.exception().message("No active IPv6 range is available on the selected node").build()
        } else {
            null
        }

        val vmIdReq = client.cluster.nextid.nextid()
        var check = processor.process(vmIdReq)
        if (check != null) return check
        val vmId = vmIdReq.data.asLong()

        val ipv4 = when {
            autoAllocateIpv4 -> ipv4Service.allocateIP(nodeId, vmId)
                ?: return builder.exception().message("No available IPv4 address on the selected node").build()

            requestedIpv4 != null -> {
                val address = ipv4Service.findById(requestedIpv4.id!!)
                    ?: return builder.exception().message("IPv4 not found").build()
                if (address.nodeId != nodeId) {
                    return builder.forbidden().message("The selected IPv4 does not belong to the requested node").build()
                }
                if (address.isAllocated == true) {
                    return builder.exception().message("The selected IPv4 is already allocated").build()
                }
                address.isAllocated = true
                address.vmId = vmId
                ipv4Service.update(address)
            }

            else -> null
        }

        val ipv6 = if (ipv6Range != null) {
            ipv6AllocationService.allocateIPv6(ipv6Range.id!!)
                ?: run {
                    ipv4?.id?.let(ipv4Service::deallocateIP)
                    return builder.exception().message("Failed to allocate IPv6").build()
                }
        } else {
            null
        }

        val cloneReq = client.nodes[nodeName].qemu[template.templateId].clone.cloneVm(
            vmId.toInt(),
            null,
            "",
            "qcow2",
            true,
            normalizedName,
            null,
            null,
            null,
            null
        )
        check = processor.process(cloneReq)
        if (check != null) {
            cleanupFailedProvision(nodeId, nodeName, vmId, ipv4?.id, ipv6?.id)
            return check
        }

        val ipConfigMap = buildCloudInitIpConfig(ipv4, ipv6, ipv6Range)

        val cloudInitReq = client.nodes[nodeName].qemu[vmId].config.updateVmAsync(
            /* acpi */ null,
            /* affinity */ null,
            /* agent */ null,
            /* allow_ksm */ null,
            /* amd_sev */ null,
            /* arch */ null,
            /* args */ null,
            /* audio0 */ null,
            /* autostart */ false,
            /* background_delay */ null,
            /* balloon */ null,
            /* bios */ null,
            /* boot */ null,
            /* bootdisk */ null,
            /* cdrom */ null,
            /* cicustom */ null,
            /* cipassword */ null,
            /* citype */ null,
            /* ciupgrade */ false,
            /* ciuser */ null,
            /* cores */ cpu.toInt(),
            /* cpu */ "host",
            /* cpulimit */ null,
            /* cpuunits */ null,
            /* delete */ null,
            /* description */ null,
            /* digest */ null,
            /* efidisk0 */ null,
            /* force */ null,
            /* freeze */ null,
            /* hookscript */ null,
            /* hostpciN */ null,
            /* hotplug */ null,
            /* hugepages */ null,
            /* ideN */ null,
            /* import_working_storage */ null,
            /* intel_tdx */ null,
            /* ipconfigN */ ipConfigMap,
            /* ivshmem */ null,
            /* keephugepages */ null,
            /* keyboard */ null,
            /* kvm */ null,
            /* localtime */ null,
            /* lock_ */ null,
            /* machine */ null,
            /* memory */ memory.toString(),
            /* migrate_downtime */ null,
            /* migrate_speed */ null,
            /* name */ null,
            /* nameserver */ "1.1.1.1 8.8.8.8",
            /* netN */ null,
            /* numa */ null,
            /* numaN */ null,
            /* onboot */ null,
            /* ostype */ null,
            /* parallelN */ null,
            /* protection */ null,
            /* reboot */ null,
            /* revert */ null,
            /* rng0 */ null,
            /* sataN */ null,
            /* scsiN */ null,
            /* scsihw */ null,
            /* searchdomain */ null,
            /* serialN */ null,
            /* shares */ null,
            /* skiplock */ null,
            /* smbios1 */ null,
            /* smp */ null,
            /* sockets */ null,
            /* spice_enhancements */ null,
            /* sshkeys */ encodedSshKeys,
            /* startdate */ null,
            /* startup */ null,
            /* tablet */ null,
            /* tags */ null,
            /* tdf */ null,
            /* template */ null,
            /* tpmstate0 */ null,
            /* unusedN */ null,
            /* usbN */ null,
            /* vcpus */ null,
            /* vga */ null,
            /* virtioN */ null,
            /* virtiofsN */ null,
            /* vmgenid */ null,
            /* vmstatestorage */ null,
            /* watchdog */ null
        )
        check = processor.process(cloudInitReq)
        if (check != null) {
            cleanupFailedProvision(nodeId, nodeName, vmId, ipv4?.id, ipv6?.id)
            return check
        }

        val vmConfigReq = client.nodes[nodeName].qemu[vmId].config.vmConfig(true, null)
        check = processor.process(vmConfigReq)
        if (check != null) {
            cleanupFailedProvision(nodeId, nodeName, vmId, ipv4?.id, ipv6?.id)
            return check
        }

        val primaryDiskKey = resolvePrimaryDiskKey(vmConfigReq.data)
        val currentDiskGb = extractDiskSizeGb(primaryDiskKey?.let { vmConfigReq.data.get(it)?.asText() })
        val effectiveDiskGb = maxOf(disk, currentDiskGb ?: disk)

        if (primaryDiskKey != null && disk > (currentDiskGb ?: 0L)) {
            val resizeSize = if (currentDiskGb != null) {
                "+${disk - currentDiskGb}G"
            } else {
                "${disk}G"
            }

            val resizeReq = client.nodes[nodeName].qemu[vmId].resize.resizeVm(primaryDiskKey, resizeSize)
            check = processor.process(resizeReq)
            if (check != null) {
                cleanupFailedProvision(nodeId, nodeName, vmId, ipv4?.id, ipv6?.id)
                return check
            }
        }

        var createdServer: Server? = null
        try {
            createdServer = serverService.create(
                Server().apply {
                    this.vmId = vmId
                    this.name = normalizedName
                    this.nodeId = nodeId
                    this.dcId = dcId
                    this.userId = userId
                    this.serverGroupId = serverGroupId
                    this.templateId = templateId
                    this.ipId = ipv4?.id
                    this.ipv6Id = ipv6?.id
                    this.cpu = cpu
                    this.memory = memory
                    this.disk = effectiveDiskGb
                    this.status = "stopped"
                    this.createdTime = LocalDateTime.now()
                    this.updatedTime = LocalDateTime.now()
                }
            )

            if (ipv4?.id != null) {
                val allocatedIpv4 = ipv4Service.findById(ipv4.id!!)
                if (allocatedIpv4 != null) {
                    allocatedIpv4.serverId = createdServer.id
                    ipv4Service.update(allocatedIpv4)
                }
            }

            if (ipv6?.id != null) {
                val allocatedIpv6 = ipv6AllocationService.findById(ipv6.id!!)
                if (allocatedIpv6 != null) {
                    allocatedIpv6.isAllocated = true
                    allocatedIpv6.serverId = createdServer.id
                    allocatedIpv6.vmId = vmId
                    ipv6AllocationService.update(allocatedIpv6)
                }
            }

            return builder.ok().data(createdServer).message("Server created successfully").build()
        } catch (exception: Exception) {
            rollbackProvisionedState(createdServer, ipv4?.id, ipv6?.id, nodeId, nodeName, vmId)
            return builder.exception().message("Failed to persist provisioned server: ${exception.message}").build()
        }
    }

    @PutMapping("/{id}")
    fun updateServer(@PathVariable id: Long, @RequestBody server: Server): ResponseEntity<Response> {
        server.id = id
        val updated = serverService.update(server)
        return builder.ok().data(updated).build()
    }

    @PostMapping("/{id}/upgrade")
    fun upgradeServer(
        @PathVariable id: Long,
        @RequestParam("cpu") cpu: Long,
        @RequestParam("memory") memory: Long,
        @RequestParam("disk") disk: Long,
        @RequestParam(value = "name", required = false) name: String? = null,
    ): ResponseEntity<Response> {
        if (cpu <= 0 || memory <= 0 || disk <= 0) {
            return builder.badRequest().message("CPU, memory and disk must be positive").build()
        }

        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("Node not found").build()
        val nodeName = node.name ?: return builder.notFound().message("Node name is missing").build()
        val client = pveClient.newClient(node.dcId!!) ?: return builder.exception().message("Unable to connect to PVE").build()

        val vmConfigReq = client.nodes[nodeName].qemu[server.vmId].config.vmConfig(true, null)
        var check = processor.process(vmConfigReq)
        if (check != null) return check

        val primaryDiskKey = resolvePrimaryDiskKey(vmConfigReq.data)
        val currentDiskGb = extractDiskSizeGb(primaryDiskKey?.let { vmConfigReq.data.get(it)?.asText() }) ?: server.disk ?: 0L
        if (disk < currentDiskGb) {
            return builder.badRequest().message("Disk shrinking is not supported").build()
        }

        val updateReq = client.nodes[nodeName].qemu[server.vmId].config.updateVmAsync(
            /* acpi */ null,
            /* affinity */ null,
            /* agent */ null,
            /* allow_ksm */ null,
            /* amd_sev */ null,
            /* arch */ null,
            /* args */ null,
            /* audio0 */ null,
            /* autostart */ null,
            /* background_delay */ null,
            /* balloon */ null,
            /* bios */ null,
            /* boot */ null,
            /* bootdisk */ null,
            /* cdrom */ null,
            /* cicustom */ null,
            /* cipassword */ null,
            /* citype */ null,
            /* ciupgrade */ null,
            /* ciuser */ null,
            /* cores */ cpu.toInt(),
            /* cpu */ null,
            /* cpulimit */ null,
            /* cpuunits */ null,
            /* delete */ null,
            /* description */ null,
            /* digest */ null,
            /* efidisk0 */ null,
            /* force */ null,
            /* freeze */ null,
            /* hookscript */ null,
            /* hostpciN */ null,
            /* hotplug */ null,
            /* hugepages */ null,
            /* ideN */ null,
            /* import_working_storage */ null,
            /* intel_tdx */ null,
            /* ipconfigN */ null,
            /* ivshmem */ null,
            /* keephugepages */ null,
            /* keyboard */ null,
            /* kvm */ null,
            /* localtime */ null,
            /* lock_ */ null,
            /* machine */ null,
            /* memory */ memory.toString(),
            /* migrate_downtime */ null,
            /* migrate_speed */ null,
            /* name */ name?.trim()?.takeIf { it.isNotEmpty() },
            /* nameserver */ null,
            /* netN */ null,
            /* numa */ null,
            /* numaN */ null,
            /* onboot */ null,
            /* ostype */ null,
            /* parallelN */ null,
            /* protection */ null,
            /* reboot */ null,
            /* revert */ null,
            /* rng0 */ null,
            /* sataN */ null,
            /* scsiN */ null,
            /* scsihw */ null,
            /* searchdomain */ null,
            /* serialN */ null,
            /* shares */ null,
            /* skiplock */ null,
            /* smbios1 */ null,
            /* smp */ null,
            /* sockets */ null,
            /* spice_enhancements */ null,
            /* sshkeys */ null,
            /* startdate */ null,
            /* startup */ null,
            /* tablet */ null,
            /* tags */ null,
            /* tdf */ null,
            /* template */ null,
            /* tpmstate0 */ null,
            /* unusedN */ null,
            /* usbN */ null,
            /* vcpus */ null,
            /* vga */ null,
            /* virtioN */ null,
            /* virtiofsN */ null,
            /* vmgenid */ null,
            /* vmstatestorage */ null,
            /* watchdog */ null
        )
        check = processor.process(updateReq)
        if (check != null) return check

        if (disk > currentDiskGb) {
            val diskKey = primaryDiskKey ?: return builder.exception().message("Primary disk not found").build()
            val resizeReq = client.nodes[nodeName].qemu[server.vmId].resize.resizeVm(diskKey, "+${disk - currentDiskGb}G")
            check = processor.process(resizeReq)
            if (check != null) return check
        }

        server.cpu = cpu
        server.memory = memory
        server.disk = disk
        name?.trim()?.takeIf { it.isNotEmpty() }?.let { server.name = it }
        val updatedServer = serverService.update(server)
        return builder.ok().data(updatedServer).message("Server upgraded").build()
    }

    @DeleteMapping("/{id}")
    fun deleteServer(
        @PathVariable id: Long,
        @RequestParam("dc_id", required = false) dcId: Long? = null
    ): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("Node not found").build()
        val nodeName = node.name ?: return builder.notFound().message("Node name is missing").build()
        val resolvedDcId = node.dcId ?: return builder.notFound().message("Datacenter not found").build()
        if (dcId != null && dcId != resolvedDcId) {
            return builder.badRequest().message("The provided datacenter does not match the server node").build()
        }
        dataCenterService.findById(resolvedDcId) ?: return builder.notFound().message("Datacenter not found").build()
        val client = pveClient.newClient(resolvedDcId) ?: return builder.exception().message("Unable to connect to PVE").build()

        val deleteReq = client.nodes[nodeName].qemu[server.vmId].destroyVm()
        val check = processor.process(deleteReq)
        if (check != null) return check

        if (server.ipId != null) {
            ipv4Service.deallocateIP(server.ipId!!)
        }

        if (server.ipv6Id != null) {
            ipv6AllocationService.deallocateIPv6(server.ipv6Id!!)
        }

        serverService.delete(server)
        return builder.ok().message("Server deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id)
        return if (server != null) {
            builder.ok().data(server).build()
        } else {
            builder.exception().message("Server not found").build()
        }
    }

    @GetMapping
    fun findByUserId(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val servers = serverService.findByUserId(userId)
        return builder.ok().data(servers).build()
    }

    @PostMapping("/{id}/start")
    fun startServer(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("Node not found").build()
        val nodeName = node.name ?: return builder.notFound().message("Node name is missing").build()
        val client = pveClient.newClient(node.dcId!!) ?: return builder.exception().message("Unable to connect to PVE").build()

        val startReq = client.nodes[nodeName].qemu[server.vmId].status.start.vmStart()
        val check = processor.process(startReq)
        if (check != null) return check

        server.status = "running"
        serverService.update(server)
        return builder.ok().data(server).message("Server started").build()
    }

    @PostMapping("/{id}/stop")
    fun stopServer(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("Node not found").build()
        val nodeName = node.name ?: return builder.notFound().message("Node name is missing").build()
        val client = pveClient.newClient(node.dcId!!) ?: return builder.exception().message("Unable to connect to PVE").build()

        val stopReq = client.nodes[nodeName].qemu[server.vmId].status.stop.vmStop()
        val check = processor.process(stopReq)
        if (check != null) return check

        server.status = "stopped"
        serverService.update(server)
        return builder.ok().data(server).message("Server stopped").build()
    }

    @PostMapping("/{id}/reboot")
    fun rebootServer(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("Node not found").build()
        val nodeName = node.name ?: return builder.notFound().message("Node name is missing").build()
        val client = pveClient.newClient(node.dcId!!) ?: return builder.exception().message("Unable to connect to PVE").build()

        val rebootReq = client.nodes[nodeName].qemu[server.vmId].status.reboot.vmReboot()
        val check = processor.process(rebootReq)
        if (check != null) return check

        return builder.ok().data(server).message("Server rebooted").build()
    }

    private fun resolvePrimaryDiskKey(vmConfig: JsonNode): String? {
        val bootDisk = vmConfig.get("bootdisk")?.asText()?.takeIf { it.isNotBlank() }
        if (bootDisk != null && vmConfig.has(bootDisk)) {
            return bootDisk
        }

        val candidates = buildList {
            addAll((0..30).map { "scsi$it" })
            addAll((0..15).map { "virtio$it" })
            addAll((0..5).map { "sata$it" })
            addAll((0..3).map { "ide$it" })
        }

        return candidates.firstOrNull { vmConfig.has(it) } ?: bootDisk
    }

    private fun buildCloudInitIpConfig(
        ipv4: IPv4?,
        ipv6: IPv6Allocation?,
        ipv6Range: IPv6Range?
    ): Map<Int, String> {
        val segments = buildList {
            ipv4?.let {
                add("ip=${it.ipAddress}/${it.netmask}")
                add("gw=${it.gateway}")
            }
            if (ipv6 != null && ipv6Range != null) {
                add("ip6=${ipv6.assignedAddress}/${ipv6Range.prefixLength}")
                add("gw6=${ipv6Range.gateway}")
            }
        }

        return if (segments.isEmpty()) {
            emptyMap()
        } else {
            mapOf(0 to segments.joinToString(","))
        }
    }

    private fun encodeCloudInitSshKeys(sshKeys: String?): String? {
        val normalized = sshKeys
            ?.lineSequence()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.joinToString("\n")
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return URLEncoder.encode(normalized, StandardCharsets.UTF_8).replace("+", "%20")
    }

    private fun cleanupFailedProvision(nodeId: Long, nodeName: String, vmId: Long, ipv4Id: Long?, ipv6Id: Long?) {
        try {
            val node = nodeService.findById(nodeId) ?: return
            val client = pveClient.newClient(node.dcId ?: return) ?: return
            client.nodes[nodeName].qemu[vmId].destroyVm()
        } catch (_: Exception) {
        } finally {
            ipv4Id?.let {
                try {
                    ipv4Service.deallocateIP(it)
                } catch (_: Exception) {
                }
            }
            ipv6Id?.let {
                try {
                    ipv6AllocationService.deallocateIPv6(it)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun rollbackProvisionedState(
        createdServer: Server?,
        ipv4Id: Long?,
        ipv6Id: Long?,
        nodeId: Long,
        nodeName: String,
        vmId: Long
    ) {
        createdServer?.let {
            try {
                serverService.delete(it)
            } catch (_: Exception) {
            }
        }

        cleanupFailedProvision(nodeId, nodeName, vmId, ipv4Id, ipv6Id)
    }

    private fun extractDiskSizeGb(diskConfig: String?): Long? {
        if (diskConfig.isNullOrBlank()) {
            return null
        }

        val match = Regex("size=(\\d+(?:\\.\\d+)?)([KMGT])", RegexOption.IGNORE_CASE).find(diskConfig) ?: return null
        val value = match.groupValues[1].toDoubleOrNull() ?: return null
        val unit = match.groupValues[2].uppercase()

        val sizeInGb = when (unit) {
            "T" -> value * 1024
            "G" -> value
            "M" -> value / 1024
            "K" -> value / (1024 * 1024)
            else -> return null
        }

        return ceil(sizeInGb).toLong()
    }
}
