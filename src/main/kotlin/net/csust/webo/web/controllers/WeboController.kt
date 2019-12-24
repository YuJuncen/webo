package net.csust.webo.web.controllers

import net.csust.webo.domain.User
import net.csust.webo.services.webo.WeboService
import net.csust.webo.services.webo.WeboView
import net.csust.webo.web.annotations.InjectUserInfo
import net.csust.webo.web.request.ForwardRequest
import net.csust.webo.web.request.PostWeboRequest
import net.csust.webo.web.request.UUIDRequest
import net.csust.webo.web.response.WeboResponse
import java.util.*
import net.csust.webo.web.response.WeboResponse.Companion.Status.response
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant

private fun (List<WeboView>).attachNextFrom(now: Instant = Instant.now()): Map<String, *> {
    val lastTime = this.lastOrNull()?.publishTime ?: now
    return mapOf(
            "nextFrom" to lastTime,
            "webos" to this
    )
}

@RestController
@RequestMapping("/post")
@Validated
class WeboController(val weboService: WeboService) {
    @GetMapping("")
    fun getPost(id: String, userId: Int?) = weboService
            .getWeboById(UUID.fromString(id), userId)
            .response()

    @GetMapping("all")
    fun getPosts(userId: Int?,
                 before: Instant?): WeboResponse<*> {
        val now = Instant.now()
        val list = if (userId == null)
            weboService.getFeed(before ?: now)
        else
            weboService.getFeedFor(userId, before ?: now)
        val response = list.attachNextFrom(now)
        return response.response()
    }

    @GetMapping("following")
    fun getFollowingWebos(id: Int, before: Instant?): WeboResponse<*> {
        val now = Instant.now()
        val list = weboService.getFollowingWebos(id, before ?: now)
        val response = list.attachNextFrom(now)
        return response.response()
    }

    @PostMapping("new")
    @InjectUserInfo
    fun newPost(@RequestAttribute user: User, @RequestBody req: PostWeboRequest) =
            weboService.publishWebo(user.id!!, req.text).response()

    @GetMapping("mine")
    @InjectUserInfo
    fun getMine(@RequestAttribute userId: Int, before: Instant?) =
            weboService.getWebosOf(userId, before ?: Instant.now()).attachNextFrom().response()

    @PostMapping("like")
    @InjectUserInfo
    fun like(@RequestAttribute user: User, @RequestBody req: UUIDRequest): WeboResponse<*> {
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
    fun delete(@RequestAttribute userId: Int, id: String) : WeboResponse<*> {
        return weboService.deleteWebo(userId, UUID.fromString(id)).response()
    }
}