package cn.locyan.pvecontroller.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "router_os_rules")
class RouterOSRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null

    @Column(name = "api_id", nullable = false)
    var apiId: Long? = null

    @Column(name = "rule_id", nullable = false)
    var ruleId: String? = null

    @Column(name = "template_id", nullable = false)
    var templateId: Long? = null

    @Column(name = "params", nullable = false)
    var params: String? = null
}