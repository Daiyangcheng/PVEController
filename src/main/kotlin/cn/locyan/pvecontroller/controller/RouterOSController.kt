package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.RouterOSApi
import cn.locyan.pvecontroller.model.RouterOSRules
import cn.locyan.pvecontroller.model.RouterOSTemplate
import cn.locyan.pvecontroller.service.jdbc.RouterOSApiService
import cn.locyan.pvecontroller.service.jdbc.RouterOSRulesService
import cn.locyan.pvecontroller.service.jdbc.RouterOSTemplateService
import cn.locyan.pvecontroller.shared.TemplateEngine
import cn.locyan.pvecontroller.shared.TemplateException
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ros-api")
class RouterOSController(
    private val routerOSApiService: RouterOSApiService,
    private val routerOSTemplateService: RouterOSTemplateService,
    private val routerOSRulesService: RouterOSRulesService,
    private val builder: ResponseBuilder
) {

    @GetMapping
    fun getApis(): ResponseEntity<Response> {
        return builder.ok().data(routerOSApiService.findAll()).build()
    }

    @PostMapping
    fun addRosApi(
        @RequestParam("name") name: String,
        @RequestParam("host") host: String,
        @RequestParam("port") port: Int,
        @RequestParam("ssl") ssl: Boolean,
        @RequestParam("user") user: String,
        @RequestParam("password") password: String,
    ): ResponseEntity<Response> {
        val status = routerOSApiService.testConn(
            host = host,
            user = user,
            ssl = ssl,
            password = password,
            port = port
        )

        if (!status) {
            return builder.exception().message("Unable to connect to RouterOS, please verify the connection settings").build()
        }

        routerOSApiService.update(
            RouterOSApi().apply {
                this.name = name
                this.host = host
                this.port = port
                this.ssl = ssl
                this.user = user
                this.password = password
            }
        )
        return builder.ok().build()
    }

    @DeleteMapping("/{id}")
    fun deleteApi(@PathVariable id: Long): ResponseEntity<Response> {
        val ros = routerOSApiService.findById(id)
            ?: return builder.notFound().message("RouterOS API not found: $id").build()
        routerOSApiService.delete(ros)
        return builder.ok().build()
    }

    @PostMapping("/{id}")
    fun action(
        @PathVariable id: Long,
        @RequestParam("template_id") templateId: Long,
        @RequestParam("params") params: String
    ): ResponseEntity<Response> {
        val template = routerOSTemplateService.findById(templateId)
            ?: return builder.exception().message("RouterOS template not found").build()
        if (template.apiId != id) {
            return builder.badRequest().message("RouterOS template does not belong to the selected API").build()
        }

        val templateContent = template.template
            ?: return builder.exception().message("RouterOS template content is empty").build()

        val rendered = try {
            TemplateEngine().render(templateContent, params)
        } catch (e: TemplateException) {
            return builder.exception().message("RouterOS template rendering failed: ${e.message}").build()
        }

        val data = routerOSApiService.execute(template.apiId!!, template.path!!, template.action!!, rendered)
            ?: return builder.exception().message("RouterOS command execution failed").build()

        val ruleId = data.firstOrNull()?.get("id")?.toLongOrNull() ?: return builder.ok().data(
            mapOf(
                "templateId" to templateId,
                "apiId" to template.apiId,
                "result" to data
            )
        ).message("RouterOS command executed").build()

        val rule = RouterOSRules().apply {
            this.templateId = templateId
            this.ruleId = ruleId.toString()
            this.params = params
            this.apiId = template.apiId
        }
        routerOSRulesService.update(rule)
        return builder.ok().data(rule).build()
    }

    @GetMapping("/{id}/rules")
    fun getRuleList(@PathVariable id: Long): ResponseEntity<Response> {
        return builder.ok().data(routerOSRulesService.findByApiId(id)).build()
    }

    @DeleteMapping("/{id}/rules")
    fun deleteAction(
        @PathVariable id: Long,
        @RequestParam("template_id") templateId: Long,
        @RequestParam("rule_id") ruleId: Long
    ): ResponseEntity<Response> {
        val template = routerOSTemplateService.findById(templateId)
            ?: return builder.exception().message("RouterOS template not found").build()
        if (template.apiId != id) {
            return builder.badRequest().message("RouterOS template does not belong to the selected API").build()
        }

        val rule = routerOSRulesService.findById(ruleId)
            ?: return builder.exception().message("RouterOS rule not found").build()
        if (rule.templateId != templateId) {
            return builder.exception().message("RouterOS template ID does not match the selected rule").build()
        }

        val data = routerOSApiService.execute(template.apiId!!, template.path!!, "remove", "number=${rule.ruleId}")
            ?: return builder.exception().message("Unable to connect to RouterOS").build()

        routerOSRulesService.delete(rule)
        return builder.ok().data(data).build()
    }

    @PostMapping("/{id}/template")
    fun addTemplateList(
        @PathVariable id: Long,
        @RequestParam("path") path: String,
        @RequestParam("action") action: String,
        @RequestParam("apiId") apiId: Long,
        @RequestParam("nodeId") nodeId: Long,
        @RequestParam("template") template: String,
        @RequestParam("templateName") templateName: String,
        @RequestParam("values") values: String
    ): ResponseEntity<Response> {
        val rosTemplate = RouterOSTemplate().apply {
            this.apiId = apiId
            this.nodeId = nodeId
            this.templateName = templateName
            this.values = values
            this.template = template
            this.action = action
            this.path = path
        }
        routerOSTemplateService.update(rosTemplate)
        return builder.ok().build()
    }

    @GetMapping("/templates")
    fun getTemplatesByNode(@RequestParam("node_id") nodeId: Long): ResponseEntity<Response> {
        return builder.ok().data(routerOSTemplateService.findByNodeId(nodeId)).build()
    }

    @GetMapping("/{id}/template")
    fun getTemplateList(@PathVariable id: Long): ResponseEntity<Response> {
        return builder.ok().data(routerOSTemplateService.findByApiId(id)).build()
    }

    @DeleteMapping("/{id}/template")
    fun deleteTemplate(
        @PathVariable id: Long,
        @RequestParam("template_id") templateId: Long
    ): ResponseEntity<Response> {
        val template = routerOSTemplateService.findById(templateId)
            ?: return builder.notFound().message("RouterOS template not found").build()
        if (template.apiId != id) {
            return builder.exception().message("RouterOS template does not belong to the selected API").build()
        }

        val rules = routerOSRulesService.findByTemplateId(templateId)
        if (rules.isNotEmpty()) {
            return builder.forbidden().message("Template is still referenced by RouterOS rules").build()
        }

        routerOSTemplateService.delete(template)
        return builder.ok().build()
    }
}
