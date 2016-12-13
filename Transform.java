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

    // Parameter for Ascii Art
    private static final int asciiWidth = 9;
    private static final int asciiHeight = 17;
    private static final int asciiNum = 36;

    // During test, native code consumes about 30 milliseconds, while java code consumes 100 milliseconds more.
    private static native byte[] nativeColorFilter(byte[] pixels, int[] intArgs);

    public static byte[] colorFilter(byte[] pixels, int[] intArgs) {
        return nativeColorFilter(pixels, intArgs);
    }


    public static byte[] motionBlur(byte[] pixels, int imgW, int imgH, int[] intArgs) {
        byte[][] redArray = copyOneColorFromOrigin(pixels, imgW, imgH, 0);
        byte[][] greenArray = copyOneColorFromOrigin(pixels, imgW, imgH, 1);
        byte[][] blueArray = copyOneColorFromOrigin(pixels, imgW, imgH, 2);
//        byte[][] alphaArray = copyOneColorFromOrigin(pixels, imgW, imgH, 3);
        final int orientation = intArgs[0];
        final int radius = intArgs[1];
        redArray = motionBlurOneColor(redArray, imgW, imgH, orientation, radius);
        greenArray = motionBlurOneColor(greenArray, imgW, imgH, orientation, radius);
        blueArray = motionBlurOneColor(blueArray, imgW, imgH, orientation, radius);
//        alphaArray = motionBlurOneColor(alphaArray, imgW, imgH, orientation, radius);
        copyOneColorToOrigin(redArray, pixels, imgW, imgH, 0);
        copyOneColorToOrigin(greenArray, pixels, imgW, imgH, 1);
        copyOneColorToOrigin(blueArray, pixels, imgW, imgH, 2);
        return pixels;
    }

    private static byte[][] motionBlurOneColor(byte[][] pixels, int imgW, int imgH, int orientation, int radius) {
        byte[][] newPixels = new byte[imgW][imgH];
        int count = 2 * radius + 1;
        for (int i = 0; i < imgW; i++) {
            for (int j = 0; j < imgH; j++) {
                int pixel = 0;
                int index;
                if (orientation == 0) {
                    // horizontal blur
                    for (int k = -radius; k <= radius; k++) {
                        index = i + k;
                        if (index >= 0 && index < imgW) {
                            pixel += pixels[index][j] & 0xFF;
                        }
                    }
                } else {
                    // vertical blur
                    for (int k = -radius; k <= radius; k++) {
                        index = j + k;
                        if (index >= 0 && index < imgH) {
                            pixel += pixels[i][index] & 0xFF;
                        }
                    }
                }
                int newPixel = pixel / count;
                if(newPixel < 0){
                    newPixel = 0;
                }
                if(newPixel > 255){
                    newPixel = 255;
                }
                newPixels[i][j] = (byte)newPixel;
            }
        }
        return newPixels;
    }

    public static byte[] gaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs) {
        final int radius = intArgs[0];
        final float delta = floatArgs[0];
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
        int pixel;
        double p;
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
                            p += gaussVector[i] * (pixels[xi][y] & 0xFF);
                        }
                    }
                    pixel = (int)p;
                    if(pixel < 0){
                        pixel = 0;
                    }
                    if(pixel > 255){
                        pixel = 255;
                    }
                    result[x][y] = (byte)p;
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
                            p += gaussVector[i] * (pixels[x][yi] & 0xFF);
                        }
                    }
                    pixel = (int)p;
                    if(pixel < 0){
                        pixel = 0;
                    }
                    if(pixel > 255){
                        pixel = 255;
                    }
                    result[x][y] = (byte)pixel;
                }
            }
        }
        return result;
    }


    public static byte[][] asciiArt(byte[] pixels, int imgH, int imgW){
        // Main method for asciiArt transform

        byte[][] results = new byte[imgH][imgW]; // Store output monocolor pixel array
        byte[][] redArray = copyOneColorFromOrigin(pixels, imgW, imgH, 0);
        byte[][] greenArray = copyOneColorFromOrigin(pixels, imgW, imgH, 1);
        byte[][] blueArray = copyOneColorFromOrigin(pixels, imgW, imgH, 2);

        byte whiteValue = (byte) 255;
        double scalingFactor = 15/255;
        int colorAvg;
        byte[][][] asciiImageList;
        double[] meanAscii;

        //Number of blocks in each direction
        int imgRepCountX = (int) Math.floor(imgW/asciiWidth);
        int imgRepCountY = (int) Math.floor(imgH/asciiHeight);


        // Avarage R,G,B channel of original image
        for (int i=0;i<imgRepCountY*asciiHeight;i++){
            for (int j=0;j<imgRepCountX*asciiWidth;j++){
                colorAvg = (redArray[i][j]&0xFF)/3+(greenArray[i][j]&0xFF)/3+(blueArray[i][j]&0xFF)/3;
                results[i][j]= (byte)(colorAvg*scalingFactor);
            }
        }

        // set edge to white.
        for (int i=imgRepCountY*asciiHeight;i<imgH){
            for (int j=imgRepCountX*asciiWidth;j<imgW;j++){
                results[i][j] = whiteValue;
            }
        }

        // Load ascii image list
        loadAsciiList(asciiImageList,meanAscii);


        // Find closest ascii image and make replacement.
        int imageIndex = 0;
        double blockAvgValue = 0;
        for (int i=0; i< imgRepCountX;i++){
            for (int j=0; j< imgRepCountY; j++) {
                blockAvgValue = blockAvg(results,i,i+asciiWidth-1,j,j+asciiHeight-1);
                imageIndex = sortMinImage(meanAscii,blockAvgValue);

                // Copy selected ascii image into results
                for (int k=0;k<asciiWidth;k++){
                    for (int l=0;l<asciiHeight;l++){
                        results[i+k][j+l] = asciiImageList[imageIndex][k][l];
                    }
                }

            }
        }
        
        return results;
    }


    private static double blockAvg(byte[][] img, int indexX1, int indexX2, int indexY1, int indexY2){
        // Calculate avergae pixel value of a block in image
        double pixelAvg = 0;
        for(int i=indexX1;i<indexX2;i++){
            for(int j=indexY1; j<indexY2;j++) {
                pixelAvg = pixelAvg + img[i][j];
            }
        }

        return pixelAvg/Math.abs((indexX2-indexX1+1)*(indexY1-indexY2+1));
    }

    private static void loadAsciiList(byte[][][] asciiImageList,double[] meanAscii){
        //load monocolor image list

        // Initialize the list of ascii image array -> asciiImageList.

        // Initialize avarage pixel value of the image list -> meanAscii

    }

    private static int sortMinImage(double[] distanceArray, double blockValue){
        // Select the right ascii Image index for original image block replacement
        int minIndex = 0;
        double distance = Math.abs(distanceArray[0] - blockValue);
        for(int i=1; i < asciiNum; i++){
            if(Math.abs(distanceArray[i] - blockValue)<distance) {
                distance = Math.abs(distanceArray[i]);
                minIndex=i+1;
            }
        }
        return minIndex;
    }

}

