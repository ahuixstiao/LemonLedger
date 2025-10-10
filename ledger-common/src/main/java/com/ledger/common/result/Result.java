package com.ledger.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: ahui
 * @Description: TODO
 * @DateTime: 2025/10/2 - 21:44
 **/
@Data
@AllArgsConstructor
public class Result<T> {

    private static final Integer SUCCESS_STATUS_CODE = 200;
    private static final String SUCCESS_MESSAGE = "成功";
    private static final Integer FAILURE_STATUS_CODE = 500;
    private static final String FAILURE_MESSAGE = "错误, 请联系管理员";

    private Integer status;   // 状态码
    private String message; // 信息
    private T data;         // 数据

    /**
     * 无参构造
     */
    public Result() {
    }

    /**
     * 有参构造
     * @param status  返回状态码
     * @param message 返回信息
     */
    private Result(Integer status, String message) {
        this.status = status;
        this.message = message;
    }


    /**
     * 执行成功，无返回数据
     * @return Object
     */
    public static <T> Result<T> ok() {
        return new Result<>(SUCCESS_STATUS_CODE, SUCCESS_MESSAGE);
    }

    /**
     * 执行成功 有返回数据
     * @param data 返回数据
     * @return Object
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS_STATUS_CODE, SUCCESS_MESSAGE, data);
    }

    /**
     * 执行成功 有返回数据和自定义消息
     * @param message 返回消息
     * @param data 返回数据
     * @return Object
     */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(SUCCESS_STATUS_CODE, message, data);
    }


    //失败,无返回数据
    public static <T> Result<T> fail() {
        return new Result<>(FAILURE_STATUS_CODE, FAILURE_MESSAGE);
    }

    // 失败 有返回信息
    public static <T> Result<T> fail(String message) {
        return new Result<>(FAILURE_STATUS_CODE, message);
    }



}
