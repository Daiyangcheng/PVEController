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
class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "dc_id", nullable = false)
    var dcId: Long? = null

    @Column(name = "node_name", nullable = false)
    var nodeName: String? = null

    @Column(name = "storage_type")
    var storageType: String? = null

    @Column(name = "path")
    var path: String? = null

    @Column(name = "total_size")
    var totalSize: Long? = null

    @Column(name = "used_size")
    var usedSize: Long? = null

    @Column(name = "available_size")
    var availableSize: Long? = null

    @Column(name = "enabled")
    var enabled: Boolean? = true

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null
}
