package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.NodeGroup

interface NodeGroupService {
    fun create(nodeGroup: NodeGroup): NodeGroup
    fun update(nodeGroup: NodeGroup): NodeGroup
    fun delete(id: Long)
    fun findById(id: Long): NodeGroup?
    fun findAllByDcId(dcId: Long): List<NodeGroup>
}
