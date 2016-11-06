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

import java.io.IOException;
import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.data.Result;
import edu.asu.msrs.artcelerationlibrary.service.ArtService;
import edu.asu.msrs.artcelerationlibrary.utils.MemoryFileUtil;
import edu.asu.msrs.artcelerationlibrary.utils.ShareMemUtil;

/**
 * Created by Lei on 11/3/2016.
 */

public class ArtLibImpl {
//    public static final String KEY_CALLBACK_MESSENGER = "k_callback_messenger";
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
        transforms[2]=new TransformTest(2, new int[]{51,42,33}, new float[]{0.5f, 0.6f, 0.3f});

        return transforms;
    }

    public void registerHandler(TransformHandler artlistener){
        this.mArtlistener=artlistener;
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs){
        //TODO: data validattion
        ParcelFileDescriptor pfd = writeImageToShareMem(img);
        //TODO: data validattion
        sendMessage(pfd, index, intArgs, floatArgs);
        return true;
    }

    private ParcelFileDescriptor writeImageToShareMem(Bitmap img){
        try {
            final int byteCount = img.getByteCount();
            MemoryFile mf = new MemoryFile("ashm", byteCount);
            mf.writeBytes(ShareMemUtil.getBytes(img), 0, 0, byteCount);
            return MemoryFileUtil.getParcelFileDescriptor(mf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendMessage(ParcelFileDescriptor pfd, int index, int[] intArgs, float[] floatArgs){
        if(mBound){
            Message msg = Message.obtain(null, index, 0, 0);
            Request request = new Request();
            request.setParcelFileDescriptor(pfd);
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
