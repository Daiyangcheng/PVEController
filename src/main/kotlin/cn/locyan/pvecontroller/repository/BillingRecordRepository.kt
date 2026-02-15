package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.BillingRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BillingRecordRepository : JpaRepository<BillingRecord, Long> {
    fun findByUserIdOrderByCreatedTimeDesc(userId: Long): List<BillingRecord>
    fun findByDcIdOrderByCreatedTimeDesc(dcId: Long): List<BillingRecord>
    fun findByUserIdAndIsPaidFalseOrderByCreatedTimeDesc(userId: Long): List<BillingRecord>
}
