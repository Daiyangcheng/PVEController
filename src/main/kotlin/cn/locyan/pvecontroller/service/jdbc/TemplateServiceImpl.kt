package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Template
import cn.locyan.pvecontroller.repository.TemplateRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TemplateServiceImpl(
    private val templateRepository: TemplateRepository
) : TemplateService {
    
    override fun create(template: Template): Template {
        template.createdTime = LocalDateTime.now()
        template.updatedTime = LocalDateTime.now()
        return templateRepository.save(template)
    }

    override fun update(template: Template): Template {
        template.updatedTime = LocalDateTime.now()
        return templateRepository.save(template)
    }

    override fun delete(id: Long) {
        templateRepository.deleteById(id)
    }

    override fun findById(id: Long): Template? {
        return templateRepository.findById(id).orElse(null)
    }

    override fun findByTemplateIdAndDcId(
        templateId: Long,
        dcId: Long
    ): Template? {
        return templateRepository.findByTemplateIdAndDcId(templateId, dcId).orElse(null)
    }

    override fun findAllByDcId(dcId: Long): List<Template> {
        return templateRepository.findAllByDcId(dcId)
    }

    override fun findByTemplateGroupId(groupId: Long): List<Template> {
        return templateRepository.findByTemplateGroupId(groupId)
    }
}
