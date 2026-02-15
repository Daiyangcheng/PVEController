package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.BillingRecord
import cn.locyan.pvecontroller.repository.BillingRecordRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class BillingServiceImpl(
    private val billingRecordRepository: BillingRecordRepository
) : BillingService {
    
    override fun create(record: BillingRecord): BillingRecord {
        record.createdTime = LocalDateTime.now()
        record.updatedTime = LocalDateTime.now()
        return billingRecordRepository.save(record)
    }

    override fun update(record: BillingRecord): BillingRecord {
        record.updatedTime = LocalDateTime.now()
        return billingRecordRepository.save(record)
    }

    override fun findByUserId(userId: Long): List<BillingRecord> {
        return billingRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId)
    }

    override fun findByDcId(dcId: Long): List<BillingRecord> {
        return billingRecordRepository.findByDcIdOrderByCreatedTimeDesc(dcId)
    }

    override fun findUnpaid(userId: Long): List<BillingRecord> {
        return billingRecordRepository.findByUserIdAndIsPaidFalseOrderByCreatedTimeDesc(userId)
    }

    override fun calculateTotalUnpaid(userId: Long): BigDecimal {
        val unpaidRecords = findUnpaid(userId)
        return unpaidRecords.mapNotNull { it.amount }.fold(BigDecimal.ZERO) { acc, value -> acc + value }
    }

    override fun markAsPaid(recordId: Long): BillingRecord {
        val record = billingRecordRepository.findById(recordId).orElse(null)
        return if (record != null) {
            record.isPaid = true
            record.paidTime = LocalDateTime.now()
            update(record)
        } else {
            throw Exception("Billing record not found")
        }
    }
}
