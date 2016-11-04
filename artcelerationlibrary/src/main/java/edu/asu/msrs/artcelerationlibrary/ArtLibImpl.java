package edu.asu.msrs.artcelerationlibrary;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import edu.asu.msrs.artcelerationlibrary.service.ArtService;

/**
 * Created by Lei on 11/3/2016.
 */

public class ArtLibImpl {
    private TransformHandler mArtlistener;
    private String[] mTransforms = {"Gaussian Blur", "Neon edges", "Color Filter"};
    private boolean mBound = false;
    private Context mContext;
    private Messenger mMessenger;

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
        return true;
    }

    private void sendMessage(int index, int[] intArgs, float[] floatArgs){
        if(mBound){
            Message msg = Message.obtain(null, index, 0, 0);
            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{

        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessenger = null;
            mBound = false;
        }
    };

    private void bindService(){
        mContext.bindService(new Intent(mContext, ArtService.class), mConnection, Service.BIND_AUTO_CREATE);
    }
}
