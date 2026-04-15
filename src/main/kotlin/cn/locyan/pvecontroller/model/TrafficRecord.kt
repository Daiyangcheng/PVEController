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
@Table(name = "traffic_records")
class TrafficRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "server_id", nullable = false, unique = true)
    var serverId: Long? = null

    @Column(name = "upload_bytes")
    var uploadBytes: Long? = null

    @Column(name = "download_bytes")
    var downloadBytes: Long? = null

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
}
