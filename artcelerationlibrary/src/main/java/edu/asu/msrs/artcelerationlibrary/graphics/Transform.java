package edu.asu.msrs.artcelerationlibrary.graphics;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Lei on 11/7/2016.
 */

public class Transform {
    private static final String TAG = "Transform";

    // TODO: For test, need further work
    // TODO: Use C++ code to improve efficiency
    public static byte[] colorFilter(int[] intArgs, byte[] pixels) {
        for (int i = 0; i < pixels.length; i += 4) {
            // Note: the data order is rgba in the pixels array.
            // Also, the data range is [-127, 127], we need to translate it to [0, 255] before do the transform.
            // transform R
            pixels[i] = transformByte(pixels[i], intArgs, 0);
            // transform G
            pixels[i + 1] = transformByte(pixels[i + 1], intArgs, 8);
            // transform B
            pixels[i + 2] = transformByte(pixels[i + 2], intArgs, 16);
        }
        return pixels;
    }

    private static byte transformByte(byte b, int[] intArgs, int offset) {
        if(b < 0){
            b += 256;
        }
        if(b == 0 || b == 255){
            return b;
        }
        int x1, y1, x2, y2;
        x1 = y1 = x2 = y2 = 0;
        int i = offset;
        for (; i < offset + 8; i += 2) {
            if (b <= intArgs[i]) {
                if (i == offset) {
                    x1 = 0;
                    y1 = 0;
                } else {
                    x1 = intArgs[i - 2];
                    y1 = intArgs[i - 1];
                }
                x2 = intArgs[i];
                y2 = intArgs[i + 1];
                break;
            }
        }
        // b > intArgs[offset + 6], so b is in [ intArgs[offset + 6], 255)
        if (i == offset + 8) {
            x1 = intArgs[i - 2];
            y1 = intArgs[i - 1];
            x2 = 255;
            y2 = 255;
        }
        int result = ((y2 - y1) * (b - x1)) / (x2 - x1) + y1;
        if (result <= 0) {
            return 0;
        }
        if(result > 127){
            result -= 256;
        }
        return (byte) result;
    }

//    public static void outputPixels(Bitmap bmp) {
//        for (int i = 0; i < 20; i++) {
//            int color = bmp.getPixel(i, 0);
//            int alpha = (color >> 24) & 0xff;
//            int r = (color >> 16) & 0xff;
//            int g = (color >> 8) & 0xff;
//            int b = color & 0xff;
//            Log.e("BMP", "read from bmp data alpha: " + alpha);
//            Log.e("BMP", "read from bmp data r: " + r);
//            Log.e("BMP", "read from bmp data g: " + g);
//            Log.e("BMP", "read from bmp data b: " + b);
//
//        }
//    }

}
