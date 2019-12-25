package net.csust.webo.services.webo

import net.csust.webo.domain.Webo
import net.csust.webo.domain.repositories.UserRepository
import net.csust.webo.domain.repositories.WeboRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class NaiveRecommendService(val weboRepository: WeboRepository) : RecommendService {

    override fun getRecommendByUser(userId: Int, before: Instant): List<Webo> {
        return weboRepository
                .getFirst10ByPublishTimeLessThanOrderByPublishTimeDesc(before)
    }

    override fun getRecommendGeneric(before: Instant): List<Webo> {
        return weboRepository
                .getFirst10ByPublishTimeLessThanOrderByPublishTimeDesc(before)
    }
}

