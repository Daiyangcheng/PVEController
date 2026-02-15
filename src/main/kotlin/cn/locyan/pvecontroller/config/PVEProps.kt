package cn.locyan.pvecontroller.config

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "pve")
class PVEProps {
    private val host: String? = null
    private val port: Int? = null
    private val tokenId: String? = null
    private val tokenSecret: String? = null
}