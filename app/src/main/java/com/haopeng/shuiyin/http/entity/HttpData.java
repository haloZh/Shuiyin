package com.haopeng.shuiyin.http.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangcb on 17/5/6.
 */

public class HttpData<T> {
    public static final int SUCCESS = 200;
    private int code;
    @SerializedName("msg")
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
