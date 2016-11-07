package edu.asu.msrs.artcelerationlibrary.tasks.schedule;

import android.os.Handler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * To make sure the first come request also has the first callback.
 * Created by Lei on 11/6/2016.
 */

public class FIFOThreadPool {
    private static FIFOThreadPool sFIFOThreadPool;
    private Queue<FIFOTask> requests = new LinkedList<>();
    private Set<FIFOTask> requestSet = new HashSet<>();
    private ThreadPool mThreadPool;
    // Used to post task from background thread to main thread.
    private Handler mHandler = new Handler();

    public static FIFOThreadPool getExecutor() {
        if (sFIFOThreadPool == null) {
            synchronized (FIFOThreadPool.class) {
                if (sFIFOThreadPool == null) {
                    sFIFOThreadPool = new FIFOThreadPool();
                }
            }
        }
        return sFIFOThreadPool;
    }

    private FIFOThreadPool() {
        mThreadPool = new ThreadPool();
    }

    public void execute(FIFOTask task) {
        requests.add(task);
        mThreadPool.execute(task);
    }

    // This function run on background thread.
    public void onTaskFinished(final FIFOTask task) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (requests.peek() == task) {
                    // The head task in the request queue is finished, remove it and callback
                    requests.poll();
                    task.callback();
                    callbackCompletedTasks();
                } else {
                    // The head task in the request queue is NOT finished, wait to callback in order.
                    requestSet.add(task);
                }
            }
        });

    }

    // Try to callback those tasks that were completed but didn't callback
    // due to waiting the head task to finish.
    private void callbackCompletedTasks() {
        while (requests.size() > 0) {
            if (requestSet.contains(requests.peek())) {
                FIFOTask task = requests.poll();
                requestSet.remove(task);
                task.callback();
            } else {
                // The head task in the queue is not finished.
                break;
            }
        }
    }

}
