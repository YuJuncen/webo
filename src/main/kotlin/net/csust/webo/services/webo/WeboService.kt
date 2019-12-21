package net.csust.webo.services.webo

import net.csust.webo.domain.UserRepository
import net.csust.webo.domain.Webo
import net.csust.webo.domain.WeboRepository
import net.csust.webo.services.user.toNameView
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException
import java.time.Instant
import java.util.*

@Service
class WeboService(val userRepository: UserRepository,
                  val weboRepository: WeboRepository,
                  val recommend: RecommendService) {
    fun Webo.snapshotView(requestedByUser: Int? = null) : WeboView {
        val publisher = userRepository.findByIdOrNull(this.publishedBy)?.toNameView()
        val ifPublisherLike = requestedByUser?. let { this.likedBy.contains(it) } ?: false
        val forwarding: Webo? = this.forwarding?. let { weboRepository.findByIdOrNull(it) }
        return WeboView(
                this.id!!,
                this.publishTime,
                publisher,
                this.content,
                this.likedBy.size,
                this.forwardedBy.size,
                0,
                ifPublisherLike,
                forwarding?.snapshotView(requestedByUser)
        )
    }

    fun WeboView.enhance(sampleSize: Int = 5, origin: Webo) : WeboDetailedView {
        return WeboDetailedView(
                base= this,
                sampleLiker = origin.likedBy.take(5).toSet()
        )
    }

    @Transactional
    fun getFeedFor(userId: Int, before: Instant): List<WeboView> {
        return recommend.getRecommendByUser(userId, before)
                .map { it.snapshotView(userId) }
    }

    @Transactional
    fun getFeed(before: Instant) : List<WeboView> {
        return recommend.getRecommendGeneric(before)
                .map { it.snapshotView() }
    }

    @Transactional
    fun getWeboById(webo: UUID, requester: Int? = null) = weboRepository.findByIdOrNull(webo)
            ?.let { it.snapshotView(requester).enhance(origin = it) }

        /**
         * 让某个用户喜欢一个 Webo。
         * 如果已经喜欢，那就不喜欢这个 Webo。
         * @param userId 操作的用户 ID。
         * @param weboId 操作的 Webo ID。
         * @return 用户是否还爱着这个 Webo。
         */
        @Transactional
        fun like(userId: Int, weboId: UUID) : Boolean {
            val toLike = weboRepository.findByIdOrNull(weboId)!!
            val stillLike = userRepository.findByIdOrNull(userId)!!.toggleLike(toLike)
            weboRepository.save(toLike)
            return stillLike
        }

        @Transactional
        fun publishWebo(userId: Int, weboMessage: String) : WeboView {
            val user = userRepository.findById(userId).orElse(null)
            val webo = user.writeWebo(weboMessage)
            return weboRepository.save(webo).snapshotView()
        }

        @Transactional
        fun deleteWebo(userId: Int, weboId: UUID) {
            val webo = weboRepository.findByIdOrNull(weboId) ?: throw IllegalStateException("您的帖已经不存在了，求求您别删了。🙏")
            if (webo.publishedBy != userId) throw SecurityException("不是您发的贴您也敢删？好大的官威啊！👑")

            weboRepository.findAllById(webo.forwardedBy).forEach {
                it.forwarding = null
                weboRepository.save(it)
            }
            weboRepository.delete(webo)
        }

        @Transactional
        fun forward(userId: Int, weboId: UUID, message: String? = null) : WeboView {
            val webo = weboRepository.findByIdOrNull(weboId)!!
            val forwardedWebo = webo.forward(userId, message ?: "💬转发一条 Webo～")
            weboRepository.save(webo)
            return weboRepository.save(forwardedWebo).snapshotView()
        }
    }
