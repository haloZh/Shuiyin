package com.haopeng.shuiyin.http.Converter;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;


/**
 * Created by zhangcb on 17/5/6.
 */

public class GsonResponseBodyConverter<T> implements Converter<ResponseBody,T> {
    private final Gson gson;
    private final Type type;

    GsonResponseBodyConverter(Gson gson, Type type){
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        String response = value.string();
        Log.i("response","response:"+response);
        if(!(response.toString().startsWith("{")&&response.toString().endsWith("}"))) {
            return (T) response.toString();
        }
//        Logu.i(Thread.currentThread().getId()+","+ Thread.currentThread().getName());

//        HttpData httpData = gson.fromJson(response, HttpData.class);
//        if (httpData != null) {
//            if (httpData.getCode() != HttpData.SUCCESS) {
//                throw new ApiException(httpData.getCode(), httpData.getMessage());
//            }
//        } else {
//            Logu.e("返回数据为空");
//            throw new NullPointerException();
//
//        }
        return gson.fromJson(response, type);
    }
}
