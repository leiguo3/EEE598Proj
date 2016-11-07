package edu.asu.msrs.artcelerationlibrary.tasks;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.service.ArtService;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;
import edu.asu.msrs.artcelerationlibrary.utils.ShareMemUtil;
import edu.asu.msrs.artcelerationlibrary.graphics.Transform;

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
        Log.d(TAG, "runOnBackgroundThread is called!");
        if(mRequest != null){
            try{
                handleRequest(mRequest);
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Log.e(TAG, "mRequest is null!");
        }
    }

    // This function run on background thread.
    private void handleRequest(Request request) throws IOException{
        Log.d(TAG, "Request type: " + request.getTransformType());
        byte[] pixels =ShareMemUtil.getBytesFromPfd(request.getParcelFileDescriptor()).array();
        byte[] newPixels = null;
        switch (request.getTransformType()){
            case ArtService.GAUSSIAN_BLUR:
                // TODO: add transform
                // Do nothing
                newPixels = pixels;
                break;
            case ArtService.NEON_EDGES:
                // TODO: add transform
                // Do nothing
                newPixels = pixels;
                break;
            case ArtService.COLOR_FILTER:
                newPixels = Transform.colorFilter(request.getIntArgs(), pixels);
                break;
        }
        ParcelFileDescriptor pfd = ShareMemUtil.writeDataToAshm(newPixels);
        request.setParcelFileDescriptor(pfd);
    }

}
