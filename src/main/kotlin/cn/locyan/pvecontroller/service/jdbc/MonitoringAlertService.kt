package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.MonitoringAlert

interface MonitoringAlertService {
    fun create(alert: MonitoringAlert): MonitoringAlert
    fun findByDcId(dcId: Long): List<MonitoringAlert>
    fun findUnresolvedByDcId(dcId: Long): List<MonitoringAlert>
    fun findByResourceType(resourceType: String): List<MonitoringAlert>
    fun markAsResolved(alertId: Long): MonitoringAlert
}
