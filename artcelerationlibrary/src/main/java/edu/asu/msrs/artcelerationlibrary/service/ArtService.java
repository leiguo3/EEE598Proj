package edu.asu.msrs.artcelerationlibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

/**
 * Created by Lei on 11/3/2016.
 */

public class ArtService extends Service {
    public static final int GAUSSIAN_BLUR = 0;
    public static final int NEON_EDGES = 1;
    public static final int COLOR_FILTER = 2;

    Handler mIncomingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GAUSSIAN_BLUR:
                    // handle Gaussian blur request
                    break;
                case NEON_EDGES:
                    // handle Neon edges request
                    break;
                case COLOR_FILTER:
                    // handle Color filter request
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    Messenger mMessenger = new Messenger(mIncomingHandler);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
