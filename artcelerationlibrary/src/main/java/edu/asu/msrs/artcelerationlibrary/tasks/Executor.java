package edu.asu.msrs.artcelerationlibrary.tasks;

import android.graphics.Bitmap;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.data.Result;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTaskExecutor;

/**
 * Created by Lei on 11/6/2016.
 * This class is used to execute FIFO task conveniently. - YZ
 */

public class Executor {

    /**
     * execute the transform tasks
     * @param request
     * @param callback
     */
    public static void performTransform(Request request, ITaskCallback callback) {
        TransformTask task = new TransformTask(request, callback);
        execute(task);
    }

    /**
     * execute the create request tasks
     */
    public static void createRequest(Bitmap bmp, int index, int[] intArgs, float[] floatArgs, ITaskCallback callback){
        RequestTask task = new RequestTask(bmp, index, intArgs, floatArgs, callback);
        execute(task);
    }

    /**
     * execute the create bitmap tasks
     */
    public static void createBitmap(Result result, ITaskCallback callback){
        ResultTask task = new ResultTask(result, callback);
        execute(task);
    }

    /**
     * The first call of this function must happens on a thread which contains a Looper,
     * because we will create a Handler at the first call.
     * @param task
     */
    private static void execute(FIFOTask task) {
        FIFOTaskExecutor.getExecutor().execute(task);
    }

}
