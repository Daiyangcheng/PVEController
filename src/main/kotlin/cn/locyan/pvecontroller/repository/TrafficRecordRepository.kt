package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.TrafficRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrafficRecordRepository : JpaRepository<TrafficRecord, Long> {
    fun findByServerId(serverId: Long): TrafficRecord?
}
