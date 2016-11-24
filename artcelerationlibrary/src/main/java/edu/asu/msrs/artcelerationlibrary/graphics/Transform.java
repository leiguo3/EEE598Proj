package edu.asu.msrs.artcelerationlibrary.graphics;

import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Lei on 11/7/2016.
 */

public class Transform {

    static {
        System.loadLibrary("transform-lib");
    }

    private static final String TAG = "Transform";

    // During test, native code consumes about 30 milliseconds, while java code consumes 100 milliseconds more.
    private static native byte[] nativeColorFilter(byte[] pixels, int[] intArgs);

    public static byte[] colorFilter(byte[] pixels, int[] intArgs) {
        return nativeColorFilter(pixels, intArgs);
    }

    public static byte[] gaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs) {
        final int radius = intArgs[0];
        final float delta = floatArgs[0];
//        final int length = pixels.length;
        final int vectorLen = 2 * radius + 1;
        double[] gaussVector = new double[vectorLen];
        double total = 0;
        for (int i = 0; i < vectorLen; i++) {
            gaussVector[i] = Math.exp(-(i - radius) * (i - radius) / (2 * delta * delta)) / Math.sqrt(2 * Math.PI * delta * delta);
            total += gaussVector[i];
        }
        // normalize
        for (int i = 0; i < vectorLen; i++) {
            gaussVector[i] = gaussVector[i] / total;
        }

        for (int i = 0; i < 3; i++) {
            gaussBlurOneColor(pixels, imgW, imgH, i, gaussVector, radius);
        }

//        int[][] midPixels = new int[imgW][imgH];
//        int[][] finalPixels = new int[imgW][imgH];
//        int pixel, startIndex;
        // convert byte pixels to int pixels
//        for (int y = 0; y < imgH; y++) {
//            for (int x = 0; x < imgW; x++) {
//                startIndex = (y * imgW + x) * 4;
//                pixel = pixels[startIndex];
//                pixel = (pixel << 8) + pixels[startIndex + 1];
//                pixel = (pixel << 8) + pixels[startIndex + 2];
//                pixel = (pixel << 8) + pixels[startIndex + 3];
//                finalPixels[x][y] = pixel;
//            }
//        }
//        Bitmap bmp = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888);
//        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(pixels));
//        int[] intPixels = new int[pixels.length / 4];
//        bmp.getPixels(intPixels, 0, imgW, 0, 0, imgW, imgH);
//        for (int y = 0; y < imgH; y++) {
//            for (int x = 0; x < imgW; x++) {
//                finalPixels[x][y] = intPixels[y * imgW + x];
//            }
//        }

//        int p;
//        // x index;
//        int xi;
//        for (int x = 0; x < imgW; x++) {
//            for (int y = 0; y < imgH; y++) {
//                p = 0;
//                for (int i = 0; i < vectorLen; i++) {
//                    xi = x + i - radius;
//                    if (xi >= 0 && xi < imgW) {
//                        p += gaussVector[i] * finalPixels[xi][y];
//                    }
//                }
//                midPixels[x][y] = p;
//            }
//        }
//        // y index
//        int yi;
//        for (int x = 0; x < imgW; x++) {
//            for (int y = 0; y < imgH; y++) {
//                p = 0;
//                for (int i = 0; i < vectorLen; i++) {
//                    yi = y + i - radius;
//                    if (yi >= 0 && yi < imgH) {
//                        p += gaussVector[i] * midPixels[x][yi];
//                    }
//                }
//                finalPixels[x][y] = p;
//            }
//        }
        //  convert int pixels to byte pixels
//        for (int y = 0; y < imgH; y++) {
//            for (int x = 0; x < imgW; x++) {
//                pixel = finalPixels[x][y];
//                startIndex = (y * imgW + x) * 4;
//                pixels[startIndex] = (byte) (pixel >> 24 & 0xFF);
//                pixels[startIndex + 1] = (byte) (pixel >> 16 & 0xFF);
//                pixels[startIndex + 2] = (byte) (pixel >> 8 & 0xFF);
//                pixels[startIndex + 3] = (byte) (pixel & 0xFF);
//            }
//        }
//        for (int y = 0; y < imgH; y++) {
//            for (int x = 0; x < imgW; x++) {
//                intPixels[y * imgW + x] = finalPixels[x][y];
//            }
//        }
//        bmp.setPixels(intPixels, 0, imgW, 0, 0, imgW, imgH);
//        ByteBuffer bb = ByteBuffer.allocate(pixels.length);
//        bmp.copyPixelsToBuffer(bb);
//        pixels = bb.array();
        return pixels;
    }

    private static void gaussBlurOneColor(byte[] pixels, int imgW, int imgH, int offset, double[] gaussVector, int radius) {
        byte[][] oneColor = copyOneColorFromOrigin(pixels, imgW, imgH, offset);
        // blur x orientation
        byte[][] newColor = convertOneColor(oneColor, gaussVector, radius, imgW, imgH, 0);
        // blur y orientation
        newColor = convertOneColor(newColor, gaussVector, radius, imgW, imgH, 1);
        copyOneColorToOrigin(newColor, pixels, imgW, imgH, offset);
    }


    private static byte[][] copyOneColorFromOrigin(byte[] pixels, int imgW, int imgH, int offset) {
        // offset: r = 0, g = 1, b = 2, a = 3;
        byte[][] result = new byte[imgW][imgH];
        for (int x = 0; x < imgW; x++) {
            for (int y = 0; y < imgH; y++) {
                int index = (y * imgW + x) * 4 + offset;
                result[x][y] = pixels[index];
            }
        }
        return result;
    }

    private static void copyOneColorToOrigin(byte[][] result, byte[] pixels, int imgW, int imgH, int offset) {
        // offset: r = 0, g = 1, b = 2, a = 3;
        for (int x = 0; x < imgW; x++) {
            for (int y = 0; y < imgH; y++) {
                int index = (y * imgW + x) * 4 + offset;
                pixels[index] = result[x][y];
            }
        }
    }

    private static byte[][] convertOneColor(byte[][] pixels, double[] gaussVector, int radius, int imgW, int imgH, int orientation) {
        // orientation: 0 = horizontal, other = vertical
        byte[][] result = new byte[imgW][imgH];
        int vectorLen = gaussVector.length;
        byte p;
        if (orientation == 0) {
            // horizontal
            // x index;
            int xi;
            for (int x = 0; x < imgW; x++) {
                for (int y = 0; y < imgH; y++) {
                    p = 0;
                    for (int i = 0; i < vectorLen; i++) {
                        xi = x + i - radius;
                        if (xi >= 0 && xi < imgW) {
                            p += gaussVector[i] * pixels[xi][y];
                        }
                    }
                    result[x][y] = p;
                }
            }
        } else {
            // vertical
            // y index
            int yi;
            for (int x = 0; x < imgW; x++) {
                for (int y = 0; y < imgH; y++) {
                    p = 0;
                    for (int i = 0; i < vectorLen; i++) {
                        yi = y + i - radius;
                        if (yi >= 0 && yi < imgH) {
                            p += gaussVector[i] * pixels[x][yi];
                        }
                    }
                    result[x][y] = p;
                }
            }
        }
        return result;
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
