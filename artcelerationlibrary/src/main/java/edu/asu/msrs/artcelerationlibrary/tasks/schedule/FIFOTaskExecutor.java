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

public class FIFOTaskExecutor {
    private static FIFOTaskExecutor sFIFOTaskExecutor;
    private Queue<FIFOTask> requests = new LinkedList<>();
    private Set<FIFOTask> requestSet = new HashSet<>();
    private ThreadPool mThreadPool;
    // Used to post task from background thread to the thread on which the FIFOTaskExecutor instance is created.
    // We operate the task Queue on a single thread. It can be the Main thread or any other thread which has a Looper.
    private Handler mHandler = new Handler();

    public static FIFOTaskExecutor getExecutor() {
        if (sFIFOTaskExecutor == null) {
            synchronized (FIFOTaskExecutor.class) {
                if (sFIFOTaskExecutor == null) {
                    sFIFOTaskExecutor = new FIFOTaskExecutor();
                }
            }
        }
        return sFIFOTaskExecutor;
    }

    private FIFOTaskExecutor() {
        mThreadPool = new ThreadPool();
    }

    public void execute(FIFOTask task) {
        requests.add(task);
        mThreadPool.execute(task);
    }

    // This function run on background thread.
    public void onTaskFinished(final FIFOTask task) {
        // post to the thread on which we operate the task Queue.
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
