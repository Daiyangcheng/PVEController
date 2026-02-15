package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDateTime

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "servers")
open class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "vm_id")
    open var vmId: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "node_name", nullable = false)
    open var nodeName: String? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

    @Column(name = "user_id", nullable = false)
    open var userId: Long? = null

    @Column(name = "server_group_id")
    open var serverGroupId: Long? = null

    @Column(name = "template_id", nullable = false)
    open var templateId: Long? = null

    @Column(name = "ip_id")
    open var ipId: Long? = null

    @Column(name = "ipv6_id")
    open var ipv6Id: Long? = null

    @Column(name = "cpu")
    open var cpu: Long? = null

    @Column(name = "memory")
    open var memory: Long? = null

    @Column(name = "disk")
    open var disk: Long? = null

    @Column(name = "status")
    open var status: String? = null

    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    open var isDeleted: Boolean? = false

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
