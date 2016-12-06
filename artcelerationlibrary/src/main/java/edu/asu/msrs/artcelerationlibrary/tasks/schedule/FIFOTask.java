package edu.asu.msrs.artcelerationlibrary.tasks.schedule;

import edu.asu.msrs.artcelerationlibrary.tasks.ITaskCallback;

/**
 * Created by Lei on 11/6/2016.
 * Use multi-thread tasks processing and make sure the first come request has first callback. - YZ
 */

public class FIFOTask implements Runnable{
    private ITaskCallback mTaskCallback;

    public FIFOTask(ITaskCallback callback){
        mTaskCallback = callback;
    }

    @Override
    final public void run() {
        runOnBackgroundThread();
        FIFOTaskExecutor.getExecutor().onTaskFinished(this);
    }

    protected void runOnBackgroundThread(){
    }

    public void setCallback(ITaskCallback callback){
        mTaskCallback = callback;
    }

    public void callback(){
        if(mTaskCallback != null){
            mTaskCallback.onTaskFinished(this);
        }
    }
}
