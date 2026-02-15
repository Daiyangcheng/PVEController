package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Node
import org.springframework.stereotype.Service

@Service
interface NodeService {
    fun update(node: Node)
    fun delete(node: Node)
    fun findAll(): MutableList<Node>
    fun findById(id: Long): Node?
    fun findByName(name: String): Node?
    fun findByNameAndDcId(name: String, dcId: Long): Node?
}