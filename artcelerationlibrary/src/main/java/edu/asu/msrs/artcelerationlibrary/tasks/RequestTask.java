package edu.asu.msrs.artcelerationlibrary.tasks;

import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;
import edu.asu.msrs.artcelerationlibrary.utils.ShareMemUtil;

/**
 * Created by Lei on 11/11/2016.
 * Write Bitmap pixels data into the Ashm
 */

public class RequestTask extends FIFOTask {
    private Bitmap mBitmap;
    private int mTransformType;
    private int[] mIntArgs;
    private float[] mFloatArgs;
    private Request mRequest;

    public RequestTask(Bitmap bmp, int index, int[] intArgs, float[] floatArgs, ITaskCallback callback) {
        super(callback);
        mBitmap = bmp;
        mTransformType = index;
        mIntArgs = intArgs;
        mFloatArgs = floatArgs;
    }

    public Request getRequest(){
        return mRequest;
    }

    @Override
    protected void runOnBackgroundThread() {
        try {
            ParcelFileDescriptor pfd = ShareMemUtil.writeBitmapToAshm(mBitmap);
            createRequest(pfd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRequest(ParcelFileDescriptor pfd){
        Request request = new Request();
        request.setTransformType(mTransformType);
        request.setParcelFileDescriptor(pfd);
        request.setWidth(mBitmap.getWidth());
        request.setHeight(mBitmap.getHeight());
        request.setIntArgs(mIntArgs);
        request.setFloatArgs(mFloatArgs);
        mRequest = request;
    }

}
