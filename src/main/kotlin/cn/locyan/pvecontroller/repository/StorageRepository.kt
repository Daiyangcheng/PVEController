package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.Storage
import org.springframework.data.jpa.repository.JpaRepository

interface StorageRepository : JpaRepository<Storage, Long> {
    fun findAllByNodeId(nodeId: Long): List<Storage>
    fun findByNodeNameAndNodeId(nodeName: String, nodeId: Long): List<Storage>
}
