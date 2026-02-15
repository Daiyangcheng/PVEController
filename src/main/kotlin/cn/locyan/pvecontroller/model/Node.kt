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
open class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Column(name = "\"group\"")
    open var group: Long? = null

    @Column(name = "cpu")
    open var cpu: Double? = null

    @Column(name = "maxcpu")
    open var maxcpu: Long? = null

    @Column(name = "mem")
    open var mem: Long? = null

    @Column(name = "maxmem")
    open var maxmem: Long? = null

    @ColumnDefault("NULL")
    @Column(name = "level")
    open var level: String? = null

    @ColumnDefault("NULL")
    @Column(name = "ssl_fingerprint")
    open var sslFingerprint: String? = null

    @Column(name = "uptime")
    open var uptime: Long? = null

    @ColumnDefault("false")
    @Column(name = "status", nullable = false)
    open var status: Boolean? = null

    @Column(name = "created_time", nullable = false)
    open var createdTime: LocalDateTime? = null

    @Column(name = "updated_time", nullable = false)
    open var updatedTime: LocalDateTime? = null

    @Column(name = "dc_id", nullable = false)
    open var dcId: Long? = null

}