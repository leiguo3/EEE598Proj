package edu.asu.msrs.artcelerationlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import edu.asu.msrs.artcelerationlibrary.data.Request;
import edu.asu.msrs.artcelerationlibrary.data.Result;

/**
 * Created by Lei on 11/3/2016.
 */

public class ArtService extends Service {
    public static final int PASS_CALLBACK_MESSENGER = -1;
    public static final int GAUSSIAN_BLUR = 0;
    public static final int NEON_EDGES = 1;
    public static final int COLOR_FILTER = 2;
    private final String TAG = "ArtService";
    private Messenger mCallbackMessenger;

    private Handler mRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Request request;
            switch (msg.what) {
                case GAUSSIAN_BLUR:
                    // handle Gaussian blur request
                    request = parseRequest(msg);
                    break;
                case NEON_EDGES:
                    // handle Neon edges request
                    request = parseRequest(msg);
                    break;
                case COLOR_FILTER:
                    // handle Color filter request
                    request = parseRequest(msg);
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

    private void sendCallback(ParcelFileDescriptor pfd){
        if(mCallbackMessenger != null){
            Result result = new Result();
            result.setParcelFileDescriptor(pfd);
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
}
