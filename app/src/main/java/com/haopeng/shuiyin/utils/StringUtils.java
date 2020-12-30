package com.haopeng.shuiyin.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    //判断是否为空
    public static boolean isValid(String s){
        if(s == null || s.isEmpty()){
            return false;
        }
        return true;
    }


    /**
     * 判断字符串是否为URL
     *
     * @param urls 需要判断的String类型url
     * @return true:是URL；false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        boolean isurl = false;
        if (urls.startsWith("http")) {
            isurl = true;
        }
        return isurl;
    }
}
