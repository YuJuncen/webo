package net.csust.webo.web

import net.csust.webo.services.jwt.JwtService
import net.csust.webo.services.user.UserService
import net.csust.webo.web.annotations.InjectUserInfo
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class WriteUser(val jwt: JwtService, val user: UserService): HandlerInterceptorAdapter() {
    companion object {
        const val USER = "user"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val handlerMethod = handler as? HandlerMethod
        if (handlerMethod?.hasMethodAnnotation(InjectUserInfo::class.java) != true) {
            return true
        }

        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
        val user = jwt.verify(token)
        request.setAttribute(USER, user)
        return true
    }
}