package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.TemplateGroup
import cn.locyan.pvecontroller.repository.TemplateGroupRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TemplateGroupServiceImpl(
    private val templateGroupRepository: TemplateGroupRepository
) : TemplateGroupService {

    override fun create(templateGroup: TemplateGroup): TemplateGroup {
        templateGroup.createdTime = LocalDateTime.now()
        templateGroup.updatedTime = LocalDateTime.now()
        return templateGroupRepository.save(templateGroup)
    }

    override fun update(templateGroup: TemplateGroup): TemplateGroup {
        templateGroup.updatedTime = LocalDateTime.now()
        return templateGroupRepository.save(templateGroup)
    }

    override fun delete(id: Long) {
        templateGroupRepository.deleteById(id)
    }

    override fun findById(id: Long): TemplateGroup? {
        return templateGroupRepository.findById(id).orElse(null)
    }

    override fun findAllByDcId(dcId: Long): List<TemplateGroup> {
        return templateGroupRepository.findAllByDcId(dcId)
    }

    override fun findByNameAndDcId(name: String, dcId: Long): TemplateGroup? {
        return templateGroupRepository.findByNameAndDcId(name, dcId)
    }
}
