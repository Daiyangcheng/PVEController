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

/*
* 全局 {id} 为 apiId
*/
@RestController
@RequestMapping("/ros-api")
class RouterOSController(
    private val routerOSApiService: RouterOSApiService,
    private val routerOSTemplateService: RouterOSTemplateService,
    private var routerOSRulesService: RouterOSRulesService,
    private val builder: ResponseBuilder
) {

    // 获取 API 列表
    @GetMapping()
    fun getApis(): ResponseEntity<Response>{
        val ros = routerOSApiService.findAll()
        return builder.ok().data(ros).build()
    }

    // 增加 API
    @PostMapping()
    fun addRosApi(
        @RequestParam(value = "name") name: String,
        @RequestParam(value = "host") host: String,
        @RequestParam(value = "port") port: Int,
        @RequestParam(value = "ssl") ssl: Boolean,
        @RequestParam(value = "user") user: String,
        @RequestParam(value = "password") password: String,
    ): ResponseEntity<Response>{

        val status = routerOSApiService.testConn(
            host = host,
            user = user,
            ssl = ssl,
            password = password,
            port = port
        )

        if (!status) return builder.exception().message("无法连接至 RouterOS，请检查参数").build()

        val ros = RouterOSApi()
        ros.apply {
            this.name = name
            this.host = host
            this.port = port
            this.ssl = ssl
            this.user = user
            this.password = password
        }
        routerOSApiService.update(ros)
        return builder.ok().build()
    }

    // 删除 API
    @DeleteMapping("/{id}")
    fun deleteApi(
        @PathVariable id: Long
    ): ResponseEntity<Response>{
        val ros = routerOSApiService.findById(id) ?: return builder.notFound().message("找不到 ID 为: $id 的 RouterOSApi").build()
        routerOSApiService.delete(ros)
        return builder.ok().build()
    }

    // 执行模板 - 添加
    @PostMapping("/{id}")
    fun action(
        @PathVariable id: Long,
        @RequestParam(value = "template_id") templateId: Long,
        @RequestParam(value = "params") params: String
    ): ResponseEntity<Response> {
        val template = routerOSTemplateService.findById(templateId)
            ?: return builder.exception().message("模板不存在").build()

        val templateContent = template.template ?: run {
            return builder.exception().message("模板内容为空").build()
        }
//
        val engine = TemplateEngine()
        val rendered = try {
            engine.render(templateContent, params)
        } catch (e: TemplateException) {
            return builder.exception().message("模板渲染失败: ${e.message}").build()
        }

        val data = routerOSApiService.execute(template.apiId!!, template.path!!, template.action!!, rendered)
            ?: return builder.exception().message("RouterOS 命令执行失败，请检查互联网连接").build()

        val ruleId = data[0]["ret"]
        val rule = RouterOSRules()
        rule.apply {
            this.templateId = templateId
            this.ruleId = ruleId
            this.params = params
            this.apiId = template.apiId
        }
        routerOSRulesService.update(rule)
        return builder.ok().data(rule).build()
    }

    // 获取所有规则
    @GetMapping("/{id}/rules")
    fun getRuleList(
        @PathVariable id: Long,
    ): ResponseEntity<Response> {
        val rules = routerOSRulesService.findByApiId(id)
        return builder.ok().data(rules).build()
    }

    // 执行模板 - 删除
    @DeleteMapping("/{id}/rules")
    fun deleteAction(
        @PathVariable id: Long,
        @RequestParam(value = "template_id") templateId: Long,
        @RequestParam(value = "rule_id") ruleId: Long
    ): ResponseEntity<Response> {
        val template = routerOSTemplateService.findById(templateId)
            ?: return builder.exception().message("模板不存在").build()

        val rule = routerOSRulesService.findById(ruleId) ?: return builder.exception().message("规则不存在").build()
        if (rule.templateId != templateId) return builder.exception().message("模板 ID 与该规则的 ID 不匹配").build()

        val data = routerOSApiService.execute(template.apiId!!, template.path!!, "remove", ".id=${rule.ruleId}")
            ?: return builder.exception().message("无法连接至 RouterOSApi，请检查网络连接").build()

        routerOSRulesService.delete(rule)
        return builder.ok().data(data).build()

    }

    /*
    * 创建模板
    * template example: ip=${ip},port=${port}
    * value example: {"ip":"1.1.1.1","port":"8888"}*/
    @PostMapping("/{id}/template")
    fun addTemplateList(
        @PathVariable id: Long,
        @RequestParam(value = "path") path: String,
        @RequestParam(value = "action") action: String,
        @RequestParam(value = "apiId") apiId: Long,
        @RequestParam(value = "nodeId") nodeId: Long,
        @RequestParam(value = "template") template: String,
        @RequestParam(value = "templateName") templateName: String,
        @RequestParam(value = "values") values: String
    ): ResponseEntity<Response>{
        val rosTemplate = RouterOSTemplate()
        rosTemplate.apply {
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

    // 获取模板
    @GetMapping("/{id}/template")
    fun getTemplateList(
        @PathVariable id: Long,
    ): ResponseEntity<Response>{
        val templateList = routerOSTemplateService.findByApiId(id)
        return builder.ok().data(templateList).build()
    }

    @DeleteMapping("/{id}/template")
    fun deleteTemplate(
        @PathVariable id: Long,
        @RequestParam(value = "template_id") templateId: Long
    ): ResponseEntity<Response>{
        val template = routerOSTemplateService.findById(templateId) ?: return builder.notFound().message("找不到该模板").build()
        if (template.apiId != id) return builder.exception().message("模板与ROS实例不匹配").build()
        val rules = routerOSRulesService.findByTemplateId(templateId)
        if (!rules.isEmpty()) return builder.forbidden().message("依旧有规则在使用该模板，请清理后删除模板").build()
        routerOSTemplateService.delete(template)
        return builder.ok().build()
    }
}