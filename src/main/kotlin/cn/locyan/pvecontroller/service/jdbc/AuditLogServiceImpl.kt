package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.AuditLog
import cn.locyan.pvecontroller.repository.AuditLogRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuditLogServiceImpl(
    private val auditLogRepository: AuditLogRepository
) : AuditLogService {
    
    override fun create(log: AuditLog): AuditLog {
        log.createdTime = LocalDateTime.now()
        return auditLogRepository.save(log)
    }

    override fun findByUserId(userId: Long): List<AuditLog> {
        return auditLogRepository.findByUserIdOrderByCreatedTimeDesc(userId)
    }

    override fun findByResourceType(resourceType: String): List<AuditLog> {
        return auditLogRepository.findByResourceTypeOrderByCreatedTimeDesc(resourceType)
    }

    override fun findByDcId(dcId: Long): List<AuditLog> {
        return auditLogRepository.findByDcIdOrderByCreatedTimeDesc(dcId)
    }
}
