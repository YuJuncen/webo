package net.csust.webo.services.webo

import net.csust.webo.services.user.UserNameView
import java.time.Instant
import java.util.*

data class CommentView(
        val id: UUID? = null,
        val publisher : UserNameView?,
        val content : String,
        val publishTime : Instant,
        val replyTo : UUID? = null
)