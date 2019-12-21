package net.csust.webo.services.kv

import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class ConcurrentHashMapKvService : KvService {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> get(key: String): T = map[key] as T

    override fun put(key: String, value: Any) {
        map[key] = value
    }

    override fun remove(key: String): Boolean = map.remove(key) != null

    val map = ConcurrentHashMap<String, Any>();
}