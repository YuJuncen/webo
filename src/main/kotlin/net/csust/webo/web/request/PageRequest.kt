package net.csust.webo.web.request

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class PageRequest(
    val offset: Int,
    val limit: Int
)  {
    fun toPageable() = PageRequest.of(offset / limit, limit)
}