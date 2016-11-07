package edu.asu.msrs.artcelerationlibrary.tasks;

import android.util.Log;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.service.ArtService;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;

/**
 * Created by Lei on 11/6/2016.
 */

public class TransformTask extends FIFOTask {
    private static final String TAG = "TransformTask";
    private Request mRequest;

    TransformTask(Request request, ITaskCallback callback){
        super(callback);
        mRequest = request;
    }

    public Request getRequest(){
        return mRequest;
    }

    @Override
    protected void runOnBackgroundThread() {
        if(mRequest != null){
            handleRequest(mRequest);
        }else{
            Log.e(TAG, "mRequest is null!");
        }
    }

    // This function run on background thread.
    private void handleRequest(Request request){
//        parseBitmap(request);
        switch (request.getTransformType()){
            case ArtService.GAUSSIAN_BLUR:

                break;
            case ArtService.NEON_EDGES:

                break;
            case ArtService.COLOR_FILTER:

                break;
        }
    }

}
