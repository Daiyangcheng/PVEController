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
@Table(name = "storages")
open class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

    @Column(name = "node_name", nullable = false)
    open var nodeName: String? = null

    @Column(name = "storage_type")
    open var storageType: String? = null

    @Column(name = "path")
    open var path: String? = null

    @Column(name = "total_size")
    open var totalSize: Long? = null

    @Column(name = "used_size")
    open var usedSize: Long? = null

    @Column(name = "available_size")
    open var availableSize: Long? = null

    @Column(name = "enabled")
    open var enabled: Boolean? = true

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null
}
