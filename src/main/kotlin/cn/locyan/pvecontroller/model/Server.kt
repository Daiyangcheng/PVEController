package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "servers")
class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "vm_id")
    var vmId: Long? = null

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "node_name", nullable = false)
    var nodeId: Long? = null

    @Column(name = "dc_id", nullable = false)
    var dcId: Long? = null

    @Column(name = "user_id", nullable = false)
    var userId: Long? = null

    @Column(name = "server_group_id")
    var serverGroupId: Long? = null

    @Column(name = "template_id", nullable = false)
    var templateId: Long? = null

    @Column(name = "ip_id")
    var ipId: Long? = null

    @Column(name = "ipv6_id")
    var ipv6Id: Long? = null

    @Column(name = "cpu")
    var cpu: Long? = null

    @Column(name = "memory")
    var memory: Long? = null

    @Column(name = "disk")
    var disk: Long? = null

    @Column(name = "status")
    var status: String? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
