package cn.locyan.pvecontroller.controller

import cn.locyan.pvecontroller.model.Node
import cn.locyan.pvecontroller.service.jdbc.NodeService
import cn.locyan.pvecontroller.shared.pve.PVEClient
import cn.locyan.pvecontroller.shared.pve.ProcessPVEResult
import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/* PVE NODE 控制器部分 */
@RestController
class NodeController(
    private val nodeService: NodeService,
    private val processor: ProcessPVEResult,
    private val builder: ResponseBuilder,
    private val pveClient: PVEClient
) {

    // 刷新 Node 信息, Node 表由程序自己维护
    @PostMapping("/nodes")
    fun updateNodesStatus(
        @RequestParam("dc_id") dcId: Long,
    ): ResponseEntity<Response> {
        val client = pveClient.newClient(dcId) ?: return builder.exception().message("无法连接至 PVE 控制器，请检查控制台输出查看详细报错内容").build()
        val nodes = client.nodes.index()
        val check = processor.process(nodes)
        if (check != null) return check
        val data = nodes.data.toList()

        // Node 同名检测, 防止意外
        val nameTmp = mutableListOf<String>()
        data.forEach { node ->
            if (!nameTmp.contains(node.get("node").asText())) {
                nameTmp.add(node.get("node").asText())
                return@forEach
            } else {
                return builder.exception()
                    .message("同数据中心禁止出现同名节点!")
                    .build()
            }
        }

        // 遍历该数据中心下的节点
        val nodeList = mutableListOf<Node>()
        data.forEach { item ->
            // 获取原先存在的数据
            var node = nodeService.findByNameAndDcId(item.get("node").asText(), dcId)
            // 已存在
            if (node != null) {
                // 但是离线了
                if (item.get("status").asText() != "online") {
                    node.apply {
                        this.name = item.get("node").asText()
                        this.status = false
                    }
                    nodeList.add(node)
                    nodeService.update(node)
                    return@forEach
                }
                // 没离线
                node.apply {
                    this.cpu = item.get("cpu").asDouble()
                    this.level = item.get("level").asText()
                    this.maxcpu = item.get("maxcpu").asLong()
                    this.maxmem = item.get("maxmem").asLong()
                    this.mem = item.get("mem").asLong()
                    this.sslFingerprint = item.get("ssl_fingerprint").asText()
                    this.uptime = item.get("uptime").asLong()
                    this.updatedTime = LocalDateTime.now()
                    this.status = true
                }
                nodeList.add(node)
                nodeService.update(node)
                return@forEach
            }
            // 发现新的节点
            node = Node()
            if (item.get("status").asText() != "online") {
                node.apply {
                    this.name = item.get("node").asText()
                    this.status = false
                }
                nodeList.add(node)
                nodeService.update(node)
                return@forEach
            }
            node.apply {
                this.dcId = dcId
                this.name = item.get("node").asText()
                this.cpu = item.get("cpu").asDouble()
                this.level = item.get("level").asText()
                this.maxcpu = item.get("maxcpu").asLong()
                this.maxmem = item.get("maxmem").asLong()
                this.mem = item.get("mem").asLong()
                this.sslFingerprint = item.get("ssl_fingerprint").asText()
                this.uptime = item.get("uptime").asLong()
                this.createdTime = LocalDateTime.now()
                this.updatedTime = LocalDateTime.now()
                this.status = true
            }
            nodeList.add(node)
            nodeService.update(node)
        }
        return builder.ok().data(nodeList).build()
    }

    @GetMapping("/nodes")
    fun findAll(): ResponseEntity<Response> {
        val nodeList = nodeService.findAll()
        return builder.ok().data(nodeList).build()
    }
}