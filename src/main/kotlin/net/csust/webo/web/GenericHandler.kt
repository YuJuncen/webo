package net.csust.webo.web

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import net.csust.webo.web.exceptions.NoToken
import net.csust.webo.web.response.WeboResponse
import net.csust.webo.web.response.WeboResponse.Companion.Status
import net.csust.webo.web.response.WeboResponse.Companion.Status.makeResponseWith
import org.slf4j.LoggerFactory
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GenericHandler {
    companion object {
        val logger = LoggerFactory.getLogger(GenericHandler::class.java)

        fun (Exception).toJson() = mapOf<String, Any>(
                "exceptionMessage" to (this.message ?: "没有可用消息！"),
                "exceptionType" to this.javaClass.simpleName
        )
    }

    // NOTE: 这是临时解决方案。
    // 这个选项用于处理找不到 ID 的通用状况。
    @ExceptionHandler(KotlinNullPointerException::class)
    @ResponseBody
    fun handleNullPtr() : WeboResponse<*> {
        return Status.SERVER_ERROR.makeResponseWith("寻寻觅觅，冷冷清清，凄凄惨惨戚戚，您要找的东西，已经不见了。😭")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseBody
    fun handleFailedOnValidation(ex: MethodArgumentNotValidException): WeboResponse<*> {
        val result = ex.bindingResult
        val errors = result.fieldErrors
        val responseData = errors.map {
            mapOf("fieldName" to it.field, "rejectedValue" to it.rejectedValue, "message" to (it.defaultMessage ?: "我已经无话可说了。"))
        }
        return Status.BAD_REQUEST.makeResponseWith(responseData)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseBody
    fun handleHttpUnreadableException(ex: HttpMessageNotReadableException, req: WebRequest): WeboResponse<Map<String, Any>> {
        return Status.BAD_REQUEST.makeResponseWith(ex.toJson())
    }

    @ExceptionHandler(JWTVerificationException::class)
    @ResponseBody
    fun handleJwtVerificationException(ex: JWTVerificationException, req: WebRequest): WeboResponse<Map<String, Any>> {
        return Status.AUTH_FAILED.makeResponseWith(ex.toJson())
    }

    @ExceptionHandler(TokenExpiredException::class)
    @ResponseBody
    fun handleExpire() = Status.TOKEN_EXPIRED.makeResponseWith(mapOf("hint" to "如题，您应该试试看刷新一下令牌了。"))

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleAnyException(ex: Exception, req: WebRequest): WeboResponse<Map<String, Any>> {
        logger.warn("Unexpected Exception <<$ex>> Catch, Some Stack Info: ")
        ex.stackTrace.take(5)
                .forEach { logger.debug(it.toString()) }
        return Status.SERVER_ERROR.makeResponseWith(ex.toJson())
    }

    @ExceptionHandler(NoToken::class)
    @ResponseBody
    fun handleNoToken() = Status.NOT_AUTH.makeResponseWith(Unit)
}