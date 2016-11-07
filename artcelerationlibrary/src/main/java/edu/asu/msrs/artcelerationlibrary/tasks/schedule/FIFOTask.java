package edu.asu.msrs.artcelerationlibrary.tasks.schedule;

import edu.asu.msrs.artcelerationlibrary.tasks.ITaskCallback;

/**
 * Created by Lei on 11/6/2016.
 */

public class FIFOTask implements Runnable{
    private ITaskCallback mTaskCallback;

    public FIFOTask(ITaskCallback callback){
        mTaskCallback = callback;
    }

    @Override
    final public void run() {
        runOnBackgroundThread();
        FIFOThreadPool.getExecutor().onTaskFinished(this);
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
