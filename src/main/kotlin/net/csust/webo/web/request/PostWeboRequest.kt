package net.csust.webo.web.request

import javax.validation.constraints.Max
import javax.validation.constraints.Size

data class PostWeboRequest(
        @get:Size(max=320, message = "Webo 最长不能超过 320 个字！否则就不微了！")
        val text : String
)
