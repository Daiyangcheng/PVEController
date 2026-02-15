package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.Storage
import org.springframework.data.jpa.repository.JpaRepository

interface StorageRepository : JpaRepository<Storage, Long> {
    fun findAllByDcId(dcId: Long): List<Storage>
    fun findByNodeNameAndDcId(nodeName: String, dcId: Long): List<Storage>
}
