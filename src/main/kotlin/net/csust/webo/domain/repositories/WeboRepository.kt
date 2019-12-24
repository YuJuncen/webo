package net.csust.webo.domain.repositories

import net.csust.webo.domain.Webo
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.Instant
import java.util.*

interface WeboRepository : PagingAndSortingRepository<Webo, UUID> {
    fun getFirst10ByPublishedByIsInAndPublishTimeLessThanOrderByPublishTimeDesc(publishedBy: MutableCollection<Int>, publishTime: Instant) : List<Webo>
    fun getFirst10ByPublishTimeLessThanOrderByPublishTimeDesc(publishTime: Instant) : List<Webo>
    fun getFirst10ByPublishedByAndPublishTimeIsBeforeOrderByPublishTimeDesc(publishedBy: Int, publishTime: Instant) : List<Webo>
    @Query( """from Webo as webo
            where webo.publishTime < :publishTime 
                and count(webo.likedBy) > :likes
            order by count(webo.likedBy)""",
            countQuery = """
                select count(id) from Webo 
                where webo.publishTime < :publishTime 
                    and count(webo.likedBy) > likes
            """)
    fun getTop10PublishTimeIsBeforeAndLikesIsMoreThanOrderByLikes(publishTime: Instant, likes: Int)
}