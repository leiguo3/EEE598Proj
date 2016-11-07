package edu.asu.msrs.artcelerationlibrary;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.data.Result;
import edu.asu.msrs.artcelerationlibrary.service.ArtService;
import edu.asu.msrs.artcelerationlibrary.utils.MemoryFileUtil;
import edu.asu.msrs.artcelerationlibrary.utils.ParamsVerifyUtil;
import edu.asu.msrs.artcelerationlibrary.utils.ShareMemUtil;

/**
 * Created by Lei on 11/3/2016.
 */

public class ArtLibImpl {
    private final String TAG = "ArtLibImpl";
    private TransformHandler mArtlistener;
    private String[] mTransforms = {"Gaussian Blur", "Neon edges", "Color Filter"};
    private boolean mBound = false;
    private Context mContext;
    private Messenger mRequestMessenger;

    ArtLibImpl(Context context){
        mContext = context.getApplicationContext();
        bindService();
    }

    public String[] getTransformsArray(){
        return mTransforms;
    }

    public TransformTest[] getTestsArray(){
        TransformTest[] transforms = new TransformTest[3];
        transforms[0]=new TransformTest(0, new int[]{1,2,3}, new float[]{0.1f, 0.2f, 0.3f});
        transforms[1]=new TransformTest(1, new int[]{11,22,33}, new float[]{0.3f, 0.2f, 0.3f});
        transforms[2]=new TransformTest(2,
                new int[]{33, 25, 208, 72, 231, 140, 233, 162, 41,
                37, 245, 124, 247, 205, 248, 245, 108, 10, 203, 168, 234,
                217, 245, 239,}, new float[]{0.5f, 0.6f, 0.3f});
        return transforms;
    }

    public void registerHandler(TransformHandler artlistener){
        this.mArtlistener=artlistener;
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs){
        if(img == null || img.isRecycled()){
            return false;
        }
        if(index < 0 || index > mTransforms.length - 1){
            return false;
        }
        if(!verifyArgs(index, intArgs, floatArgs)){
            return false;
        }
        sendRequest(img, index, intArgs,floatArgs);
        return true;
    }

    private boolean verifyArgs(int type, int[] intArgs, float[] floatArgs){
        switch (type){
            case ArtService.COLOR_FILTER:
                return ParamsVerifyUtil.verifyColorFilterParams(intArgs);
            case ArtService.GAUSSIAN_BLUR:
                break;
            case ArtService.NEON_EDGES:
                break;
            default:
                return false;
        }
        return true;
    }

    private void sendRequest(final Bitmap img, final int index, final int[] intArgs, final float[] floatArgs){
        // TODO: make sure messages send in order.
        // TODO: Use thread pool to improve performance.
        new Thread(){
            @Override
            public void run() {
                ParcelFileDescriptor pfd = writeImageToShareMem(img);
                Log.d(TAG, "ParcelFileDescriptor: " + pfd.toString());
                sendMessage(pfd, img.getWidth(), img.getHeight(), index, intArgs, floatArgs);
            }
        }.start();
    }

    private ParcelFileDescriptor writeImageToShareMem(Bitmap img){
        try {
            // TODO: remove test code
            int byteCount = img.getByteCount();
            Log.d(TAG, "image byte count: " + byteCount);
            // Above code for test

            byte[] imgData = ShareMemUtil.getBytes(img);
            int size = imgData.length;
            MemoryFile mf = new MemoryFile("ashm", size);
            mf.writeBytes(imgData, 0, 0, size);
            return MemoryFileUtil.getParcelFileDescriptor(mf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(ParcelFileDescriptor pfd, int width, int height, int index, int[] intArgs, float[] floatArgs){
        if(mBound){
            Message msg = Message.obtain(null, ArtService.TRANSFORM_REQUEST, 0, 0);
            Request request = new Request();
            request.setTransformType(index);
            request.setParcelFileDescriptor(pfd);
            request.setWidth(width);
            request.setHeight(height);
            request.setIntArgs(intArgs);
            request.setFloatArgs(floatArgs);
            msg.setData(request.writeToBundle());
            try {
                mRequestMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            // TODO: what if the service is not connected here?
        }
    }

    private Handler mCallbackHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //TODO: handle callback
            Result result = Result.create(msg.getData());
        }
    };
    private Messenger mCallbackMessenger = new Messenger(mCallbackHandler);

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

    private void bindService(){
        Intent intent = new Intent(mContext, ArtService.class);
        mContext.bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * Pass a callback Messenger to the Service, so it can call the client when the transform is finished.
     */
    private void passCallbackMessenger(){
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
