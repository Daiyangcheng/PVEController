package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.NodeGroup
import cn.locyan.pvecontroller.repository.NodeGroupRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NodeGroupServiceImpl(
    private val nodeGroupRepository: NodeGroupRepository
) : NodeGroupService {

    override fun create(nodeGroup: NodeGroup): NodeGroup {
        nodeGroup.createdTime = LocalDateTime.now()
        nodeGroup.updatedTime = LocalDateTime.now()
        return nodeGroupRepository.save(nodeGroup)
    }

    override fun update(nodeGroup: NodeGroup): NodeGroup {
        nodeGroup.updatedTime = LocalDateTime.now()
        return nodeGroupRepository.save(nodeGroup)
    }

    override fun delete(id: Long) {
        nodeGroupRepository.deleteById(id)
    }

    override fun findById(id: Long): NodeGroup? {
        return nodeGroupRepository.findById(id).orElse(null)
    }

    override fun findAllByDcId(dcId: Long): List<NodeGroup> {
        return nodeGroupRepository.findAllByDcId(dcId)
    }
}
