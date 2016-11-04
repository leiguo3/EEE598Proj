package edu.asu.msrs.artcelerationlibrary;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by rlikamwa on 10/2/2016.
 */

public class ArtLib {
    private ArtLibImpl mArtLibImpl;

    public ArtLib(Context context){
        mArtLibImpl = new ArtLibImpl(context);
    }

    public String[] getTransformsArray(){
        return mArtLibImpl.getTransformsArray();
    }

    public TransformTest[] getTestsArray(){
        return mArtLibImpl.getTestsArray();
    }

    public void registerHandler(TransformHandler artlistener){
        mArtLibImpl.registerHandler(artlistener);
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs){
        return mArtLibImpl.requestTransform(img, index, intArgs, floatArgs);
    }

}
