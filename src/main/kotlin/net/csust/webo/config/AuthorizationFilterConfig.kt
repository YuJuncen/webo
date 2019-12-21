package net.csust.webo.config

import net.csust.webo.web.WriteUser
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class AuthorizationFilterConfig(val userFilter: WriteUser) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
                .addInterceptor(userFilter)
                .addPathPatterns("/**")
    }
}