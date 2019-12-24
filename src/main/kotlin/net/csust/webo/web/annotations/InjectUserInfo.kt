package net.csust.webo.web.annotations

/**
 * 这个注解和 WriteUser 拦截器同用。
 *
 * WriteUser 发现某个 HandlerMethod 含有这个注解之后，
 * 会将封存在令牌中的用户信息注入 RequestAttribute 中。
 *
 * ### 包括 ：
 * - userId: Int
 * - user: User(现在不推荐使用！)
 *
 * @see net.csust.webo.web.WriteUser
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectUserInfo