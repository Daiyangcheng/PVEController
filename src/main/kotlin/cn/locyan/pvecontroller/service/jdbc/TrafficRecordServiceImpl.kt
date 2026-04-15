package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.TrafficRecord
import cn.locyan.pvecontroller.repository.TrafficRecordRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TrafficRecordServiceImpl(
    private val trafficRecordRepository: TrafficRecordRepository
) : TrafficRecordService {

    override fun createOrUpdate(serverId: Long, uploadBytes: Long, downloadBytes: Long): TrafficRecord {
        val record = trafficRecordRepository.findByServerId(serverId) ?: TrafficRecord().apply {
            this.serverId = serverId
        }
        record.uploadBytes = uploadBytes
        record.downloadBytes = downloadBytes
        record.updatedAt = LocalDateTime.now()
        return trafficRecordRepository.save(record)
    }

    override fun findByServerId(serverId: Long): TrafficRecord? {
        return trafficRecordRepository.findByServerId(serverId)
    }
}
