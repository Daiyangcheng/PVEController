package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.IpGroup
import cn.locyan.pvecontroller.service.jdbc.IpGroupService
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
@RequestMapping("/ip-groups")
class IpGroupController(
    private val ipGroupService: IpGroupService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody ipGroup: IpGroup): ResponseEntity<Response> {
        val created = ipGroupService.create(ipGroup)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody ipGroup: IpGroup): ResponseEntity<Response> {
        ipGroup.id = id
        val updated = ipGroupService.update(ipGroup)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        ipGroupService.delete(id)
        return builder.ok().message("IP group deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val ipGroup = ipGroupService.findById(id)
        if (ipGroup != null) {
            return builder.ok().data(ipGroup).build()
        } else {
            return builder.exception().message("IP group not found").build()
        }
    }

    @GetMapping
    fun findAllByNodeId(@RequestParam("node_id") nodeId: Long): ResponseEntity<Response> {
        val ipGroups = ipGroupService.findAllByNodeId(nodeId)
        return builder.ok().data(ipGroups).build()
    }
}
