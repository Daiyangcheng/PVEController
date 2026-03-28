package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Storage

interface StorageService {
    fun create(storage: Storage): Storage
    fun update(storage: Storage): Storage
    fun delete(id: Long)
    fun findById(id: Long): Storage?
    fun findAllByNodeId(nodeId: Long): List<Storage>
    fun findByNodeNameAndNodeId(nodeName: String, nodeId: Long): List<Storage>
}
