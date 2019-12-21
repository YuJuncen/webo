package net.csust.webo.web.request

import com.fasterxml.jackson.annotation.JsonAlias
import java.util.*

data class CommentRequest(
        @JsonAlias("id")
        val commentTo: UUID,
        val text: String,
        val replyTo: UUID?
)