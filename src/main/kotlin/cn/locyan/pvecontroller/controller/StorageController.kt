package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.Storage
import cn.locyan.pvecontroller.service.jdbc.StorageService
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
@RequestMapping("/storage")
class StorageController(
    private val storageService: StorageService,
    private val builder: ResponseBuilder
) {

    @PostMapping
    fun create(@RequestBody storage: Storage): ResponseEntity<Response> {
        val created = storageService.create(storage)
        return builder.ok().data(created).build()
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody storage: Storage): ResponseEntity<Response> {
        storage.id = id
        val updated = storageService.update(storage)
        return builder.ok().data(updated).build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        storageService.delete(id)
        return builder.ok().message("Storage deleted successfully").build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val storage = storageService.findById(id)
        if (storage != null) {
            return builder.ok().data(storage).build()
        } else {
            return builder.exception().message("Storage not found").build()
        }
    }

    @GetMapping
    fun findAllByDcId(@RequestParam("node_id") nodeId: Long): ResponseEntity<Response> {
        val storages = storageService.findAllByNodeId(nodeId)
        return builder.ok().data(storages).build()
    }

    @GetMapping("/node")
    fun findByNodeName(@RequestParam("node_name") nodeName: String, @RequestParam("node_id") nodeId: Long): ResponseEntity<Response> {
        val storages = storageService.findByNodeNameAndNodeId(nodeName, nodeId)
        return builder.ok().data(storages).build()
    }
}
