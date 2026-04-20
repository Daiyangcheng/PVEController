package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IpGroup
import cn.locyan.pvecontroller.repository.IpGroupRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IpGroupServiceImpl(
    private val ipGroupRepository: IpGroupRepository
) : IpGroupService {

    override fun create(ipGroup: IpGroup): IpGroup {
        ipGroup.createdTime = LocalDateTime.now()
        ipGroup.updatedTime = LocalDateTime.now()
        return ipGroupRepository.save(ipGroup)
    }

    override fun update(ipGroup: IpGroup): IpGroup {
        ipGroup.updatedTime = LocalDateTime.now()
        return ipGroupRepository.save(ipGroup)
    }

    override fun delete(id: Long) {
        ipGroupRepository.deleteById(id)
    }

    override fun findById(id: Long): IpGroup? {
        return ipGroupRepository.findById(id).orElse(null)
    }

    override fun findAllByNodeId(nodeId: Long): List<IpGroup> {
        return ipGroupRepository.findAllByNodeId(nodeId)
    }
}
