package net.csust.webo.web.request

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.lang.NonNull
import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.Size

data class CommentRequest(
        @JsonAlias("id")
        @NonNull
        val commentTo: UUID,
        @Size(max= 256, message= "请不要在评论区发表长篇大论——这不符合 Webo 的撕逼规范。")
        val text: String,
        val replyTo: UUID?
)