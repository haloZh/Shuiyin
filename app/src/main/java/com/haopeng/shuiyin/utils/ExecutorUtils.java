package com.haopeng.shuiyin.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorUtils {
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void excute(Runnable runnable){
        executorService.execute(runnable);
    }
}
