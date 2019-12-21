package net.csust.webo

import com.fasterxml.jackson.databind.ObjectMapper
import net.csust.webo.services.jwt.TokenPair
import net.csust.webo.services.webo.CommentView
import net.csust.webo.services.webo.WeboView
import net.csust.webo.web.response.WeboResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import net.csust.webo.web.response.WeboResponse.Companion.Status
import org.junit.Assert
import org.opentest4j.AssertionFailedError
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.mock.http.server.reactive.MockServerHttpRequest.post
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Instant
import java.util.*

private const val MY_NAME = "maruruku@stu.csust.edu.cn"
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
        val weboId = postWebo(token, "我能发帖木？");
        println(weboId)
    }

    @Test
    fun testLikeWebo() {
        val user = getUserOrRegister(MY_NAME, MY_PASSWORD)
        val token = user.token
        val weboId = postWebo(token, "我是被爱的 webo！")
        likeWebo(token, weboId)
        val webo = getMyWebo(weboId, 1)
        Assertions.assertThat(webo.likes).isGreaterThan(0)
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
        (1 .. 15).forEach {
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

    //
    //
    //
    //  Functions Below are support functions.
    //
    //
    //

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

    private fun getMyWebo(webo: UUID, myself: Int): WeboView {
        val resp = getMine("/post", mapOf("id" to webo.toString(), "userId" to myself.toString()))
        Assertions.assertThat(resp.code).isEqualTo(0)
        val data = resp.data as Map<*, *>
        return mapper.convertValue(data, WeboView::class.java)
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

    private fun getMine(apiPath: String, params: Map<String, String> = mapOf()): WeboResponse<*> {
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

    private fun urlWithParams(apiPath: String, params: Map<String, String>): URI {
        val uri = URI.create(base + apiPath)
        val builder = UriComponentsBuilder.fromUri(uri)
        params.forEach { (t, u) -> builder.queryParam(t, u) }
        return builder.build().toUri()
    }

    private fun getUserOrRegister(username: String, password: String, nickname: String = username): TokenPair {
        return try {
            login(username, password)
        } catch (e: AssertionFailedError) {
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
