package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.AuditLog
import org.springframework.stereotype.Service

@Service
interface AuditLogService {
    fun create(log: AuditLog): AuditLog
    fun findByUserId(userId: Long): List<AuditLog>
    fun findByResourceType(resourceType: String): List<AuditLog>
    fun findByDcId(dcId: Long): List<AuditLog>
}
