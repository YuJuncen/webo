package net.csust.webo.web.controllers

import net.csust.webo.services.jwt.JwtService
import net.csust.webo.services.user.UserService
import net.csust.webo.services.user.toNameView
import net.csust.webo.services.webo.user.WeboUserService
import net.csust.webo.web.annotations.InjectUserInfo
import net.csust.webo.web.request.FollowRequest
import net.csust.webo.web.request.UserRequest
import net.csust.webo.web.response.TokenPairAndUserInfoView
import net.csust.webo.web.request.UserRegisterRequest
import net.csust.webo.web.response.WeboResponse
import net.csust.webo.web.response.WeboResponse.Companion.Status.response
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

@RestController
@RequestMapping("/user")
@Validated
class UserController(val userService: UserService,
                     val weboUserService: WeboUserService,
                     val jwt: JwtService) {
    @PostMapping("/login")
    fun login(@RequestBody req: UserRequest): WeboResponse<*> {
        val user = userService.getUserInfo(req.username)
        val token = jwt.login(req.username, req.password)
        return TokenPairAndUserInfoView(token, user!!.toNameView()).response()
    }


    @PostMapping("/register")
    fun register(@RequestBody req: UserRegisterRequest) =
            userService.register(req.username, req.password, req.nickname, req.email).let {
                TokenPairAndUserInfoView(jwt.sign(it),  it.toNameView())
            }.response()

    @PostMapping("/refresh")
    fun refresh(@RequestHeader("Authorization") refreshToken: String) =
        jwt.refresh(refreshToken).response()

    @PostMapping("/follow")
    @Transactional
    @InjectUserInfo
    fun followAlpha(@RequestAttribute userId: Int, @RequestBody request: FollowRequest): WeboResponse<*> {
        val to = request.to
        val resp = if (weboUserService.isFollowing(userId, to))
            weboUserService.unfollow(userId, to) else weboUserService.follow(userId, to)
        return resp.response()
    }

    @GetMapping("/follow/all")
    fun getFollows(id: Int) = weboUserService.getFollowing(id).response()

    @GetMapping("/follow/all-by")
    fun getFollowBy(id: Int) = weboUserService.getFollowers(id).response()
}