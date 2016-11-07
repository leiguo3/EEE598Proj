package edu.asu.msrs.artcelerationlibrary.data;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;

/**
 * Created by Lei on 11/5/2016.
 */

public class Base {
    private static final String KEY_PARCEL_FILE_DESCRIPTOR = "k_p_f_d";
    private static final String KEY_WIDTH = "k_width";
    private static final String KEY_HEIGHT = "k_height";
    private ParcelFileDescriptor mPfd;
    /**
     * The Bitmap width. If the file is not a Bitmap, don't care about this value.
     */
    private int mWidth;
    /**
     * The Bitmap height. If the file is not a Bitmap, don't care about this value.
     */
    private int mHeight;

    Base(){}

    public ParcelFileDescriptor getParcelFileDescriptor() {
        return mPfd;
    }

    public void setParcelFileDescriptor(ParcelFileDescriptor pfd) {
        this.mPfd = pfd;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

    public Bundle writeToBundle(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_PARCEL_FILE_DESCRIPTOR, mPfd);
        bundle.putInt(KEY_WIDTH, mWidth);
        bundle.putInt(KEY_HEIGHT, mHeight);
        return bundle;
    }

    public void readFromBundle(Bundle bundle){
        mPfd = bundle.getParcelable(KEY_PARCEL_FILE_DESCRIPTOR);
        mWidth = bundle.getInt(KEY_WIDTH);
        mHeight = bundle.getInt(KEY_HEIGHT);
    }

}
