package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.MonitoringAlert
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MonitoringAlertRepository : JpaRepository<MonitoringAlert, Long> {
    fun findByDcIdOrderByCreatedTimeDesc(dcId: Long): List<MonitoringAlert>
    fun findByDcIdAndIsResolvedFalseOrderByCreatedTimeDesc(dcId: Long): List<MonitoringAlert>
    fun findByResourceTypeOrderByCreatedTimeDesc(resourceType: String): List<MonitoringAlert>
}
