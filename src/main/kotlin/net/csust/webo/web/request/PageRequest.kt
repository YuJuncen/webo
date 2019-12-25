package net.csust.webo.web.request

import org.springframework.data.domain.PageRequest

data class PageRequest(
    val offset: Int,
    val limit: Int
)  {
    fun toPageable() = PageRequest.of(offset / limit, limit)
}