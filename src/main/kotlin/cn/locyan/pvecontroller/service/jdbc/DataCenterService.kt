package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.DataCenter
import org.springframework.stereotype.Service

interface DataCenterService {
    fun create(dataCenter: DataCenter): DataCenter
    fun update(dataCenter: DataCenter): DataCenter
    fun delete(dataCenter: DataCenter)
    fun findById(id: Long): DataCenter?
    fun findAll(): List<DataCenter>
    fun findByName(name: String): DataCenter?
}
