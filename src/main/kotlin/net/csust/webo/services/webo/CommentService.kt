package net.csust.webo.services.webo

import net.csust.webo.domain.Comment
import net.csust.webo.domain.repositories.CommentRepository
import net.csust.webo.domain.repositories.WeboRepository
import net.csust.webo.services.user.UserService
import net.csust.webo.services.webo.exceptions.NoSuchCommentException
import net.csust.webo.services.webo.exceptions.NoSuchWeboException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*


@Service
class CommentService(
        val commentRepository: CommentRepository,
        val weboRepository: WeboRepository,
        val userService: UserService
) {
    fun (Comment).snapshotView(): CommentView {
        val publisher = userService.getViewOfUserId(this.publisher)
        return CommentView(
                this.id, publisher, this.content, this.publishTime, this.replyTo
        )
    }

    fun getCommentsOf(weboId: UUID, before: Instant = Instant.now()) =
            commentRepository.getTop10ByCommentToAndPublishTimeBeforeOrderByPublishTimeDesc(weboId, before)
                    .map { it.snapshotView() }

    fun publishCommentTo(weboId: UUID, publisher: Int,
                         message: String, replying: UUID? = null) : Comment {
        val comment = if (replying == null) {
            val webo = weboRepository.findByIdOrNull(weboId) ?: throw NoSuchWeboException
            webo.commentTo(by = publisher, content = message)
        } else {
            val originalComment = commentRepository.findByIdOrNull(replying) ?: throw NoSuchCommentException
            originalComment.reply(publisher, message)
        }
        return commentRepository.save(comment)
    }

    fun removeComment(operator: Int, commentId: UUID) {
        val comment = commentRepository.findByIdOrNull(commentId) ?: throw NoSuchCommentException
        if (comment.publisher != operator) throw SecurityException("这不是你的评论。")
        commentRepository.deleteById(commentId)
    }
}