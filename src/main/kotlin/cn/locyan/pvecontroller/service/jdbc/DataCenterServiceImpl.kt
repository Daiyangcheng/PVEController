package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.DataCenter
import cn.locyan.pvecontroller.repository.DataCenterRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DataCenterServiceImpl(
    private val dataCenterRepository: DataCenterRepository
) : DataCenterService {
    
    override fun create(dataCenter: DataCenter): DataCenter {
        return dataCenterRepository.save(dataCenter)
    }

    override fun update(dataCenter: DataCenter): DataCenter {
        dataCenter.updatedTime = LocalDateTime.now()
        return dataCenterRepository.save(dataCenter)
    }

    override fun delete(dataCenter: DataCenter) {
        dataCenterRepository.delete(dataCenter)
    }

    override fun findById(id: Long): DataCenter? {
        return dataCenterRepository.findById(id).orElse(null)
    }

    override fun findAll(): List<DataCenter> {
        return dataCenterRepository.findAll()
    }

    override fun findByName(name: String): DataCenter? {
        return dataCenterRepository.findByName(name)
    }
}
