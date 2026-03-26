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
import java.time.Instant
import java.time.LocalDateTime

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "nodes")
class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "name", nullable = false)
    var name: String? = null

    @Column(name = "\"group\"")
    var group: Long? = null

    @Column(name = "cpu")
    var cpu: Double? = null

    @Column(name = "maxcpu")
    var maxcpu: Long? = null

    @Column(name = "mem")
    var mem: Long? = null

    @Column(name = "maxmem")
    var maxmem: Long? = null

    @ColumnDefault("NULL")
    @Column(name = "level")
    var level: String? = null

    @ColumnDefault("NULL")
    @Column(name = "ssl_fingerprint")
    var sslFingerprint: String? = null

    @Column(name = "uptime")
    var uptime: Long? = null

    @ColumnDefault("false")
    @Column(name = "status", nullable = false)
    var status: Boolean? = null

    @Column(name = "created_time", nullable = false)
    var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    var updatedTime: LocalDateTime? = null

    @Column(name = "dc_id", nullable = false)
    var dcId: Long? = null

}