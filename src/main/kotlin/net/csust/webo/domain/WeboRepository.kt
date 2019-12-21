package net.csust.webo.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

interface WeboRepository : PagingAndSortingRepository<Webo, UUID> {
    fun getFirst10ByPublishedByIsInAndPublishTimeLessThanOrderByPublishTimeDesc(publishedBy: MutableCollection<Int>, publishTime: Instant) : List<Webo>
    fun getFirst10ByPublishTimeLessThanOrderByPublishTimeDesc(publishTime: Instant) : List<Webo>
}