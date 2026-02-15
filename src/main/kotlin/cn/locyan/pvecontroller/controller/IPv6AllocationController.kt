package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.IPv6Allocation
import cn.locyan.pvecontroller.service.jdbc.IPv6AllocationService
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
@RequestMapping("/ipv6-allocations")
class IPv6AllocationController(
    private val ipv6AllocationService: IPv6AllocationService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody allocation: IPv6Allocation): ResponseEntity<Response> {
        return try {
            val created = ipv6AllocationService.create(allocation)
            builder.ok().data(created).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to create IPv6 allocation").build()
        }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody allocation: IPv6Allocation): ResponseEntity<Response> {
        return try {
            allocation.id = id
            val updated = ipv6AllocationService.update(allocation)
            builder.ok().data(updated).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to update IPv6 allocation").build()
        }
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        return try {
            ipv6AllocationService.delete(id)
            builder.ok().message("IPv6 allocation deleted successfully").build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to delete IPv6 allocation").build()
        }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        return try {
            val allocation = ipv6AllocationService.findById(id)
            if (allocation != null) {
                builder.ok().data(allocation).build()
            } else {
                builder.exception().message("IPv6 allocation not found").build()
            }
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve IPv6 allocation").build()
        }
    }

    @GetMapping("/range")
    fun findByRangeId(@RequestParam("range_id") rangeId: Long): ResponseEntity<Response> {
        return try {
            val allocations = ipv6AllocationService.findAllByRangeId(rangeId)
            builder.ok().data(allocations).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve IPv6 allocations").build()
        }
    }

    @GetMapping("/server")
    fun findByServerId(@RequestParam("server_id") serverId: Long): ResponseEntity<Response> {
        val allocation = ipv6AllocationService.findByServerId(serverId)
        if (allocation != null) {
            return builder.ok().data(allocation).build()
        } else {
            return builder.exception().message("IPv6 allocation not found for server").build()
        }
    }

    @PostMapping("/allocate")
    fun allocate(
        @RequestParam("range_id") rangeId: Long,
        @RequestParam(value = "method", required = false, defaultValue = "sequential") method: String = "sequential"
    ): ResponseEntity<Response> {
        val allocation = ipv6AllocationService.allocateIPv6(rangeId, method)
        if (allocation != null) {
            return builder.ok().data(allocation).message("IPv6 allocated successfully").build()
        } else {
            return builder.exception().message("Failed to allocate IPv6 address").build()
        }
    }

    @PostMapping("/{id}/deallocate")
    fun deallocate(@PathVariable id: Long): ResponseEntity<Response> {
        ipv6AllocationService.deallocateIPv6(id)
        return builder.ok().message("IPv6 deallocated successfully").build()
    }
}
