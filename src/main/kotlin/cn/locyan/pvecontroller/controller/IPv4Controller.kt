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
import java.net.InetAddress

@RestController
@RequestMapping("/ipv4s")
class IPv4Controller(
    private val ipv4Service: IPv4Service,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody ipv4: IPv4): ResponseEntity<Response> {
        val validationError = validateIpv4Payload(ipv4)
        if (validationError != null) {
            return builder.badRequest().message(validationError).build()
        }
        normalizeNatFields(ipv4)
        val created = ipv4Service.create(ipv4)
        return builder.ok().data(created).build()
    }

    @PostMapping("/batch")
    fun createBatch(
        @RequestParam("node_id") nodeId: Long,
        @RequestParam("start_ip") startIp: String,
        @RequestParam("count") count: Int,
        @RequestParam("gateway") gateway: String,
        @RequestParam("netmask") netmask: String,
        @RequestParam(value = "is_nat_ip", required = false, defaultValue = "false") isNatIp: Boolean,
        @RequestParam(value = "external_ip_address", required = false) externalIpAddress: String? = null,
        @RequestParam(value = "remote_port", required = false) remotePort: Int? = null,
        @RequestParam(value = "port_range_start", required = false) portRangeStart: Int? = null,
        @RequestParam(value = "port_range_end", required = false) portRangeEnd: Int? = null,
        @RequestParam(value = "ip_group_id", required = false) ipGroupId: Long? = null
    ): ResponseEntity<Response> {
        if (count <= 0) {
            return builder.badRequest().message("Count must be greater than 0").build()
        }

        val startOctets = parseIpv4Octets(startIp) ?: return builder.badRequest().message("Invalid start IPv4 address").build()
        if (parseIpv4Octets(gateway) == null) {
            return builder.badRequest().message("Invalid IPv4 gateway").build()
        }
        if (!isValidNetmask(netmask)) {
            return builder.badRequest().message("Invalid IPv4 netmask").build()
        }

        val lastOctet = startOctets[3]
        if (lastOctet + count - 1 > 255) {
            return builder.badRequest().message("The requested IPv4 range exceeds the current /24 boundary").build()
        }

        val natTemplate = IPv4().apply {
            this.gateway = gateway
            this.netmask = netmask
            this.isNatIp = isNatIp
            this.externalIpAddress = externalIpAddress
            this.remotePort = remotePort
            this.portRangeStart = portRangeStart
            this.portRangeEnd = portRangeEnd
        }
        val natValidationError = validateIpv4Payload(natTemplate)
        if (natValidationError != null) {
            return builder.badRequest().message(natValidationError).build()
        }

        val ipv4List = mutableListOf<IPv4>()
        var currentOctet = lastOctet

        repeat(count) {
            val ipAddress = "${startOctets[0]}.${startOctets[1]}.${startOctets[2]}.$currentOctet"
            val ipv4 = IPv4()
            ipv4.nodeId = nodeId
            ipv4.ipAddress = ipAddress
            ipv4.gateway = gateway
            ipv4.netmask = netmask
            ipv4.isAllocated = false
            ipv4.isNatIp = isNatIp
            ipv4.externalIpAddress = externalIpAddress
            ipv4.remotePort = remotePort
            ipv4.portRangeStart = portRangeStart
            ipv4.portRangeEnd = portRangeEnd
            ipv4.ipGroupId = ipGroupId
            normalizeNatFields(ipv4)
            ipv4List.add(ipv4Service.create(ipv4))
            currentOctet++
        }
        return builder.ok().data(ipv4List).build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody ipv4: IPv4
    ): ResponseEntity<Response> {
        ipv4.id = id
        val validationError = validateIpv4Payload(ipv4)
        if (validationError != null) {
            return builder.badRequest().message(validationError).build()
        }
        normalizeNatFields(ipv4)
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
        return builder.ok().data(ipv4).build()
    }

    @GetMapping("/list/{nodeId}/available")
    fun findAvailable(@PathVariable nodeId: Long): ResponseEntity<Response> {
        val ipv4 = ipv4Service.findAvailableByNodeId(nodeId)
        return builder.ok().data(ipv4).build()
    }

    private fun parseIpv4Octets(address: String): List<Int>? {
        return try {
            val bytes = InetAddress.getByName(address).address
            if (bytes.size != 4) {
                null
            } else {
                bytes.map { it.toInt() and 0xFF }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun isValidNetmask(netmask: String): Boolean {
        return netmask.toIntOrNull()?.let { it in 0..32 } == true || parseIpv4Octets(netmask) != null
    }

    private fun validateIpv4Payload(ipv4: IPv4): String? {
        val gateway = ipv4.gateway ?: return "IPv4 gateway is required"
        val netmask = ipv4.netmask ?: return "IPv4 netmask is required"
        if (parseIpv4Octets(gateway) == null) {
            return "Invalid IPv4 gateway"
        }
        if (!isValidNetmask(netmask)) {
            return "Invalid IPv4 netmask"
        }

        if (ipv4.ipAddress != null && parseIpv4Octets(ipv4.ipAddress!!) == null) {
            return "Invalid IPv4 address"
        }

        if (ipv4.isNatIp == true) {
            if (parseIpv4Octets(ipv4.externalIpAddress ?: "") == null) {
                return "External IPv4 address is required for NAT IPs"
            }
            val remotePort = ipv4.remotePort ?: return "Remote port is required for NAT IPs"
            if (remotePort !in 1..65535) {
                return "Remote port must be between 1 and 65535"
            }
            val portStart = ipv4.portRangeStart ?: return "Port range start is required for NAT IPs"
            val portEnd = ipv4.portRangeEnd ?: return "Port range end is required for NAT IPs"
            if (portStart !in 1..65535 || portEnd !in 1..65535 || portStart > portEnd) {
                return "Invalid NAT port range"
            }
        }

        return null
    }

    private fun normalizeNatFields(ipv4: IPv4) {
        if (ipv4.isNatIp != true) {
            ipv4.externalIpAddress = null
            ipv4.remotePort = null
            ipv4.portRangeStart = null
            ipv4.portRangeEnd = null
        }
    }
}
