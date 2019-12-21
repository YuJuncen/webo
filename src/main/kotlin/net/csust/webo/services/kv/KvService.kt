package net.csust.webo.services.kv

import org.springframework.lang.NonNull

interface KvService {
    operator fun <T> get(@NonNull key: String): T
    fun put(@NonNull key: String, @NonNull value: Any)
    fun remove(@NonNull key: String): Boolean
}
