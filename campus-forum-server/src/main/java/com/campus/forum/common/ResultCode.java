package com.campus.forum.common;

/**
 * 响应状态码枚举
 */
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 服务端错误 5xx
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USER_EXISTS(1004, "用户已存在"),
    USER_NOT_VERIFIED(1005, "用户未实名认证"),

    TOKEN_EXPIRED(1101, "登录已过期，请重新登录"),
    TOKEN_INVALID(1102, "无效的登录凭证"),

    PARAM_ERROR(1201, "参数校验失败"),
    PARAM_MISSING(1202, "缺少必要参数"),
    REPEAT_OPERATION(1203, "重复操作，请勿重复提交"),

    FILE_UPLOAD_ERROR(1301, "文件上传失败"),
    FILE_TYPE_ERROR(1302, "文件类型不支持"),
    FILE_SIZE_ERROR(1303, "文件大小超出限制"),

    POST_NOT_FOUND(1401, "帖子不存在"),
    POST_AUDIT_PENDING(1402, "帖子审核中"),
    POST_AUDIT_REJECTED(1403, "帖子审核未通过"),
    CONTENT_AUDIT_BLOCKED(1404, "内容包含违规信息，无法发布"),

    PRODUCT_NOT_FOUND(1501, "商品不存在"),
    PRODUCT_SOLD_OUT(1502, "商品已售出"),
    PRODUCT_ORDER_NOT_FOUND(1503, "订单不存在"),
    PRODUCT_ORDER_STATUS_ERROR(1504, "订单状态不允许当前操作"),

    ACTIVITY_NOT_FOUND(1601, "活动不存在"),
    ACTIVITY_FULL(1602, "活动报名人数已满"),
    ACTIVITY_EXPIRED(1603, "活动报名已截止"),

    HELP_NOT_FOUND(1701, "互助请求不存在"),
    HELP_ALREADY_TAKEN(1702, "该请求已被接单"),

    WX_LOGIN_ERROR(1801, "微信登录失败"),
    WX_CODE_INVALID(1802, "微信授权码无效");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
