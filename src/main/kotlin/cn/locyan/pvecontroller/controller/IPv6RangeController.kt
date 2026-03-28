package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.IPv6Range
import cn.locyan.pvecontroller.service.jdbc.IPv6RangeService
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
@RequestMapping("/ipv6-ranges")
class IPv6RangeController(
    private val ipv6RangeService: IPv6RangeService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(
        @RequestParam("node_id") nodeId: Long? = null,
        @RequestParam("start_address") startAddress: String? = null,
        @RequestParam("end_address") endAddress: String? = null,
        @RequestParam("gateway") gateway: String? = null,
        @RequestParam("prefix_length") prefixLength: Int? = null,
    ): ResponseEntity<Response> {
        val range = IPv6Range()
        range.apply {
            this.id = null
            this.nodeId = nodeId
            this.startAddress = startAddress
            this.endAddress = endAddress
            this.gateway = gateway
            this.prefixLength = prefixLength
        }
        val created = ipv6RangeService.create(range)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody range: IPv6Range
    ): ResponseEntity<Response> {
        return try {
            range.id = id
            val updated = ipv6RangeService.update(range)
            builder.ok().data(updated).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to update IPv6 range").build()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        return try {
            ipv6RangeService.delete(id)
            builder.ok().message("IPv6 range deleted successfully").build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to delete IPv6 range").build()
        }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        return try {
            val range = ipv6RangeService.findById(id)
            if (range != null) {
                builder.ok().data(range).build()
            } else {
                builder.exception().message("IPv6 range not found").build()
            }
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve IPv6 range").build()
        }
    }

    @GetMapping
    fun findAllByDcId(@RequestParam("node_id") nodeId: Long): ResponseEntity<Response> {
        return try {
            val ranges = ipv6RangeService.findAllByNodeId(nodeId)
            builder.ok().data(ranges).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve IPv6 ranges").build()
        }
    }

    @GetMapping("/active")
    fun findActiveByDcId(@RequestParam("node_id") nodeId: Long): ResponseEntity<Response> {
        return try {
            val ranges = ipv6RangeService.findActiveByNodeId(nodeId)
            builder.ok().data(ranges).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve active IPv6 ranges").build()
        }
    }
}
