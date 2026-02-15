package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Storage

interface StorageService {
    fun create(storage: Storage): Storage
    fun update(storage: Storage): Storage
    fun delete(id: Long)
    fun findById(id: Long): Storage?
    fun findAllByDcId(dcId: Long): List<Storage>
    fun findByNodeName(nodeName: String, dcId: Long): List<Storage>
}
