package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.BillingRecord
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
interface BillingService {
    fun create(record: BillingRecord): BillingRecord
    fun update(record: BillingRecord): BillingRecord
    fun findByUserId(userId: Long): List<BillingRecord>
    fun findByDcId(dcId: Long): List<BillingRecord>
    fun findUnpaid(userId: Long): List<BillingRecord>
    fun calculateTotalUnpaid(userId: Long): BigDecimal
    fun markAsPaid(recordId: Long): BillingRecord
}
