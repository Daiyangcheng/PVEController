package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.RouterOSApi
import cn.locyan.pvecontroller.repository.RouterOSApiRepository
import me.legrange.mikrotik.ApiConnection
import org.springframework.stereotype.Service

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

    override fun execute(
        id: Long,
        path: String,
        action: String,
        params: List<Map<String, String>>?
    ): List<Map<String, String>>? {
        val ros = findById(id) ?: return null
        val conn = ApiConnection.connect(ros.host)
        conn.login(ros.user, ros.password)
        val command = if (!params.isNullOrEmpty()) {
            "${path}/${action} ${buildCommandString(params)}"
        } else {
            "${path}/${action}"
        }
        println(command)
        val list: List<Map<String, String>> = conn.execute(command)
        conn.close()
        return list
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
}