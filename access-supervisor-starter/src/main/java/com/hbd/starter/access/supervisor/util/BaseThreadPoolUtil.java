package com.hbd.starter.access.supervisor.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池工具
 */
public class BaseThreadPoolUtil {

    private static ExecutorService executorService;

    public static ExecutorService getInstance() {
        if(executorService == null) {
            synchronized (BaseThreadPoolUtil.class) {
                if(executorService == null) {
                    executorService = Executors.newFixedThreadPool(10);
                }
            }
        }
        return executorService;
    }

    public static void run(Runnable runnable) {
        getInstance().execute(runnable);
    }

}
