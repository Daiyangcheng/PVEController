package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Node
import cn.locyan.pvecontroller.repository.NodeRepository
import org.springframework.stereotype.Service

@Service
class NodeServiceImpl(
    private val nodeRepository: NodeRepository,
) : NodeService {
    override fun update(node: Node) {
        nodeRepository.save(node)
    }

    override fun delete(node: Node) {
        nodeRepository.delete(node)
    }

    override fun findAll(): MutableList<Node> {
        return nodeRepository.findAll()
    }

    override fun findById(id: Long): Node? {
        return nodeRepository.findById(id).orElse(null)
    }

    override fun findByName(name: String): Node? {
        return nodeRepository.findByName(name)
    }

    override fun findByNameAndDcId(
        name: String,
        dcId: Long
    ): Node? {
        return nodeRepository.findByNameAndDcId(name, dcId)
    }
}