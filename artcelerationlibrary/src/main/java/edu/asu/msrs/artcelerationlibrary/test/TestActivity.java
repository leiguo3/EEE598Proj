package edu.asu.msrs.artcelerationlibrary.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import edu.asu.msrs.artcelerationlibrary.R;

public class TestActivity extends AppCompatActivity {
    public static final String BROADCAST_ACTION = "edu.asu.msrs.show_image";
    public static Bitmap sBitmap;
    private ImageView mImageView;
    private static final String TAG = "TestActivity";

    public static void startTestActivity(Context context){
        Intent intent = new Intent(context, TestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static void sendShowImageBroadcast(Context context){
        Intent intent = new Intent(BROADCAST_ACTION);
        context.sendBroadcast(intent);
        Log.d(TAG, "Send broadcast!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mImageView = (ImageView)findViewById(R.id.imageView);
        registerMyReceiver();
        Log.d(TAG, "onCreate finished!");
        showImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterMyReceiver();
    }

    private void registerMyReceiver(){
        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void unRegisterMyReceiver(){
        if(mBroadcastReceiver != null){
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void showImage(){
        if(sBitmap != null && !sBitmap.isRecycled()){
            mImageView.setImageBitmap(sBitmap);
            Log.d(TAG, "Perform show image!");
        }else{
            Log.e(TAG, "Wrong bitmap data!");
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BROADCAST_ACTION.equals(action)){
                Log.d(TAG, "Receive broadcast!");
                showImage();
            }

        }
    };
}
