package net.csust.webo.web.controllers

import net.csust.webo.domain.repositories.UserRepository
import net.csust.webo.services.jwt.JwtService
import net.csust.webo.services.user.UserNameView
import net.csust.webo.services.user.UserService
import net.csust.webo.services.user.toNameView
import net.csust.webo.services.webo.user.WeboUserService
import net.csust.webo.web.annotations.InjectUserInfo
import net.csust.webo.web.request.*
import net.csust.webo.web.response.TokenPairAndUserInfoView
import net.csust.webo.web.response.WeboResponse
import net.csust.webo.web.response.WeboResponse.Companion.Status.response
import org.springframework.data.domain.PageRequest.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/user")
@Validated
class UserController(val userService: UserService,
                     val weboUserService: WeboUserService,
                     val userRepository: UserRepository,
                     val jwt: JwtService) {
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: UserRequest): WeboResponse<*> {
        val user = userService.getUserInfo(req.username)
        val token = jwt.login(req.username, req.password)
        return TokenPairAndUserInfoView(token, user!!.toNameView()).response()
    }


    @PostMapping("/register")
    fun register(@Valid @RequestBody req: UserRegisterRequest) =
            userService.register(req.username, req.password, req.nickname, req.email).let {
                TokenPairAndUserInfoView(jwt.sign(it),  it.toNameView())
            }.response()

    @PostMapping("/refresh")
    fun refresh(@RequestHeader("Authorization") refreshToken: String) =
        jwt.refresh(refreshToken).response()

    @PostMapping("/follow")
    @Transactional
    @InjectUserInfo
    fun followAlpha(@RequestAttribute userId: Int, @Valid @RequestBody request: FollowRequest): WeboResponse<*> {
        val to = request.to
        val resp = if (weboUserService.isFollowing(userId, to))
            weboUserService.unfollow(userId, to) else weboUserService.follow(userId, to)
        return resp.response()
    }

    @GetMapping("/follow/all")
    fun getFollows(id: Int,

                   @RequestParam(required = false, defaultValue = "0")
                   page: Int,

                   @RequestParam(required = false, defaultValue = "10")
                   size: Int) = weboUserService
            .getFollowing(id, of(page, size))
            .map {
                userService.getViewOfUserId(it)
            }.response()

    @GetMapping("/follow/all-by")
    fun getFollowBy(id: Int,

                    @RequestParam(required = false, defaultValue = "0")
                    page: Int,

                    @RequestParam(required = false, defaultValue = "10")
                    size: Int) = weboUserService
            .getFollowers(id, of(page, size))
            .map {
                userService.getViewOfUserId(it)
            }.response()

    @PostMapping("/modify")
    @InjectUserInfo
    fun modify(@RequestAttribute userId: Int, @Valid @RequestBody modifier: AttributeBasedUserInfoModifier): WeboResponse<*> {
        return userService.modifyUser(userId, modifier).response()
    }

    @PostMapping("/change-password")
    @InjectUserInfo
    fun changePassword(@RequestAttribute userId: Int, @Valid @RequestBody changePasswordRequest: ChangePasswordRequest) : WeboResponse<*> {
        val user = userRepository.findByIdOrNull(userId)!!
        user.changePassword(changePasswordRequest.origin, changePasswordRequest.changed)
        userRepository.save(user)
        val signed = jwt.sign(user, user.lastPasswordChanged.plusSeconds(1))
        return signed.response()
    }

    @GetMapping("")
    fun getUserInfo(id: Int) = weboUserService.getUserOf(id).response()

    @GetMapping("/me")
    @InjectUserInfo
    fun getMyInfo(@RequestAttribute userId: Int) = weboUserService.getUserOf(userId).response()
}