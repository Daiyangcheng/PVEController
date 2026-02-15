package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.BillingRecord
import cn.locyan.pvecontroller.service.jdbc.BillingService
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
@RequestMapping("/billing")
class BillingController(
    private val billingService: BillingService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody record: BillingRecord): ResponseEntity<Response> {
        val created = billingService.create(record)
        return builder.ok().data(created).build()
    }

    @GetMapping("/user")
    fun findByUserId(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val records = billingService.findByUserId(userId)
        return builder.ok().data(records).build()
    }

    @GetMapping("/dc")
    fun findByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val records = billingService.findByDcId(dcId)
        return builder.ok().data(records).build()
    }

    @GetMapping("/unpaid")
    fun findUnpaid(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val records = billingService.findUnpaid(userId)
        return builder.ok().data(records).build()
    }

    @GetMapping("/unpaid-total")
    fun getUnpaidTotal(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val total = billingService.calculateTotalUnpaid(userId)
        return builder.ok().data(mapOf("userId" to userId, "totalUnpaid" to total)).build()
    }

    @PostMapping("/{id}/pay")
    fun markAsPaid(@PathVariable id: Long): ResponseEntity<Response> {
        val record = billingService.markAsPaid(id)
        return builder.ok().data(record).message("Billing record marked as paid").build()
    }
}
