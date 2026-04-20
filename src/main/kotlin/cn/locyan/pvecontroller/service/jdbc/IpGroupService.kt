package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.IpGroup

interface IpGroupService {
    fun create(ipGroup: IpGroup): IpGroup
    fun update(ipGroup: IpGroup): IpGroup
    fun delete(id: Long)
    fun findById(id: Long): IpGroup?
    fun findAllByNodeId(nodeId: Long): List<IpGroup>
}
