package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.MonitoringAlert
import cn.locyan.pvecontroller.repository.MonitoringAlertRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MonitoringAlertServiceImpl(
    private val monitoringAlertRepository: MonitoringAlertRepository
) : MonitoringAlertService {
    
    override fun create(alert: MonitoringAlert): MonitoringAlert {
        alert.createdTime = LocalDateTime.now()
        alert.updatedTime = LocalDateTime.now()
        return monitoringAlertRepository.save(alert)
    }

    override fun findByDcId(dcId: Long): List<MonitoringAlert> {
        return monitoringAlertRepository.findByDcIdOrderByCreatedTimeDesc(dcId)
    }

    override fun findUnresolvedByDcId(dcId: Long): List<MonitoringAlert> {
        return monitoringAlertRepository.findByDcIdAndIsResolvedFalseOrderByCreatedTimeDesc(dcId)
    }

    override fun findByResourceType(resourceType: String): List<MonitoringAlert> {
        return monitoringAlertRepository.findByResourceTypeOrderByCreatedTimeDesc(resourceType)
    }

    override fun markAsResolved(alertId: Long): MonitoringAlert {
        val alert = monitoringAlertRepository.findById(alertId).orElse(null)
        return if (alert != null) {
            alert.isResolved = true
            alert.resolvedTime = LocalDateTime.now()
            alert.updatedTime = LocalDateTime.now()
            monitoringAlertRepository.save(alert)
        } else {
            throw Exception("Alert not found")
        }
    }
}
