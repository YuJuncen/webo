package net.csust.webo.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface CommentRepository : PagingAndSortingRepository<Comment, UUID> {
    fun getCommentsByCommentToAndReplyToIsNull(commentTo: UUID, pageable: Pageable) : Page<Comment>
    fun getCommentsByCommentToAndReplyTo(commentTo: UUID, replyTo: UUID, pageable: Pageable) : Page<Comment>
    fun getCommentsByCommentTo(commentTo: UUID, pageable: Pageable) : Page<Comment>
    fun countCommentsByCommentTo(commentTo: UUID) : Int
}