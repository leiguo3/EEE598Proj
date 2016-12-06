package edu.asu.msrs.artcelerationlibrary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.data.Result;
import edu.asu.msrs.artcelerationlibrary.tasks.Executor;
import edu.asu.msrs.artcelerationlibrary.tasks.ITaskCallback;
import edu.asu.msrs.artcelerationlibrary.tasks.TransformTask;

/**
 * Created by Lei on 11/3/2016.
 * The service which carries transform task - YZ
 */

public class ArtService extends Service {
    public static final int TRANSFORM_REQUEST = -2;
    public static final int PASS_CALLBACK_MESSENGER = -1;
    public static final int GAUSSIAN_BLUR = 0;
    public static final int ASCII_ART = 1;
    public static final int COLOR_FILTER = 2;
    public static final int MOTION_BLUR = 3;
    private static Context sContext;
    private final String TAG = "ArtService";
    private Messenger mCallbackMessenger;
    private TransformCallback mTransformCallback = new TransformCallback();

    public static Context getContext(){
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sContext = null;
    }

    private Handler mRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANSFORM_REQUEST:
                    // hanlde transform request
                    Request request = parseRequest(msg);
                    handleRequest(request);
                    break;
                case PASS_CALLBACK_MESSENGER:
                    // bind callback messenger
                    mCallbackMessenger = (Messenger)msg.obj;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private Messenger mRequestMessenger = new Messenger(mRequestHandler);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mRequestMessenger.getBinder();
    }

    private Request parseRequest(Message msg){
        Bundle bundle = msg.getData();
        return Request.create(bundle);
    }

    private void sendCallback(Request request){
        if(mCallbackMessenger != null){
            Result result = new Result();
            result.setParcelFileDescriptor(request.getParcelFileDescriptor());
            result.setWidth(request.getWidth());
            result.setHeight(request.getHeight());
            Message msg = Message.obtain();
            msg.setData(result.writeToBundle());
            try {
                mCallbackMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else{
            Log.e(TAG, "The Callback Messenger is null!");
        }
    }

    private void handleRequest(Request request){
        Executor.performTransform(request, mTransformCallback);
    }

    private class TransformCallback implements ITaskCallback<TransformTask>{
        @Override
        public void onTaskFinished(TransformTask task) {
            Request request = task.getRequest();
            sendCallback(request);
        }
    }

}
