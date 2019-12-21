package net.csust.webo.web.request

import java.util.*
import javax.validation.constraints.Max

data class ForwardRequest(
        val id: UUID,

        @get:Max(320, message = "Webo 最长不能超过 320 个字，转发感言也不行！")
        val message: String?
)