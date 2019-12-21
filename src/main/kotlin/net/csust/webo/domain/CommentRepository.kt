package net.csust.webo.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.Instant
import java.util.*

interface CommentRepository : PagingAndSortingRepository<Comment, UUID> {
    fun getCommentsByCommentTo(commentTo: UUID, pageable: Pageable) : Page<Comment>
    fun getTop10ByCommentToAndPublishTimeBeforeOrderByPublishTimeDesc(commentTo: UUID, publishTime: Instant) : List<Comment>
    fun countCommentsByCommentTo(commentTo: UUID) : Int
}