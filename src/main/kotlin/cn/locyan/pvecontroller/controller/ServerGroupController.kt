package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.ServerGroup
import cn.locyan.pvecontroller.service.jdbc.ServerGroupService
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
@RequestMapping("/server-groups")
class ServerGroupController(
    private val serverGroupService: ServerGroupService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody serverGroup: ServerGroup): ResponseEntity<Response> {
        val created = serverGroupService.create(serverGroup)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody serverGroup: ServerGroup): ResponseEntity<Response> {
        serverGroup.id = id
        val updated = serverGroupService.update(serverGroup)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        serverGroupService.delete(id)
        return builder.ok().message("Server group deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val serverGroup = serverGroupService.findById(id)
        if (serverGroup != null) {
            return builder.ok().data(serverGroup).build()
        } else {
            return builder.exception().message("Server group not found").build()
        }
    }

    @GetMapping
    fun findAllByDcId(@RequestParam("dc_id") dcId: Long): ResponseEntity<Response> {
        val serverGroups = serverGroupService.findAllByDcId(dcId)
        return builder.ok().data(serverGroups).build()
    }

    @GetMapping("/user")
    fun findByUserId(@RequestParam("user_id") userId: Long): ResponseEntity<Response> {
        val serverGroups = serverGroupService.findByUserId(userId)
        return builder.ok().data(serverGroups).build()
    }
}
