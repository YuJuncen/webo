package net.csust.webo.services.webo

import net.csust.webo.domain.Webo
import java.time.Instant

interface RecommendService {
    fun getRecommendByUser(userId: Int, before: Instant = Instant.now()) : List<Webo>
    fun getRecommendGeneric(before: Instant = Instant.now()) : List<Webo>
}
