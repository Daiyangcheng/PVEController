package cn.locyan.pvecontroller.service.jdbc

import cn.locyan.pvecontroller.model.Storage
import cn.locyan.pvecontroller.repository.StorageRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StorageServiceImpl(
    private val storageRepository: StorageRepository
) : StorageService {

    override fun create(storage: Storage): Storage {
        storage.createdTime = LocalDateTime.now()
        storage.updatedTime = LocalDateTime.now()
        if (storage.enabled == null) {
            storage.enabled = true
        }
        return storageRepository.save(storage)
    }

    override fun update(storage: Storage): Storage {
        storage.updatedTime = LocalDateTime.now()
        return storageRepository.save(storage)
    }

    override fun delete(id: Long) {
        storageRepository.deleteById(id)
    }

    override fun findById(id: Long): Storage? {
        return storageRepository.findById(id).orElse(null)
    }

    override fun findAllByDcId(dcId: Long): List<Storage> {
        return storageRepository.findAllByDcId(dcId)
    }

    override fun findByNodeName(nodeName: String, dcId: Long): List<Storage> {
        return storageRepository.findByNodeNameAndDcId(nodeName, dcId)
    }
}
