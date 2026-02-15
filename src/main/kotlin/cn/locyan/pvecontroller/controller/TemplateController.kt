package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.Template
import cn.locyan.pvecontroller.service.jdbc.TemplateService
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/templates")
class TemplateController(
    private val templateService: TemplateService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody template: Template): ResponseEntity<Response> {
        val created = templateService.create(template)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody template: Template
    ): ResponseEntity<Response> {
        template.id = id
        val updated = templateService.update(template)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        templateService.delete(id)
        return builder.ok().message("Template deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val template = templateService.findById(id)
        if (template != null) {
            return builder.ok().data(template).build()
        } else {
            return builder.exception().message("Template not found").build()
        }
    }

    @GetMapping
    fun findByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val templates = templateService.findAllByDcId(dcId)
        return builder.ok().data(templates).build()
    }

    @GetMapping("/group/{groupId}")
    fun findByGroupId(@PathVariable groupId: Long): ResponseEntity<Response> {
        val templates = templateService.findByTemplateGroupId(groupId)
        return builder.ok().data(templates).build()
    }
}
