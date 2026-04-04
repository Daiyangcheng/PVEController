package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSTemplate
import cn.locyan.pvecontroller.repository.RouterOSTemplateRepository
import org.springframework.stereotype.Service

@Service
class RouterOSTemplateServiceImpl(
    private val routerOSTemplateRepository: RouterOSTemplateRepository
) : RouterOSTemplateService {
    override fun update(routerOSTemplate: RouterOSTemplate) {
        routerOSTemplateRepository.save(routerOSTemplate)
    }

    override fun delete(routerOSTemplate: RouterOSTemplate) {
        routerOSTemplateRepository.delete(routerOSTemplate)
    }

    override fun findAll(): List<RouterOSTemplate> {
        return routerOSTemplateRepository.findAll()
    }

    override fun findById(id: Long): RouterOSTemplate? {
        return routerOSTemplateRepository.findById(id).orElse(null)
    }

    override fun findByNodeId(nodeId: Long): List<RouterOSTemplate> {
        return routerOSTemplateRepository.findByNodeId(nodeId)
    }

    override fun findByApiId(apiId: Long): List<RouterOSTemplate> {
        return routerOSTemplateRepository.findByApiId(apiId)
    }
}
