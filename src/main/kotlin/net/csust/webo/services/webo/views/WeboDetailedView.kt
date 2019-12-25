package net.csust.webo.services.webo.views

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class WeboDetailedView(
        val base: WeboView,

        val sampleLiker: Set<Int>
)