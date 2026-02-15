package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findByUserIdOrderByCreatedTimeDesc(userId: Long): List<AuditLog>
    fun findByResourceTypeOrderByCreatedTimeDesc(resourceType: String): List<AuditLog>
    fun findByDcIdOrderByCreatedTimeDesc(dcId: Long): List<AuditLog>
}
