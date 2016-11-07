package edu.asu.msrs.artcelerationlibrary.tasks.schedule;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lei on 11/6/2016.
 */

public class ThreadPool {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE_TIME = 30; // seconds
    private Executor mExecutor;
    private final BlockingQueue<Runnable> mPoolWorkQueue = new LinkedBlockingQueue<>(50);

    ThreadPool() {
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, mPoolWorkQueue);
    }

    public void execute(Runnable runnable) {
        mExecutor.execute(runnable);
    }

}
