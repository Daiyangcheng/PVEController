package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.TemplateGroup
import cn.locyan.pvecontroller.repository.TemplateGroupRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TemplateGroupServiceImpl(
    private val templateGroupRepository: TemplateGroupRepository
) : TemplateGroupService {

    override fun create(group: TemplateGroup): TemplateGroup {
        group.createdTime = LocalDateTime.now()
        group.updatedTime = LocalDateTime.now()
        return templateGroupRepository.save(group)
    }

    override fun update(group: TemplateGroup): TemplateGroup {
        group.updatedTime = LocalDateTime.now()
        return templateGroupRepository.save(group)
    }

    override fun delete(id: Long) {
        templateGroupRepository.deleteById(id)
    }

    override fun findById(id: Long): TemplateGroup? {
        return templateGroupRepository.findById(id).orElse(null)
    }

    override fun findAllByNodeId(nodeId: Long): List<TemplateGroup> {
        return templateGroupRepository.findAllByNodeId(nodeId)
    }

    override fun findByNameAndNodeId(name: String, nodeId: Long): TemplateGroup? {
        return templateGroupRepository.findByNameAndNodeId(name, nodeId)
    }
}
