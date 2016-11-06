package edu.asu.msrs.artcelerationlibrary.utils;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

/**
 * Created by Lei on 11/5/2016.
 */

public class ShareMemUtil {

    public static byte[] getBytes(Bitmap bmp){
        ByteBuffer bb = ByteBuffer.allocate(bmp.getByteCount());
        bmp.copyPixelsToBuffer(bb);
        return bb.array();
    }

}
