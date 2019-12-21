package net.csust.webo.web.response

class WeboResponse<T>(val code : Int) {
    var message: String = ""
    var data : T? = null

    companion object {
        object Status {
            const val OK = 0;
            const val BAD_REQUEST = 1;
            const val NOT_AUTH = 2;
            const val AUTH_FAILED = 3;
            const val SERVER_ERROR = 9;

            val messageMap = mapOf(
                    OK to "成功",
                    BAD_REQUEST to "请求有误",
                    NOT_AUTH to "没有验明身份",
                    AUTH_FAILED to "身份无效",
                    SERVER_ERROR to "服务器异常"
            )

            fun <T> (Int).makeResponseWith(data: T?) : WeboResponse<T> {
                val resp = WeboResponse<T>(this);
                resp.message = messageMap[this] ?: "";
                resp.data = data;
                return resp;
            }

            fun <T> (T).response() = OK.makeResponseWith(this);
        }
    }
}