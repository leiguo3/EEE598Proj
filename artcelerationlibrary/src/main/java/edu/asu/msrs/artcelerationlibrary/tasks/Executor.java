package edu.asu.msrs.artcelerationlibrary.tasks;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOThreadPool;

/**
 * Created by Lei on 11/6/2016.
 */

public class Executor {

    public static void performTransform(Request request, ITaskCallback callback) {
        TransformTask task = new TransformTask(request, callback);
        execute(task);
    }

    private static void execute(FIFOTask task) {
        FIFOThreadPool.getExecutor().execute(task);
    }

}
