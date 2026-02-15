package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.ServerGroup
import cn.locyan.pvecontroller.repository.ServerGroupRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ServerGroupServiceImpl(
    private val serverGroupRepository: ServerGroupRepository
) : ServerGroupService {

    override fun create(serverGroup: ServerGroup): ServerGroup {
        serverGroup.createdTime = LocalDateTime.now()
        serverGroup.updatedTime = LocalDateTime.now()
        return serverGroupRepository.save(serverGroup)
    }

    override fun update(serverGroup: ServerGroup): ServerGroup {
        serverGroup.updatedTime = LocalDateTime.now()
        return serverGroupRepository.save(serverGroup)
    }

    override fun delete(id: Long) {
        serverGroupRepository.deleteById(id)
    }

    override fun findById(id: Long): ServerGroup? {
        return serverGroupRepository.findById(id).orElse(null)
    }

    override fun findAllByDcId(dcId: Long): List<ServerGroup> {
        return serverGroupRepository.findAllByDcId(dcId)
    }

    override fun findByUserId(userId: Long): List<ServerGroup> {
        return serverGroupRepository.findByUserId(userId)
    }
}
