package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.Server
import cn.locyan.pvecontroller.service.jdbc.DataCenterService
import cn.locyan.pvecontroller.service.jdbc.IPv4Service
import cn.locyan.pvecontroller.service.jdbc.IPv6AllocationService
import cn.locyan.pvecontroller.service.jdbc.NodeService
import cn.locyan.pvecontroller.service.jdbc.ServerGroupService
import cn.locyan.pvecontroller.service.jdbc.ServerService
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
import java.util.UUID

@RestController
@RequestMapping("/servers")
class ServerController(
    private val serverService: ServerService,
    private val templateService: TemplateService,
    private val ipv4Service: IPv4Service,
    private val ipv6AllocationService: IPv6AllocationService,
    private val nodeService: NodeService,
    private val serverGroupService: ServerGroupService,
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
        @RequestParam("disk") disk: Long,
        @RequestParam(value = "ip_id", required = false) ipId: Long? = null,
        @RequestParam(value = "ipv6_range_id", required = false) ipv6RangeId: Long? = null,
        @RequestParam(value = "ipv6_allocation_method", required = false, defaultValue = "sequential") allocationMethod: String = "sequential",
        @RequestParam(value = "server_group_id", required = false) serverGroupId: Long? = null,
        @RequestParam(value = "name", required = false) name: String? = null
    ): ResponseEntity<Response> {
        val node = nodeService.findById(nodeId) ?: return builder.notFound().message("节点不存在").build()
        val dcId = node.dcId ?: return builder.exception().message("该节点未配置数据中心 ID").build();
        val dc = dataCenterService.findById(dcId) ?: return builder.notFound().message("数据中心不存在").build()
        val client = pveClient.newClient(dc.id!!) ?: return builder.exception().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()

        val template = templateService.findByTemplateIdAndDcId(templateId, dcId) ?: return builder.exception().message("不存在该模板").build()

        val allocatedIpId = if (ipId != null) {
            val ipv4 = ipv4Service.findById(ipId) ?: return builder.exception().message("该 IP 不存在").build()
            if (ipv4.isAllocated == true) {
                return builder.exception().message("该 IP 已被位于节点ID: ${ipv4.nodeId} 的服务器 ${ipv4.vmId} 使用").build()
            }
            ipId
        } else {
            // 不自动分配 IP
            null
        }

        var ipv6AllocationId: Long? = null
        if (ipv6RangeId != null) {
            val allocation = ipv6AllocationService.allocateIPv6(ipv6RangeId, allocationMethod)
                ?: return builder.exception().message("Failed to allocate IPv6").build()
            ipv6AllocationId = allocation.id
        }

        val vmIdReq = client.cluster.nextid.nextid()
        var check = processor.process(vmIdReq)
        if (check != null) return check
        val vmId = vmIdReq.data.asInt()
        val cloneReq = client.nodes[node.name].qemu[template.templateId].clone.cloneVm(
            vmId,
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

        // 8. Persist Server entity
        val server = Server().apply {
            this.vmId = vmId.toLong()
            this.name = name ?: "server-${System.currentTimeMillis()}"
            this.nodeName = nodeName
            this.dcId = dcId
            this.userId = userId
            this.serverGroupId = serverGroupId
            this.templateId = templateId
            this.ipId = allocatedIpId
            this.ipv6Id = ipv6AllocationId
            this.cpu = cpu
            this.memory = memory
            this.disk = disk
            this.status = "running"
            this.isDeleted = false
            this.createdTime = LocalDateTime.now()
            this.updatedTime = LocalDateTime.now()
        }
        val createdServer = serverService.create(server)

        if (allocatedIpId != null) {
            val ipv4 = ipv4Service.findById(allocatedIpId)
            if (ipv4 != null) {
                ipv4.vmId = vmId.toLong()
                ipv4Service.update(ipv4)
            }
        }

        if (ipv6AllocationId != null) {
            val ipv6Alloc = ipv6AllocationService.findById(ipv6AllocationId)
            if (ipv6Alloc != null) {
                ipv6Alloc.serverId = createdServer.id
                ipv6Alloc.vmId = vmId.toLong()
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
    fun deleteServer(@PathVariable id: Long, @RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()

        // TODO: Delete VM from PVE
        // val client = pveClient.newClient(dc.host, dc.port, dc.tokenId, dc.tokenSecret)
        // client.nodes.{node}.qemu.destroy(server.vmId)

        if (server.ipId != null) {
            ipv4Service.deallocateIP(server.ipId!!)
        }

        if (server.ipv6Id != null) {
            ipv6AllocationService.deallocateIPv6(server.ipv6Id!!)
        }

        server.isDeleted = true
        serverService.update(server)

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
    fun startServer(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        // TODO: Call PVE API to start VM
        server.status = "running"
        serverService.update(server)
        return builder.ok().data(server).message("Server started").build()
    }

    @PostMapping("/{id}/stop")
    fun stopServer(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        // TODO: Call PVE API to stop VM
        server.status = "stopped"
        serverService.update(server)
        return builder.ok().data(server).message("Server stopped").build()
    }

    @PostMapping("/{id}/reboot")
    fun rebootServer(@PathVariable id: Long): ResponseEntity<Response> {
        val server = serverService.findById(id) ?: return builder.exception().message("Server not found").build()
        // TODO: Call PVE API to reboot VM
        return builder.ok().data(server).message("Server rebooted").build()
    }
}
