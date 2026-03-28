package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.IPv6Allocation
import cn.locyan.pvecontroller.model.Server
import cn.locyan.pvecontroller.service.jdbc.DataCenterService
import cn.locyan.pvecontroller.service.jdbc.IPv4Service
import cn.locyan.pvecontroller.service.jdbc.IPv6AllocationService
import cn.locyan.pvecontroller.service.jdbc.IPv6RangeService
import cn.locyan.pvecontroller.service.jdbc.NodeService
import cn.locyan.pvecontroller.service.jdbc.ServerGroupService
import cn.locyan.pvecontroller.service.jdbc.ServerService
import cn.locyan.pvecontroller.service.jdbc.TemplateGroupService
import cn.locyan.pvecontroller.service.jdbc.TemplateService
import cn.locyan.pvecontroller.shared.pve.PVEClient
import cn.locyan.pvecontroller.shared.pve.ProcessPVEResult
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
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
import java.time.LocalDateTime

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
    private val serverGroupService: ServerGroupService,
    private val dataCenterService: DataCenterService,
    private val pveClient: PVEClient,
    private val processor: ProcessPVEResult,
    private val builder: ResponseBuilder
) {

    // serverId 虚拟机在数据库中的自增 ID
    // vmId 虚拟机在 PVE 中的 VMID，考虑到不同的数据中心可能会重复因此不用此 ID 作为 primaryKey
    @PostMapping
    fun createServer(
        @RequestParam("node_id") nodeId: Long,
        @RequestParam("user_id") userId: Long,
        @RequestParam("template_id") templateId: Long,
        @RequestParam("cpu") cpu: Long,
        @RequestParam("memory") memory: Long,
        @RequestParam("sshkeys") sshKeys: String,
        @RequestParam("disk") disk: Long,
        @RequestParam(value = "ip_id", required = false) ipId: Long? = null,
        @RequestParam(value = "ipv6_range_id", required = false) ipv6RangeId: Long? = null,
        @RequestParam(value = "server_group_id", required = false) serverGroupId: Long? = null,
        @RequestParam(value = "name", required = false) name: String? = null,
    ): ResponseEntity<Response> {
        val node = nodeService.findById(nodeId) ?: return builder.notFound().message("节点不存在").build()
        val dc = dataCenterService.findById(node.dcId!!) ?: return builder.notFound().message("数据中心不存在").build()
        val serverGroup = serverGroupService.findById(serverGroupId!!) ?: return builder.notFound().message("服务器组别不存在").build()
        if (nodeId != serverGroup.nodeId) return builder.exception().message("服务器组不可跨数据中心使用").build()
        val client = pveClient.newClient(dc.id!!) ?: return builder.exception().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()

        val template = templateService.findById(templateId) ?: return builder.exception().message("不存在该模板").build()
        val templateGroupId = template.templateGroupId
        val templateGroup = templateGroupService.findById(templateGroupId!!) ?: return builder.notFound().message("该模板组不存在").build()
        if (templateGroup.nodeId != nodeId) return builder.forbidden().message("模板与服务器节点不匹配").build()

        // 先分配，后更新 serverId 和 vmId
        // 分配 IPv4
        val ipv4 = if (ipId != null) {
            val ipv4 = ipv4Service.findById(ipId) ?: return builder.exception().message("该 IP 不存在").build()
            if (ipv4.isAllocated == true) {
                return builder.exception().message("该 IP 已被位于节点ID: ${ipv4.nodeId} 的服务器 ${ipv4.vmId} 使用").build()
            }
            ipv4
        } else {
            // 不分配 IP
            null
        }

        // 分配 IPv6
        var ipv6: IPv6Allocation? = null
        if (ipv6RangeId != null) {
            ipv6 = ipv6AllocationService.allocateIPv6(ipv6RangeId) ?: return builder.exception().message("Failed to allocate IPv6").build()
        }

        // 获取下一个 VMID
        val vmIdReq = client.cluster.nextid.nextid()
        var check = processor.process(vmIdReq)
        if (check != null) return check
        val vmId = vmIdReq.data.asLong()

        val cloneReq = client.nodes[node.name].qemu[template.templateId].clone.cloneVm(
            vmId.toInt(),
            null,
            "",
            "qcow2",
            true,
            name ?: "server-${System.currentTimeMillis()}",
            null,
            null,
            null,
            null
        )
        check = processor.process(cloneReq)
        if (check != null) return check

        val ipConfigMap = buildMap {
            ipv4?.let {
                put(0, "ip=${it.ipAddress}/${it.netmask},gw=${it.gateway}")
            }
            ipv6?.let {
                val ipv6Range = ipv6RangeService.findById(it.ipv6RangeId!!) ?: return builder.exception().message("该 IPv6 范围不存在").build()
                put(0, "ip=${it.assignedAddress}/${ipv6Range.prefixLength},gw=${ipv6Range.gateway}")
            }
        }

        val cloudInitReq = client.nodes[node.name].qemu[vmId].config.updateVmAsync(
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
            /* cores */ null,
            /* cpu */ cpu.toString(),
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
            /* sshkeys */ sshKeys,
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
        if (check != null) return check

        val server = Server().apply {
            this.vmId = vmId
            this.name = name ?: "server-${System.currentTimeMillis()}"
            this.nodeId = nodeId
            this.dcId = dcId
            this.userId = userId
            this.serverGroupId = serverGroup.id
            this.templateId = templateId
            this.ipId = ipv4?.id
            this.ipv6Id = ipv6?.id
            this.cpu = cpu
            this.memory = memory
            this.disk = disk
            this.status = "stopped"
            this.createdTime = LocalDateTime.now()
            this.updatedTime = LocalDateTime.now()
        }
        val createdServer = serverService.create(server)

        // 更新分配的 IP 的 VM 信息
        //TODO: 支持多个IP分配
        if (ipv4?.id != null) {
            val ipv4 = ipv4Service.findById(ipv4.id!!)
            if (ipv4 != null) {
                ipv4.vmId = vmId
                ipv4.serverId = createdServer.id
                ipv4Service.update(ipv4)
            }
        }

        if (ipv6?.id != null) {
            val ipv6Alloc = ipv6AllocationService.findById(ipv6.id!!)
            if (ipv6Alloc != null) {
                ipv6Alloc.serverId = createdServer.id
                ipv6Alloc.vmId = vmId
                ipv6AllocationService.update(ipv6Alloc)
            }
        }

        return builder.ok().data(createdServer).message("Server created successfully").build()
    }


    @PutMapping("/{id}")
    fun updateServer(@PathVariable id: Long, @RequestBody server: Server): ResponseEntity<Response> {
        server.id = id
        val updated = serverService.update(server)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun deleteServer(
        @PathVariable id: Long,
        @RequestParam("dc_id") dcId: Long
    ): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.exception().message("找不到该节点").build()
        dataCenterService.findById(dcId) ?: return builder.notFound().message("数据中心不存在").build()
        val client = pveClient.newClient(dcId) ?: return builder.notFound().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()

        val deleteReq = client.nodes[node.name].qemu[server.vmId].destroyVm()
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
        if (server != null) {
            return builder.ok().data(server).build()
        } else {
            return builder.exception().message("Server not found").build()
        }
    }

    @GetMapping
    fun findAllByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val servers = serverService.findAllByDcId(dcId)
        return builder.ok().data(servers).build()
    }

    @GetMapping("/user")
    fun findByUserId(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val servers = serverService.findByUserId(userId)
        return builder.ok().data(servers).build()
    }

    @GetMapping("/group")
    fun findByServerGroupId(@RequestParam("group_id") groupId: Long): ResponseEntity<Response> {
        val servers = serverService.findByServerGroupId(groupId)
        return builder.ok().data(servers).build()
    }

    @PostMapping("/{id}/start")
    fun startServer(
        @PathVariable id: Long
    ): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("节点不存在!").build()
        val client = pveClient.newClient(server.dcId!!) ?: return builder.exception().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()
        val startReq = client.nodes[node.name].qemu[server.vmId].status.start.vmStart()
        val check = processor.process(startReq)
        if (check != null) return check

        server.status = "running"
        serverService.update(server)
        return builder.ok().data(server).message("Server started").build()
    }

    @PostMapping("/{id}/stop")
    fun stopServer(
        @PathVariable id: Long
    ): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("节点不存在!").build()
        val client = pveClient.newClient(server.dcId!!) ?: return builder.exception().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()
        val startReq = client.nodes[node.name].qemu[server.vmId].status.stop.vmStop()
        val check = processor.process(startReq)
        if (check != null) return check

        server.status = "stopped"
        serverService.update(server)
        return builder.ok().data(server).message("Server stopped").build()
    }

    @PostMapping("/{id}/reboot")
    fun rebootServer(
        @PathVariable id: Long
    ): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        val node = nodeService.findById(server.nodeId!!) ?: return builder.notFound().message("节点不存在!").build()
        val client = pveClient.newClient(server.dcId!!) ?: return builder.exception().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()
        val startReq = client.nodes[node.name].qemu[server.vmId].status.reboot.vmReboot()
        val check = processor.process(startReq)
        if (check != null) return check

        return builder.ok().data(server).message("Server rebooted").build()
    }
}