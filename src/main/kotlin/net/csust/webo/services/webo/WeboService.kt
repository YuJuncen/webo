package net.csust.webo.services.webo

import net.csust.webo.domain.Webo
import net.csust.webo.domain.repositories.CommentRepository
import net.csust.webo.domain.repositories.UserRepository
import net.csust.webo.domain.repositories.WeboRepository
import net.csust.webo.services.user.toNameView
import net.csust.webo.services.webo.user.WeboUserService
import net.csust.webo.services.webo.views.WeboDetailedView
import net.csust.webo.services.webo.views.WeboUserView
import net.csust.webo.services.webo.views.WeboView
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class WeboService(val userRepository: UserRepository,
                  val weboRepository: WeboRepository,
                  val commentRepository: CommentRepository,
                  val weboUserService: WeboUserService,
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
                commentRepository.countCommentsByCommentTo(this.id!!),
                ifPublisherLike,
                forwarding?.snapshotView(requestedByUser)
        )
    }

    fun WeboView.enhance(sampleSize: Int = 5, origin: Webo) : WeboDetailedView {
        return WeboDetailedView(
                base = this,
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
    fun getWebosOf(userId: Int, before: Instant) : List<WeboView> {
        return weboRepository.getFirst10ByPublishedByAndPublishTimeIsBeforeOrderByPublishTimeDesc(userId, before)
                .map { it.snapshotView(userId) }
    }

    @Transactional
    fun getFollowingWebos(userId: Int, before: Instant) : List<WeboView> {
        val user = userRepository.findById(userId).orElse(null)
        val following = user.following
        return weboRepository
                .getFirst10ByPublishedByIsInAndPublishTimeLessThanOrderByPublishTimeDesc(following, before)
                .map { it.snapshotView(userId) }
    }

    @Transactional
    fun getWeboById(webo: UUID, requester: Int? = null) = weboRepository.findByIdOrNull(webo)
            ?.let { it.snapshotView(requester).enhance(origin = it) }

    /**
     * 让某个用户喜欢一个 Webo。
     * 如果已经喜欢，那就不喜欢这个 Webo。
     * @param userId 操作的用户 ID。
     * @param weboId 操作的 Webo ID。
     * @return 几个人还爱着这个 Webo。
     */
    @Transactional
    fun like(userId: Int, weboId: UUID) : Int {
        val toLike = weboRepository.findByIdOrNull(weboId)!!
        userRepository.findByIdOrNull(userId)!!.toggleLike(toLike)
        val webo = weboRepository.save(toLike)
        return webo.likedBy.size
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
        commentRepository.deleteAllByCommentTo(weboId)
    }

    @Transactional
    fun forward(userId: Int, weboId: UUID, message: String? = null) : WeboView {
        val webo = weboRepository.findByIdOrNull(weboId)!!
        val forwardedWebo = webo.forward(userId, message ?: "💬转发一条 Webo～")
        weboRepository.save(webo)
        return weboRepository.save(forwardedWebo).snapshotView()
    }

    fun getWeboUserInfo(userId: Int) : WeboUserView? {
        return weboUserService.getUserOf(userId)
    }

}
