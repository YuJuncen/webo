package net.csust.webo.web.controllers

import net.csust.webo.domain.User
import net.csust.webo.services.webo.WeboService
import net.csust.webo.web.annotations.InjectUserInfo
import net.csust.webo.web.request.ForwardRequest
import net.csust.webo.web.request.PostWeboRequest
import net.csust.webo.web.request.WeboIdRequest
import net.csust.webo.web.response.WeboResponse
import java.util.*
import net.csust.webo.web.response.WeboResponse.Companion.Status.response
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant
import javax.validation.Valid

@RestController
@RequestMapping("/post")
@Validated
class WeboController(val weboService: WeboService) {
    @GetMapping("")
    fun getPost(id: String) = weboService.getWeboById(UUID.fromString(id)).response()

    @GetMapping("all")
    fun getPosts(userId: Int?,
                 before: Instant?): WeboResponse<*> {
        val now = Instant.now()
        val list = if (userId == null)
            weboService.getFeed(before ?: now)
        else
            weboService.getFeedFor(userId, before ?: now)
        val lastTime = list.lastOrNull()?.publishTime ?: now
        val response = mapOf(
                    "nextFrom" to lastTime,
                    "webos" to list
            )
        return response.response()
    }

    @PostMapping("new")
    @InjectUserInfo
    @ResponseStatus(HttpStatus.CREATED)
    fun newPost(@RequestAttribute user: User, @RequestBody req: PostWeboRequest) =
            weboService.publishWebo(user.id!!, req.text).response()

    @PostMapping("like")
    @InjectUserInfo
    fun like(@RequestAttribute user: User, @RequestBody req: WeboIdRequest): WeboResponse<*> {
        val stillLike = weboService.like(user.id!!, req.id)
        return mapOf("stillLike" to stillLike).response()
    }

    @PostMapping("forward")
    @InjectUserInfo
    fun forward(@RequestAttribute user: User, @RequestBody req: ForwardRequest): WeboResponse<*> {
        return weboService.forward(user.id!!, req.id, req.message).response()
    }

    @DeleteMapping("")
    @InjectUserInfo
    fun delete(@RequestAttribute user: User, @RequestBody req: WeboIdRequest) : WeboResponse<*> {
        return weboService.deleteWebo(user.id!!, req.id).response()
    }
}