package com.union_test.toutiao.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件名称: ThreadUtils.java
 * 功能描述:
 *  这里只是线程池示例代码，不建议媒体直接使用
 * @author: bytedance
 * 创建时间: 9/26/24
 * Copyright (C) 2024 bytedance
 */
public class ThreadUtils {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(CPU_COUNT, 5); // 至少允许5个线程
    private static final int THREAD_KEEP_LIVE_TIME = 30; // 线程如果30秒不用，允许超时
    private static final int TASK_QUEUE_MAX_COUNT = 128;

    private static ThreadPoolExecutor mThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE,
                                                                                   CORE_POOL_SIZE,
                                                                                   THREAD_KEEP_LIVE_TIME,
                                                                                   TimeUnit.SECONDS,
                                                                                   new LinkedBlockingQueue<Runnable>(TASK_QUEUE_MAX_COUNT));

    public static Handler mMainHandler = new Handler(Looper.getMainLooper());


    /**
     * 在子线程中运行任务
     *
     * @param runnable
     */
    public static void runOnThreadPool(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    /**
     * 在主线程中运行任务
     *
     * @param runnable
     */
    public static void runOnUIThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
        }
    }

    /**
     * 在子线程中切主线程运行任务（切两次线程，先切子线程，再在子线程中切主线程）
     *
     * @param runnable
     */
    public static void runOnUIThreadByThreadPool(Runnable runnable) {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mMainHandler.post(runnable);
            }
        });
    }

    /**
     * 在子线程中运行任务
     *
     * @param callable
     * @return
     */
    public static<T> Future<T> runOnThreadPool(Callable<T> callable) {
        return mThreadPoolExecutor.submit(callable);
    }
}
