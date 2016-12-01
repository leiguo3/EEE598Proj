package edu.asu.msrs.artcelerationlibrary.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.asu.msrs.artcelerationlibrary.R;
import edu.asu.msrs.artcelerationlibrary.utils.Utils;

/**
 * Created by Lei on 11/29/2016.
 */

public class AsciiArt {
    private Context mContext;
    // The number of bitmap characters
    private final int mCharCount = 36;
    private String mCharId = "char%d";
    // Bitmap characters resource Ids.
    private Bitmap[] mCharBitmaps = new Bitmap[mCharCount];
    // The image to be processed
    private byte[] mImagePixels;
    private int mImageWidth;
    private int mImageHeight;

    public AsciiArt(Context context, byte[] pixels, int imgW, int imgH) {
        mContext = context;
        mImagePixels = pixels;
        mImageWidth = imgW;
        mImageHeight = imgH;
        init();
    }

    private void init(){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Resources res = mContext.getResources();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        for(int i = 0; i < mCharCount; i++){
            String resName = String.format(mCharId, i);
            int resId = Utils.getResId(resName, R.drawable.class);
            Bitmap bmp = BitmapFactory.decodeResource(res, resId, opts);
            mCharBitmaps[i] = bmp;
        }
    }

    public byte[] asciiArt(){
        return null;
    }

}
