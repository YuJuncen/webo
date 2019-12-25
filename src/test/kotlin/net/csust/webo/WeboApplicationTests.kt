package net.csust.webo

import com.fasterxml.jackson.databind.ObjectMapper
import net.csust.webo.services.jwt.TokenPair
import net.csust.webo.services.user.UserNameView
import net.csust.webo.services.webo.views.CommentView
import net.csust.webo.services.webo.views.WeboDetailedView
import net.csust.webo.services.webo.views.WeboUserView
import net.csust.webo.services.webo.views.WeboView
import net.csust.webo.web.response.WeboResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import net.csust.webo.web.response.WeboResponse.Companion.Status
import org.junit.Assert
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Instant
import java.util.*

private const val MY_NAME = "maruruku"
private const val MY_PASSWORD = "a123456;"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeboApplicationTests(@Autowired val mapper : ObjectMapper){
    @LocalServerPort
    var port: Int = 0

    @Autowired
    var rest: TestRestTemplate? = null

    @Test
    fun contextLoads() { }

    @Test
    fun createUser() {
        getUserOrRegister(MY_NAME, MY_PASSWORD)
    }

    @Test
    fun testPostWebo() {
        val token = getUserOrRegister(MY_NAME, MY_PASSWORD).token
        (1..15).forEach { _ ->
            postWebo(token, "我能发帖木？")
        }
        println(getMine("/post/all", mapOf("userId" to 1)).data)
    }

    @Test
    fun testPostWebo2() {
        val token = getUserOrRegister(MY_NAME, MY_PASSWORD).token
        (1..15).forEach { _ ->
            postWebo(token, "我能发帖木？")
        }
        val webos = getMyWebos(token)
        Assert.assertEquals(10, webos.size)
    }

    @Test
    fun testLikeWebo() {
        val user = getUserOrRegister(MY_NAME, MY_PASSWORD)
        val token = user.token
        val weboId = postWebo(token, "我是被爱的 webo！")
        likeWebo(token, weboId)
        val webo = getMyWebo(weboId, 1)
        Assertions.assertThat(webo.base.likes).isGreaterThan(0)
    }

    @Test
    fun testComment() {
        val user = getUserOrRegister(MY_NAME, MY_PASSWORD)
        val token = user.token
        val weboId = postWebo(token, "我是被评论的 webo！")
        comment(weboId, token, "我能评论木？")
        val comments = getComments(weboId)
        val myComment = comments.find { it.publisher?.username == MY_NAME && it.content == "我能评论木？" }
        Assert.assertNotNull(myComment)
        deleteMineWithToken("/comment/", token, mapOf("id" to myComment!!.id.toString()))
        val commentsAfterDelete = getComments(weboId)
        Assert.assertNull(commentsAfterDelete.find { it.id == myComment.id })
    }

    @Test
    fun testComments() {
        val user = getUserOrRegister(MY_NAME, MY_PASSWORD)
        val token = user.token
        val weboId = postWebo(token, "我是被评论的 webo！")
        (1 .. 14).forEach {
            comment(weboId, token, "我能评论木？$it")
        }
        val comments = getComments(weboId)
        comments.zip(14 downTo 4).forEach {
            Assert.assertEquals("我能评论木？${it.second}", it.first.content)
        }
        val nextTime = comments.last().publishTime
        val remainingComments = getComments(weboId, nextTime)
        remainingComments.zip(4 downTo 0).forEach {
            Assert.assertEquals("我能评论木？${it.second}", it.first.content)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testFollow() {
        val me = getUserOrRegister(MY_NAME, MY_PASSWORD)
        val wuBro = getUserOrRegister("hugefiver", "a123456;")
        val WU_ID = whoAmI(wuBro.token).id
        Assert.assertEquals(postMineWithToken("/user/follow", mapOf("to" to WU_ID), me.token).code, 0)
        val mine = getMine("/user/follow/all", mapOf("id" to 1))
        val data = mine.data as List<Map<String, *>>
        Assertions.assertThat(data).anyMatch { it["id"] == WU_ID }
        val wuData = getMine("/user/follow/all-by", mapOf("id" to WU_ID)).data as List<Map<String, *>>
        Assertions.assertThat(wuData).anyMatch { it["id"] == 1 }
        val webo = postWebo(wuBro.token, "大五哥哥天下第一！")
        val mWebos = getMyFollowWebos(1)
        Assertions.assertThat(mWebos).anyMatch { it.id == webo }
    }

    @Test
    fun testUserModify() {
        val me = getUserOrRegister(MY_NAME, MY_PASSWORD)
        postMineWithToken("/user/modify", mapOf("email" to "maruruku@stu.csust.edu.cn"), me.token)
        var after = whoAmI(me.token)
        Assert.assertEquals("maruruku@stu.csust.edu.cn", after.email)

        postMineWithToken("/user/modify", mapOf("nickname" to "大五", "bio" to "一个人。"), me.token)
        after = whoAmI(me.token)
        Assert.assertEquals("大五", after.nickname)
        Assert.assertEquals("maruruku@stu.csust.edu.cn", after.email)
        Assert.assertEquals("一个人。", after.bio)

        postMineWithToken("/user/modify", mapOf<String, String>(), me.token)
        after = whoAmI(me.token)
        Assert.assertEquals("大五", after.nickname)
        Assert.assertEquals("maruruku@stu.csust.edu.cn", after.email)
        Assert.assertEquals("一个人。", after.bio)

        postMineWithToken("/user/modify", mapOf("email" to "a@a.com", "nickname" to "小明"), me.token)
        after = whoAmI(me.token)
        Assert.assertEquals("小明", after.nickname)
        Assert.assertEquals("a@a.com", after.email)
    }

    @Test
    fun testChangePassword() {
        val name = "12j9123"
        val password = "asd12d132d"
        val me = getUserOrRegister(name, password)
        val resp = postMineWithToken("/user/change-password", mapOf("origin" to password, "changed" to "fuck it!;"), me.token)
        Assert.assertEquals(0, resp.code)
        val newToken = castResponse<Map<String, *>>(resp).data?.get("token")!! as String
        val after = postMineWithToken("/user/modify", mapOf("nickname" to "大五"), me.token)
        Assert.assertNotEquals(0, after.code)
        val resp2 = postMineWithToken("/user/change-password", mapOf("origin" to "fuck it!;", "changed" to password), newToken)
        Assert.assertEquals(0, resp2.code)
    }

    @Test
    fun testGetMyInfo() {
        val me = getUserOrRegister(MY_NAME, MY_PASSWORD)
        val mine = getMineWithToken("/user/me", me.token).parsed<WeboUserView>()
        Assert.assertEquals(0, mine.code)
        Assert.assertEquals(MY_NAME, mine.data!!.personal.username)
        val id = mine.data!!.personal.id

        val mine2 = getMine("/user", mapOf("id" to id)).parsed<WeboUserView>()
        Assert.assertEquals(0, mine2.code)
        Assert.assertEquals(MY_NAME, mine2.data!!.personal.username)
    }

    //
    //
    //
    //  Functions Below are support functions.
    //
    //
    //

    private inline fun <reified T> castResponse(raw: WeboResponse<*>) : WeboResponse<T> {
        val parsedData = mapper.convertValue(raw.data, T::class.java)!!
        return WeboResponse<T>(raw.code).apply {
            data = parsedData
            message = raw.message
        }
    }

    private inline fun <reified T> (WeboResponse<*>).parsed() = castResponse<T>(this)

    private fun whoAmI(token: String) : UserNameView {
        val resp = getMineWithToken("/whoami", token)
        Assert.assertEquals(0, resp.code)
        val user = castResponse<UserNameView>(resp).data
        return user!!
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMyWebos(token: String): List<WeboView> {
        val items = getMineWithToken("/post/mine", token).data  as Map<*, *>
        return makeWebos(items["webos"]  as List<Map<*, *>>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getMyFollowWebos(id: Int): List<WeboView> {
        val resp = getMine("/post/following", mapOf("id" to id))
        Assert.assertEquals(0, resp.code)
        val items = resp.data as Map<*, *>
        return makeWebos(items["webos"]  as List<Map<*, *>>)
    }

    private fun makeWebos(origin: Iterable<Map<*, *>>) : List<WeboView> {
        return origin.map { mapper.convertValue(it, WeboView::class.java) }.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun getComments(weboId: UUID, before: Instant = Instant.now()): List<CommentView> {
        val comments = getMine("/comment/all", mapOf("id" to weboId.toString(), "before" to before.toString()))
        Assertions.assertThat(comments.code).isEqualTo(0)
        val data = comments.data as List<Map<*, *>>
        return data.map { mapper.convertValue(it, CommentView::class.java) }
    }

    private fun comment(weboId: UUID, token: String, text: String) {
        val response = postMineWithToken("/comment/new", mapOf("id" to weboId, "text" to text), token)
        Assertions.assertThat(response.code).isEqualTo(0)
    }

    private fun getMyWebo(webo: UUID, myself: Int): WeboDetailedView {
        val resp = getMine("/post", mapOf("id" to webo.toString(), "userId" to myself.toString()))
        Assertions.assertThat(resp.code).isEqualTo(0)
        val data = resp.data as Map<*, *>
        return mapper.convertValue(data, WeboDetailedView::class.java)
    }


    private fun likeWebo(token: String, webo: UUID) {
        postMineWithToken("/post/like", mapOf("id" to webo.toString()), token)
    }

    private fun postWebo(token: String, text: String) : UUID {
        val webo = postMineWithToken("/post/new", mapOf("text" to text), token)
        Assertions.assertThat(webo.code).isEqualTo(0)
        val weboData = webo.data as Map<*, *>
        Assertions.assertThat(weboData["message"])
                .isNotNull.asString().isEqualTo(text)
        return UUID.fromString(weboData["id"] as String?)
    }

    private val base
        get() = "http://localhost:$port/"

    private fun postMine(apiPath: String, params: Map<String, String>): WeboResponse<*> {
        return rest!!.postForObject(base + apiPath
                , params
                , WeboResponse::class.java
        )
    }

    private fun getMine(apiPath: String, params: Map<String, *> = mapOf<String, String>()): WeboResponse<*> {
        val url = urlWithParams(apiPath, params)
        return rest!!.getForObject(
                url
                , WeboResponse::class.java
        )
    }

    private fun deleteMineWithToken(apiPath: String, token: String, params: Map<String, String> = mapOf()): WeboResponse<*> {
        val uri = urlWithParams(apiPath, params)
        val body = RequestEntity.delete(uri)
                .header(HttpHeaders.AUTHORIZATION, token)
                .build()
        return rest!!.exchange(body, WeboResponse::class.java).body!!
    }

    private fun getMineWithToken(apiPath: String, token: String, params: Map<String, *> = mapOf<String, String>()): WeboResponse<*> {
        val uri = urlWithParams(apiPath, params)
        val body = RequestEntity.get(uri)
                .header(HttpHeaders.AUTHORIZATION, token)
                .build()
        return rest!!.exchange(body, WeboResponse::class.java).body!!
    }

    private fun urlWithParams(apiPath: String, params: Map<String, *>): URI {
        val uri = URI.create(base + apiPath)
        val builder = UriComponentsBuilder.fromUri(uri)
        params.forEach { (t, u) -> builder.queryParam(t, u) }
        return builder.build().toUri()
    }

    private fun getUserOrRegister(username: String, password: String, nickname: String = username): TokenPair {
        return try {
            login(username, password)
        } catch (e: Throwable) {
            val response = postMine("/user/register"
                    , mapOf("username" to username, "password" to password, "nickname" to nickname))
            Assertions.assertThat(response.code).isEqualTo(Status.OK)
            val data = response.data as Map<*, *>
            Assertions.assertThat(data["token"]).isNotNull

            login(username, password)
        }
    }

    private fun login(username: String, password: String) : TokenPair {
        val login = postMine("/user/login", mapOf("username" to username, "password" to password))
        Assertions.assertThat(login.code).isEqualTo(Status.OK)
        val loginData = login.data as Map<*, *>
        Assertions.assertThat(loginData["token"]).isNotNull
        Assertions.assertThat(loginData["refreshToken"]).isNotNull
        return TokenPair(loginData["token"] as String, loginData["refreshToken"] as String)
    }

    private fun postMineWithToken(apiPath: String, params: Map<*, *>, token: String): WeboResponse<*> {
        val body = RequestEntity.post(URI.create(base + apiPath))
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(params)
        return rest!!.postForObject(base + apiPath, body, WeboResponse::class.java)
    }
}
