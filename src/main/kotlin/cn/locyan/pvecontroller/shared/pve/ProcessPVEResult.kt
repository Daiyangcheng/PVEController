package cn.locyan.pvecontroller.shared.pve

import cn.locyan.pvecontroller.shared.response.Response
import cn.locyan.pvecontroller.shared.response.ResponseBuilder
import it.corsinvest.proxmoxve.api.Result
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ProcessPVEResult(
    private val builder: ResponseBuilder
) {
    fun process(result: Result): ResponseEntity<Response>? {
        if (result.isSuccessStatusCode && !result.responseInError()) {
            return null
        } else if (result.responseInError()) {
            return builder.exception().message("API Error: ${result.error}").build()
        } else {
            return builder.exception().message("HTTP Error: ${result.statusCode}: ${result.reasonPhrase}").build()
        }
    }
}