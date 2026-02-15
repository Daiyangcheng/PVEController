package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.AuditLog
import cn.locyan.pvecontroller.service.jdbc.AuditLogService
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/audit-logs")
class AuditLogController(
    private val auditLogService: AuditLogService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody log: AuditLog): ResponseEntity<Response> {
        val created = auditLogService.create(log)
        return builder.ok().data(created).build()
    }

    @GetMapping("/user")
    fun findByUserId(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val logs = auditLogService.findByUserId(userId)
        return builder.ok().data(logs).build()
    }

    @GetMapping("/resource-type")
    fun findByResourceType(@RequestParam("resource_type") resourceType: String): ResponseEntity<Response> {
        val logs = auditLogService.findByResourceType(resourceType)
        return builder.ok().data(logs).build()
    }

    @GetMapping("/dc")
    fun findByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val logs = auditLogService.findByDcId(dcId)
        return builder.ok().data(logs).build()
    }
}
