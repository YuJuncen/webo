package net.csust.webo.services.webo

import net.csust.webo.domain.UserRepository
import net.csust.webo.domain.Webo
import net.csust.webo.domain.WeboRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class NaiveRecommendService(val userRepository: UserRepository,
                            val weboRepository: WeboRepository) : RecommendService {

    override fun getRecommendByUser(userId: Int, before: Instant): List<Webo> {
        val user = userRepository.findById(userId).orElse(null)
        val following = user.following
        return weboRepository
                .getFirst10ByPublishedByIsInAndPublishTimeLessThanOrderByPublishTimeDesc(following, before)
    }

    override fun getRecommendGeneric(before: Instant): List<Webo> {
        return weboRepository
                .getFirst10ByPublishTimeLessThanOrderByPublishTimeDesc(before)
    }
}

