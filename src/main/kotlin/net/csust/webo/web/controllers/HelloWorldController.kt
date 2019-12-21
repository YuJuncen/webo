package net.csust.webo.web.controllers

import net.csust.webo.domain.User
import net.csust.webo.domain.UserRepository
import net.csust.webo.services.jwt.JwtService
import net.csust.webo.services.jwt.TokenPair
import net.csust.webo.services.user.UserService
import net.csust.webo.services.webo.WeboService
import net.csust.webo.web.response.WeboResponse
import net.csust.webo.web.response.WeboResponse.Companion.Status.makeResponseWith
import net.csust.webo.web.response.WeboResponse.Companion.Status.response
import net.csust.webo.web.response.WeboResponse.Companion.Status
import net.csust.webo.web.annotations.InjectUserInfo
import net.csust.webo.web.request.PageRequest
import net.csust.webo.web.request.UserRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class HelloWorldController(val jwt: JwtService, val user: UserRepository, val users: UserService, val weboService: WeboService) {
    val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/")
    fun hello() = "hello, world!".response()

    @GetMapping("/error-test")
    fun fuck() = Status.SERVER_ERROR.makeResponseWith("Damn... We boom shakalaka!")

    @PostMapping("/echo")
    fun echo(@RequestBody json: Map<String, Any>) = json.response()

    @PostMapping("/unformed")
    fun unformed(@RequestBody json: UserRequest) : Nothing = TODO()

    @GetMapping("/login")
    fun login(): WeboResponse<out TokenPair> {
        user.save(User(null, "XiaoMing2", "1234567"))
        return jwt.sign(user.findUserByUsername("XiaoMing2")!!).response()
    }

    @PostMapping("/file")
    fun upload(@RequestBody blob: MultipartFile, @RequestParam message: String?): String {
        logger.info("we got ${blob.bytes.size} bytes, Client said: $message! What a great success!")
        return "Ok! We got it!"
    }

    @InjectUserInfo
    @GetMapping("/get-user")
    fun getUser(@RequestAttribute(name = "user") u: User): User {
        return u
    }

    @InjectUserInfo
    @PostMapping("/follow")
    fun followUser(@RequestAttribute(name = "user") u: User, @RequestParam followTo: Int) : WeboResponse<*> {
        users.addFollow(u.id!!, followTo)
        return "${u.nickname} is now following UID: $followTo".response()
    }

    @GetMapping("/get-user-following")
    fun getUserFollow(userId: Int): WeboResponse<*> {
        return users.getFollowings(userId).response()
    }

    @InjectUserInfo
    @PostMapping("/post-webo")
    fun postWebo(@RequestAttribute user: User, message: String?): WeboResponse<*> {
        weboService.publishWebo(user.id!!, message ?: "我还有什么话可说呢？")
        return "OK".response()
    }
}