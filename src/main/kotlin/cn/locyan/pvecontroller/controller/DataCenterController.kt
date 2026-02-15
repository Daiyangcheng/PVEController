package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.DataCenter
import cn.locyan.pvecontroller.service.jdbc.DataCenterService
import cn.locyan.pvecontroller.shared.pve.PVEClient
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import lombok.experimental.PackagePrivate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/datacenter")
class DataCenterController(
    private val dataCenterService: DataCenterService,
    private val builder: ResponseBuilder,
    private val pveClient: PVEClient
) {

    @PostMapping
    fun create(
        @RequestParam("name") name: String,
        @RequestParam("host") host: String,
        @RequestParam("port") port: Int,
        @RequestParam("ssl") ssl: Boolean,
        @RequestParam("token_id") tokenId: String,
        @RequestParam("token_secret") tokenSecret: String,
    ): ResponseEntity<Response> {
        val dataCenter = DataCenter()
        dataCenter.apply {
            this.id = null
            this.name = name
            this.host = host
            this.port = port
            this.ssl = ssl
            this.tokenId = tokenId
            this.tokenSecret = tokenSecret
            this.status = true
            this.createdTime = LocalDateTime.now()
            this.updatedTime = LocalDateTime.now()
        }
        // 查验可用性
        val success = pveClient.testAuthentication(host, port, ssl, tokenId, tokenSecret)
        if (!success.status) return builder.exception().message("无法连接至 PVE 数据中心, 报错: ${success.msg}").build()
        dataCenterService.create(dataCenter)
        return builder.ok().build()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam("name", required = false) name: String?,
        @RequestParam("host", required = false) host: String?,
        @RequestParam("port", required = false) port: Int?,
        @RequestParam("ssl", required = false) ssl: Boolean?,
        @RequestParam("token_id", required = false) tokenId: String?,
        @RequestParam("token_secret", required = false) tokenSecret: String?,
    ): ResponseEntity<Response> {
        val dataCenter = dataCenterService.findById(id) ?: return builder.exception().message("该数据中心不存在").build()
        dataCenter.apply {
            name?.let { this.name = name }
            host?.let { this.host = host }
            port?.let { this.port = port }
            ssl?.let { this.ssl = ssl }
            tokenId?.let { this.tokenId = tokenId }
            tokenSecret?.let { this.tokenSecret = tokenSecret }
            this.updatedTime = LocalDateTime.now()
        }
        // 查验可用性
        val success = pveClient.testAuthentication(dataCenter.host!!, dataCenter.port!!, dataCenter.ssl!!, dataCenter.tokenId!!, dataCenter.tokenSecret!!)
        if (!success.status) return builder.exception().message("无法连接至 PVE 数据中心, 报错: ${success.msg}").build()
        dataCenterService.update(dataCenter)
        return builder.ok().build()
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Response> {
        val dataCenter = dataCenterService.findById(id) ?: return builder.notFound().build()
        dataCenterService.delete(dataCenter)
        return builder.ok().build()
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Response> {
        val dataCenter = dataCenterService.findById(id) ?: return builder.notFound().build()
        return builder.ok().data(dataCenter).build()
    }

    @GetMapping
    fun findAll(): ResponseEntity<Response> {
        val dataCenters = dataCenterService.findAll()
        return builder.ok().data(dataCenters).build()
    }
}
