package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Server
import cn.locyan.pvecontroller.repository.ServerRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ServerServiceImpl(
    private val serverRepository: ServerRepository
) : ServerService {
    
    override fun create(server: Server): Server {
        server.createdTime = LocalDateTime.now()
        server.updatedTime = LocalDateTime.now()
        return serverRepository.save(server)
    }

    override fun update(server: Server): Server {
        server.updatedTime = LocalDateTime.now()
        return serverRepository.save(server)
    }

    override fun delete(server: Server) {
        serverRepository.delete(server)
    }

    override fun findById(id: Long): Server? {
        return serverRepository.findById(id).orElse(null)
    }

    override fun findByUserId(userId: Long): List<Server> {
        return serverRepository.findByUserId(userId)
    }

    override fun findByTemplateId(templateId: Long): List<Server> {
        return serverRepository.findByTemplateId(templateId)
    }

    override fun findByIpId(ipId: Long): Server? {
        return serverRepository.findByIpId(ipId)
    }
}
