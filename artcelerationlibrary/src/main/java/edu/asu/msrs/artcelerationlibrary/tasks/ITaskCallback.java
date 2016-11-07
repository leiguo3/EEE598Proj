package edu.asu.msrs.artcelerationlibrary.tasks;

import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;

/**
 * Created by Lei on 11/6/2016.
 */

public interface ITaskCallback<T extends FIFOTask> {
    public void onTaskFinished(T task);
}
