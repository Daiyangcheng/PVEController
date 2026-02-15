package cn.locyan.pvecontroller.shared.pve

import cn.locyan.pvecontroller.service.jdbc.DataCenterService
import it.corsinvest.proxmoxve.api.PveClient
import org.springframework.stereotype.Component

@Component
class PVEClient(
    private val dataCenterService: DataCenterService
) {
    fun newClient(dcId: Long): PveClient? {
        val dc = dataCenterService.findById(dcId) ?: return null
        val host = dc.host
        val port = dc.port ?: 8006
        val tokenId = dc.tokenId
        val tokenSecret = dc.tokenSecret
        val pveClient = PveClient(host, port)
        pveClient.apiToken = "${tokenId}=${tokenSecret}"
        pveClient.validateCertificate = dc.ssl ?: false
        val success = testAuthentication(pveClient)
        if (!success.status) {
            // 输出报错
            print(success.msg)
            return null
        }
        return pveClient
    }

    fun testAuthentication(client: PveClient): TestResult {
        try {
            val version = client.version.version()
            if (version.isSuccessStatusCode) {
                val rs = TestResult(true, "Connected to Proxmox VE " + version.data.get("version").asText())
                return rs
            } else {
                val rs = TestResult(false, "Authentication failed: " + version.reasonPhrase)
                return rs
            }
        } catch (ex: Exception) {
            val rs = TestResult(false, "Connection error: " + ex.message)
            return rs
        }
    }

    fun testAuthentication(host: String, port: Int = 8006, ssl: Boolean = false, tokenId: String, tokenSecret: String): TestResult {
        val client = PveClient(host, port)
        client.apiToken = "${tokenId}=${tokenSecret}"
        client.validateCertificate = ssl
        try {
            val version = client.version.version()
            if (version.isSuccessStatusCode) {
                val rs = TestResult(true, "Connected to Proxmox VE " + version.data.get("version").asText())
                return rs
            } else {
                val rs = TestResult(false, "Authentication failed: " + version.reasonPhrase)
                return rs
            }
        } catch (ex: Exception) {
            val rs = TestResult(false, "Connection error: " + ex.message)
            return rs
        }
    }

    data class TestResult(
        val status: Boolean,
        val msg: String
    )
}