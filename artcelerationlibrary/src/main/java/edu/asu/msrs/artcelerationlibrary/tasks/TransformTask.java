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
 * This is a subclass of FIFO Task which does the transform task. - YZ
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
        byte[] pixels =ShareMemUtil.getBytesFromPfd(request.getParcelFileDescriptor()).array();
        byte[] newPixels = null;
        switch (request.getTransformType()){
            case ArtService.GAUSSIAN_BLUR:
                newPixels = Transform.gaussianBlur(pixels, request.getWidth(), request.getHeight(), request.getIntArgs(), request.getFloatArgs());
                break;
            case ArtService.ASCII_ART:
                newPixels = Transform.asciiArt(pixels, request.getWidth(), request.getHeight());
                break;
            case ArtService.COLOR_FILTER:
                newPixels = Transform.colorFilter(pixels, request.getIntArgs());
                break;
            case ArtService.MOTION_BLUR:
                newPixels = Transform.motionBlur(pixels, request.getWidth(), request.getHeight(), request.getIntArgs());
                break;
        }
        ParcelFileDescriptor pfd = ShareMemUtil.writeDataToAshm(newPixels);
        request.setParcelFileDescriptor(pfd);
    }

}
