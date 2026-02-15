package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.MonitoringAlert
import cn.locyan.pvecontroller.service.jdbc.MonitoringAlertService
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/alerts")
class MonitoringAlertController(
    private val alertService: MonitoringAlertService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody alert: MonitoringAlert): ResponseEntity<Response> {
        return try {
            val created = alertService.create(alert)
            builder.ok().data(created).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to create alert").build()
        }
    }

    @GetMapping("/dc")
    fun findByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        return try {
            val alerts = alertService.findByDcId(dcId)
            builder.ok().data(alerts).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve alerts").build()
        }
    }

    @GetMapping("/unresolved")
    fun findUnresolved(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        return try {
            val alerts = alertService.findUnresolvedByDcId(dcId)
            builder.ok().data(alerts).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve unresolved alerts").build()
        }
    }

    @GetMapping("/resource-type")
    fun findByResourceType(@RequestParam("resource_type") resourceType: String): ResponseEntity<Response> {
        return try {
            val alerts = alertService.findByResourceType(resourceType)
            builder.ok().data(alerts).build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to retrieve alerts").build()
        }
    }

    @PostMapping("/{id}/resolve")
    fun markAsResolved(@PathVariable id: Long): ResponseEntity<Response> {
        return try {
            val alert = alertService.markAsResolved(id)
            builder.ok().data(alert).message("Alert marked as resolved").build()
        } catch (e: Exception) {
            builder.exception().message(e.message ?: "Failed to resolve alert").build()
        }
    }
}
