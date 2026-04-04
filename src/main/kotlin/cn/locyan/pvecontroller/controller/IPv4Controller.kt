package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.IPv4
import cn.locyan.pvecontroller.service.jdbc.IPv4Service
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

@RestController
@RequestMapping("/ipv4s")
class IPv4Controller(
    private val ipv4Service: IPv4Service,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody ipv4: IPv4): ResponseEntity<Response> {
        val created = ipv4Service.create(ipv4)
        return builder.ok().data(created).build()
    }

    @PostMapping("/batch")
    fun createBatch(
        @RequestParam("node_id") nodeId: Long,
        @RequestParam("start_ip") startIp: String,
        @RequestParam("count") count: Int,
        @RequestParam("gateway") gateway: String,
        @RequestParam("netmask") netmask: String
    ): ResponseEntity<Response> {
        val ipv4List = mutableListOf<IPv4>()
        val startParts = startIp.split(".")
        var lastOctet = startParts[3].toInt()

        repeat(count) {
            val ipAddress = "${startParts[0]}.${startParts[1]}.${startParts[2]}.$lastOctet"
            val ipv4 = IPv4()
            ipv4.nodeId = nodeId
            ipv4.ipAddress = ipAddress
            ipv4.gateway = gateway
            ipv4.netmask = netmask
            ipv4.isAllocated = false
            ipv4List.add(ipv4Service.create(ipv4))
            lastOctet++
        }
        return builder.ok().data(ipv4List).build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody ipv4: IPv4
    ): ResponseEntity<Response> {
        ipv4.id = id
        val updated = ipv4Service.update(ipv4)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        ipv4Service.delete(id)
        return builder.ok().message("IPv4 deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val ipv4 = ipv4Service.findById(id)
        if (ipv4 != null) {
            return builder.ok().data(ipv4).build()
        } else {
            return builder.exception().message("IPv4 not found").build()
        }
    }

    @PostMapping("/allocate")
    fun allocateIP(
        @RequestParam("node_id") nodeId: Long,
        @RequestParam("vm_id") vmId: Long
        ): ResponseEntity<Response> {
        val ipv4 = ipv4Service.allocateIP(nodeId, vmId)
        return if (ipv4 != null) {
            builder.ok().data(ipv4).build()
        } else {
            builder.exception().message("No available IPv4 for allocation").build()
        }
    }

    @PostMapping("/{id}/deallocate")
    fun deallocateIP(@PathVariable id: Long): ResponseEntity<Response> {
        ipv4Service.deallocateIP(id)
        return builder.ok().message("IPv4 deallocated successfully").build()
    }

    @GetMapping("/list/{nodeId}")
    fun findAll(@PathVariable nodeId: Long): ResponseEntity<Response> {
        val ipv4 = ipv4Service.findAllByNodeId(nodeId)
        if (!ipv4.isEmpty()) {
            return builder.ok().data(ipv4).build()
        } else {
            return builder.exception().message("IPv4 not found").build()
        }
    }

    @GetMapping("/list/{nodeId}/available")
    fun findAvailable(@PathVariable nodeId: Long): ResponseEntity<Response> {
        val ipv4 = ipv4Service.findAvailableByNodeId(nodeId)
        if (!ipv4.isEmpty()) {
            return builder.ok().data(ipv4).build()
        } else {
            return builder.exception().message("IPv4 not found").build()
        }
    }
}
