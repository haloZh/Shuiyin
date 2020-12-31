package com.haopeng.shuiyin.http.retrofit;

import com.haopeng.shuiyin.http.bean.ResponseBean;
import com.haopeng.shuiyin.http.entity.HttpData;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface HttpService {
    @GET("http://kyzhiban.cn/dy/url")
    public Call<String> getTargetUrl(@QueryMap Map<String, String> params);
}
