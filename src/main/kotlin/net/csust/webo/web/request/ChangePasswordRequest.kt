package net.csust.webo.web.request

import javax.validation.constraints.Size

data class ChangePasswordRequest(
        @get:Size(max= 64)
        val origin: String,

        @get:Size(max= 64)
        val changed: String
)