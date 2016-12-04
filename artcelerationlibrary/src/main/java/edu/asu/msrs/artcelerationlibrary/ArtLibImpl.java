package edu.asu.msrs.artcelerationlibrary;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.data.Result;
import edu.asu.msrs.artcelerationlibrary.service.ArtService;
import edu.asu.msrs.artcelerationlibrary.tasks.Executor;
import edu.asu.msrs.artcelerationlibrary.tasks.ITaskCallback;
import edu.asu.msrs.artcelerationlibrary.tasks.RequestTask;
import edu.asu.msrs.artcelerationlibrary.tasks.ResultTask;
import edu.asu.msrs.artcelerationlibrary.utils.ParamsVerifyUtil;

/**
 * Created by Lei on 11/3/2016.
 */

public class ArtLibImpl {
    private final String TAG = "ArtLibImpl";
    private TransformHandler mArtlistener;
    private String[] mTransforms = {"Gaussian Blur", "Ascii Art", "Color Filter", "Motion Blur"};
    private boolean mBound = false;
    private Context mContext;
    private Messenger mRequestMessenger;

    ArtLibImpl(Context context) {
        mContext = context.getApplicationContext();
        bindService();
    }

    public String[] getTransformsArray() {
        return mTransforms;
    }

    public TransformTest[] getTestsArray() {
        TransformTest[] transforms = new TransformTest[4];
        transforms[0] = new TransformTest(0, new int[]{10}, new float[]{6.0f});
        transforms[1] = new TransformTest(1, new int[]{11, 22, 33}, new float[]{0.3f, 0.2f, 0.3f});
        transforms[2] = new TransformTest(2,
                new int[]{33, 25, 208, 72, 231, 140, 233, 162, 41,
                        37, 245, 124, 247, 205, 248, 245, 108, 10, 203, 168, 234,
                        217, 245, 239,}, new float[]{0.5f, 0.6f, 0.3f});
        transforms[3] = new TransformTest(3, new int[]{0, 10}, new float[]{});
        return transforms;
    }

    public void registerHandler(TransformHandler artlistener) {
        this.mArtlistener = artlistener;
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs) {
        if (img == null || img.isRecycled()) {
            return false;
        }
        if (index < 0 || index > mTransforms.length - 1) {
            return false;
        }
        if (!verifyArgs(index, intArgs, floatArgs)) {
            return false;
        }
        sendRequest(img, index, intArgs, floatArgs);
        return true;
    }

    private boolean verifyArgs(int type, int[] intArgs, float[] floatArgs) {
        switch (type) {
            case ArtService.COLOR_FILTER:
                return ParamsVerifyUtil.verifyColorFilterParams(intArgs);
            case ArtService.GAUSSIAN_BLUR:
                // TODO: verify Gaussian blur params here
                break;
            case ArtService.ASCII_ART:
                // No need to verify params here.
                // Ascii Art only use the Bitmap and the transform index which are already verified.
                break;
            case ArtService.MOTION_BLUR:
                return ParamsVerifyUtil.verifyMotionBlurParams(intArgs);
            default:
                return false;
        }
        return true;
    }

    private void sendRequest(Bitmap img, int index, int[] intArgs, float[] floatArgs) {
        // Copy pixels from Bitmap to Ashm is time consuming, do this on the background threads.
        // When the work is done, will callback with a RequestTask object which contains a Request object.
        // Use the Request object to send message to service.
        Executor.createRequest(img, index, intArgs, floatArgs, mCreateRequestCallback);
    }

    private ITaskCallback mCreateRequestCallback = new ITaskCallback<RequestTask>() {
        @Override
        public void onTaskFinished(RequestTask task) {
            Request request = task.getRequest();
            if (request != null) {
                sendMessage(request);
            } else {
                Log.e(TAG, "Create request error!");
            }
        }
    };

    private void sendMessage(Request request) {
        if (mBound) {
            Message msg = Message.obtain(null, ArtService.TRANSFORM_REQUEST, 0, 0);
            msg.setData(request.writeToBundle());
            try {
                mRequestMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: what if the service is not connected here?
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRequestMessenger = new Messenger(service);
            mBound = true;
            passCallbackMessenger();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRequestMessenger = null;
            mBound = false;
        }
    };

    private void bindService() {
        Intent intent = new Intent(mContext, ArtService.class);
        mContext.bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
    }

    // Create Bitmap from Result on multi-threads.
    private ITaskCallback mCreateBitmapCallback = new ITaskCallback<ResultTask>() {
        @Override
        public void onTaskFinished(ResultTask task) {
            Bitmap bmp = task.getBitmap();
            callbackClient(bmp);
        }
    };

    private void callbackClient(Bitmap bmp){
        if (mArtlistener != null && bmp != null && !bmp.isRecycled()) {
            mArtlistener.onTransformProcessed(bmp);
        } else {
            Log.e(TAG, "Error found, no callback!");
        }
    }

    private Messenger mCallbackMessenger = new Messenger(new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Result result = Result.create(msg.getData());
            Executor.createBitmap(result, mCreateBitmapCallback);
        }
    });

    /**
     * Pass a callback Messenger to the Service, so it can call the client when the transform is finished.
     */
    private void passCallbackMessenger() {
        Message msg = Message.obtain(null, ArtService.PASS_CALLBACK_MESSENGER, 0, 0);
        // Support after android.os.Build.VERSION_CODES#FROYO release
        msg.obj = mCallbackMessenger;
        try {
            mRequestMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
