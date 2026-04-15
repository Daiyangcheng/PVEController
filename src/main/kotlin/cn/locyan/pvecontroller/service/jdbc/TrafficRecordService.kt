package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.TrafficRecord

interface TrafficRecordService {
    fun createOrUpdate(serverId: Long, uploadBytes: Long, downloadBytes: Long): TrafficRecord
    fun findByServerId(serverId: Long): TrafficRecord?
}
