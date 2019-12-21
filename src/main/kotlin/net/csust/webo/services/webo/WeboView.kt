package net.csust.webo.services.webo

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import net.csust.webo.domain.Webo
import net.csust.webo.services.user.UserNameView
import java.time.Instant
import java.util.*

data class WeboView(
        val id: UUID,
        val publishTime: Instant,
        val publishedBy: UserNameView?,
        val message: String,
        val likes: Int,
        val forwards: Int,
        val comments: Int,
        val myselfIsLike: Boolean,
        val forwarding: WeboView?
)
