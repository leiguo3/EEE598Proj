package edu.asu.msrs.artcelerationlibrary.data;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;

/**
 * Created by Lei on 11/5/2016.
 */

public class Base {
    private static final String KEY_PARCEL_FILE_DESCRIPTOR = "k_p_f_d";
    private ParcelFileDescriptor mPfd;

    Base(){}

    public ParcelFileDescriptor getParcelFileDescriptor() {
        return mPfd;
    }

    public void setParcelFileDescriptor(ParcelFileDescriptor pfd) {
        this.mPfd = pfd;
    }

    public Bundle writeToBundle(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_PARCEL_FILE_DESCRIPTOR, mPfd);
        return bundle;
    }

    public void readFromBundle(Bundle bundle){
        mPfd = bundle.getParcelable(KEY_PARCEL_FILE_DESCRIPTOR);
    }

}
