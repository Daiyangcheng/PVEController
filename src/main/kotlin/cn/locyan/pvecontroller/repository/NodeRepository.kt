package cn.locyan.pvecontroller.repository

import cn.locyan.pvecontroller.model.Node
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NodeRepository : JpaRepository<Node, Long> {
    fun findByName(name: String): Node?
    fun findByNameAndDcId(name: String, dcId: Long): Node?
}