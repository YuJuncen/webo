package net.csust.webo.services.webo

import com.fasterxml.jackson.annotation.JsonUnwrapped
import java.util.*

data class WeboDetailedView(
        @JsonUnwrapped
        val base: WeboView,

        val sampleLiker: Set<Int>
)