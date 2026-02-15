package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.TemplateGroup
import cn.locyan.pvecontroller.service.jdbc.TemplateGroupService
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
@RequestMapping("/template-groups")
class TemplateGroupController(
    private val templateGroupService: TemplateGroupService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody templateGroup: TemplateGroup): ResponseEntity<Response> {
        val created = templateGroupService.create(templateGroup)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody templateGroup: TemplateGroup): ResponseEntity<Response> {
        templateGroup.id = id
        val updated = templateGroupService.update(templateGroup)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        templateGroupService.delete(id)
        return builder.ok().message("Template group deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val templateGroup = templateGroupService.findById(id)
        if (templateGroup != null) {
            return builder.ok().data(templateGroup).build()
        } else {
            return builder.exception().message("Template group not found").build()
        }
    }

    @GetMapping
    fun findAllByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val templateGroups = templateGroupService.findAllByDcId(dcId)
        return builder.ok().data(templateGroups).build()
    }

    @GetMapping("/name")
    fun findByNameAndDcId(@RequestParam name: String, @RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val templateGroup = templateGroupService.findByNameAndDcId(name, dcId)
        if (templateGroup != null) {
            return builder.ok().data(templateGroup).build()
        } else {
            return builder.exception().message("Template group not found").build()
        }
    }
}
