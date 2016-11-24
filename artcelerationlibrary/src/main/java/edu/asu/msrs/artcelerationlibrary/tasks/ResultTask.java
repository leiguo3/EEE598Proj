package edu.asu.msrs.artcelerationlibrary.tasks;

import android.graphics.Bitmap;

import edu.asu.msrs.artcelerationlibrary.data.Result;
import edu.asu.msrs.artcelerationlibrary.tasks.schedule.FIFOTask;
import edu.asu.msrs.artcelerationlibrary.utils.ShareMemUtil;

/**
 * Created by Lei on 11/11/2016.
 * Create Bitmap from the Ashm and callback.
 */

public class ResultTask extends FIFOTask {
    private Result mResult;
    private Bitmap mBitmap;

    public ResultTask(Result result, ITaskCallback callback) {
        super(callback);
        mResult = result;
    }

    public Bitmap getBitmap(){
        return mBitmap;
    }

    @Override
    protected void runOnBackgroundThread() {
        if(mResult != null){
            final Result result = mResult;
            mBitmap = ShareMemUtil.createBitmapFromPfd(result.getParcelFileDescriptor(), result.getWidth(), result.getHeight());
        }
    }

}
