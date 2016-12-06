package edu.asu.msrs.artcelerationlibrary.data;

import android.os.Bundle;

/**
 * Created by Lei on 11/5/2016.
 * Request Data: used to pass transform type and args from Activity to Service
 */



public class Request extends Base {
    private static final String KEY_INT_ARGS = "k_int_args";
    private static final String KEY_FLOAT_ARGS = "k_float_args";
    private static final String KEY_TRANSFORM_TYPE = "k_tf_type";
    private int mTransformType;
    private int[] mIntArgs;
    private float[] mFloatArgs;

    public static Request create(Bundle bundle){
        Request request = new Request();
        request.readFromBundle(bundle);
        return request;
    }

    public int[] getIntArgs() {
        return mIntArgs;
    }

    public void setIntArgs(int[] intArgs) {
        this.mIntArgs = intArgs;
    }

    public float[] getFloatArgs() {
        return mFloatArgs;
    }

    public void setFloatArgs(float[] floatArgs) {
        this.mFloatArgs = floatArgs;
    }

    public int getTransformType() {
        return mTransformType;
    }

    public void setTransformType(int transformType) {
        this.mTransformType = transformType;
    }

    @Override
    public Bundle writeToBundle() {
        Bundle bundle = super.writeToBundle();
        bundle.putIntArray(KEY_INT_ARGS, mIntArgs);
        bundle.putFloatArray(KEY_FLOAT_ARGS, mFloatArgs);
        bundle.putInt(KEY_TRANSFORM_TYPE, mTransformType);
        return bundle;
    }

    @Override
    public void readFromBundle(Bundle bundle){
        super.readFromBundle(bundle);
        mIntArgs = bundle.getIntArray(KEY_INT_ARGS);
        mFloatArgs = bundle.getFloatArray(KEY_FLOAT_ARGS);
        mTransformType = bundle.getInt(KEY_TRANSFORM_TYPE);
    }
}
