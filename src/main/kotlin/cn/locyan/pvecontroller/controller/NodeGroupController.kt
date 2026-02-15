package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.NodeGroup
import cn.locyan.pvecontroller.service.jdbc.NodeGroupService
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
@RequestMapping("/node-groups")
class NodeGroupController(
    private val nodeGroupService: NodeGroupService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody nodeGroup: NodeGroup): ResponseEntity<Response> {
        val created = nodeGroupService.create(nodeGroup)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody nodeGroup: NodeGroup): ResponseEntity<Response> {
        nodeGroup.id = id
        val updated = nodeGroupService.update(nodeGroup)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        nodeGroupService.delete(id)
        return builder.ok().message("Node group deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val nodeGroup = nodeGroupService.findById(id)
        if (nodeGroup != null) {
            return builder.ok().data(nodeGroup).build()
        } else {
            return builder.exception().message("Node group not found").build()
        }
    }

    @GetMapping
    fun findAllByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val nodeGroups = nodeGroupService.findAllByDcId(dcId)
        return builder.ok().data(nodeGroups).build()
    }
}
