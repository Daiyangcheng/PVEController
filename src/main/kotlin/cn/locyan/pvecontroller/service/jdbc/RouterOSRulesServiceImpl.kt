package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSRules
import cn.locyan.pvecontroller.repository.RouterOSRulesRepository
import org.springframework.stereotype.Service

@Service
class RouterOSRulesServiceImpl(
    private val routerOSRulesRepository: RouterOSRulesRepository
) : RouterOSRulesService {
    override fun update(routerOSRules: RouterOSRules) {
        routerOSRulesRepository.save(routerOSRules)
    }

    override fun delete(routerOSRules: RouterOSRules) {
        routerOSRulesRepository.delete(routerOSRules)
    }

    override fun findAll(): List<RouterOSRules> {
        return routerOSRulesRepository.findAll()
    }

    override fun findById(id: Long): RouterOSRules? {
        return routerOSRulesRepository.findById(id).orElse(null)
    }

    override fun findByTemplateId(templateId: Long): List<RouterOSRules> {
        return routerOSRulesRepository.findByTemplateId(templateId)
    }

    override fun findByApiId(apiId: Long): List<RouterOSRules> {
        return routerOSRulesRepository.findByApiId(apiId)
    }
}