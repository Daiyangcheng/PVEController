package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSApi
import cn.locyan.pvecontroller.repository.RouterOSApiRepository
import me.legrange.mikrotik.ApiConnection
import org.springframework.stereotype.Service
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

@Service
class RouterOSApiServiceImpl(
    private val routerOSApiRepository: RouterOSApiRepository
) : RouterOSApiService {

    override fun delete(routerOSApi: RouterOSApi) {
        routerOSApiRepository.delete(routerOSApi)
    }

    override fun update(routerOSApi: RouterOSApi) {
        routerOSApiRepository.save(routerOSApi)
    }

    override fun findById(id: Long): RouterOSApi? {
        return routerOSApiRepository.findById(id).orElse(null)
    }

    override fun findAll(): List<RouterOSApi> {
        return routerOSApiRepository.findAll()
    }

    // param 是列表
    override fun execute(
        id: Long,
        path: String,
        action: String,
        params: List<Map<String, String>>?
    ): List<Map<String, String>>? {
        val ros = findById(id) ?: return null
        val conn = openConnection(ros) ?: return null
        conn.login(ros.user, ros.password)
        if (!conn.isConnected) return null
        val command = if (!params.isNullOrEmpty()) {
            "${path}/${action} ${buildCommandString(params)}"
        } else {
            "${path}/${action}"
        }
        val list: List<Map<String, String>> = conn.execute(command)
        conn.close()
        return list
    }

    // param 是 String
    override fun execute(
        id: Long,
        path: String,
        action: String,
        params: String
    ): List<Map<String, String>>? {
        val ros = findById(id) ?: return null
        val conn = openConnection(ros) ?: return null
        conn.login(ros.user, ros.password)
        if (!conn.isConnected) return null
        val command = if (params.isNotEmpty()) {
            "${path}/${action} $params"
        } else {
            "${path}/${action}"
        }
        val list: List<Map<String, String>> = conn.execute(command)
        conn.close()
        return list
    }

    override fun testConn(
        host: String,
        port: Int,
        ssl: Boolean,
        user: String,
        password: String
    ): Boolean {
        val socketFactory = getSocketFactory(ssl)
        val targetPort = resolvePort(port, ssl)
        val conn = ApiConnection.connect(socketFactory, host, targetPort, ApiConnection.DEFAULT_CONNECTION_TIMEOUT)
        conn.login(user, password)
        val status = conn.isConnected
        conn.close()
        return status
    }

    fun buildCommandString(paramsList: List<Map<String, String>>): String {
        return paramsList.joinToString(" ") { paramMap ->
            paramMap.entries.joinToString(" ") { (key, value) ->
                val formattedValue = if (value.contains(" ") || value.isEmpty()) {
                    "\"$value\""
                } else {
                    value
                }
                "$key=$formattedValue"
            }
        }
    }

    private fun openConnection(ros: RouterOSApi): ApiConnection? {
        val host = ros.host ?: return null
        val socketFactory = getSocketFactory(ros.ssl == true)
        val targetPort = resolvePort(ros.port, ros.ssl == true)
        return ApiConnection.connect(socketFactory, host, targetPort, ApiConnection.DEFAULT_CONNECTION_TIMEOUT)
    }

    private fun getSocketFactory(ssl: Boolean): SocketFactory {
        return if (ssl) {
            SSLSocketFactory.getDefault()
        } else {
            SocketFactory.getDefault()
        }
    }

    private fun resolvePort(port: Int?, ssl: Boolean): Int {
        return when {
            port != null && port > 0 -> port
            ssl -> ApiConnection.DEFAULT_TLS_PORT
            else -> ApiConnection.DEFAULT_PORT
        }
    }
}
