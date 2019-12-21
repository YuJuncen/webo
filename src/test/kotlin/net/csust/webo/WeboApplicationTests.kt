package net.csust.webo

import net.csust.webo.services.jwt.TokenPair
import net.csust.webo.web.response.WeboResponse
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import net.csust.webo.web.response.WeboResponse.Companion.Status
import org.opentest4j.AssertionFailedError
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import java.net.URI
import java.util.*

private const val MY_NAME = "maruruku@stu.csust.edu.cn"
private const val MY_PASSWORD = "a123456;"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WeboApplicationTests {
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

    private fun postMine(apiPath: String, params: Map<String, String>): WeboResponse<*> {
        return rest!!.postForObject("http://localhost:$port/$apiPath"
                , params
                , WeboResponse::class.java
        )
    }

    private fun getUserOrRegister(username: String, password: String, nickname: String = username): TokenPair {
        try {
            return login(username, password)
        } catch (e: AssertionFailedError) {
            val response = postMine("/user/register"
                    , mapOf("username" to username, "password" to password, "nickname" to nickname))
            Assertions.assertThat(response.code).isEqualTo(Status.OK)
            val data = response.data as Map<*, *>
            Assertions.assertThat(data["token"]).isNotNull

            return login(username, password)
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
        val body = RequestEntity.post(URI.create("http://localhost:$port/$apiPath"))
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(params)
        return rest!!.postForObject("http://localhost:$port/$apiPath", body, WeboResponse::class.java)
    }
}
