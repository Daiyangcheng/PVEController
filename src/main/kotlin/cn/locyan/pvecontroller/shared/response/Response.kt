package cn.locyan.pvecontroller.shared.response

/**
 * @param status 状态
 * @param message 返回消息
 * @param data 返回数据体
 */
data class Response(
    val status: Int,
    val message: String,
    val data: Any?
)