package edu.asu.msrs.artcelerationlibrary.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.asu.msrs.artcelerationlibrary.R;
import edu.asu.msrs.artcelerationlibrary.service.ArtService;
import edu.asu.msrs.artcelerationlibrary.utils.ShareMemUtil;
import edu.asu.msrs.artcelerationlibrary.utils.Utils;

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
    private static native byte[] nativeMotionBlur(byte[] pixels, int[] intArgs, int imgW, int imgH);
    private static native  byte[] nativeGaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs);

    public static byte[] colorFilter(byte[] pixels, int[] intArgs) {
        return nativeColorFilter(pixels, intArgs);
    }

    public static byte[] motionBlur(byte[] pixels, int imgW, int imgH, int[] intArgs) {
        return nativeMotionBlur(pixels, intArgs, imgW, imgH);
//        byte[][] redArray = copyOneColorFromOrigin(pixels, imgW, imgH, 0);
//        byte[][] greenArray = copyOneColorFromOrigin(pixels, imgW, imgH, 1);
//        byte[][] blueArray = copyOneColorFromOrigin(pixels, imgW, imgH, 2);
//        final int orientation = intArgs[0];
//        final int radius = intArgs[1];
//        redArray = motionBlurOneColor(redArray, imgW, imgH, orientation, radius);
//        greenArray = motionBlurOneColor(greenArray, imgW, imgH, orientation, radius);
//        blueArray = motionBlurOneColor(blueArray, imgW, imgH, orientation, radius);
//        copyOneColorToOrigin(redArray, pixels, imgW, imgH, 0);
//        copyOneColorToOrigin(greenArray, pixels, imgW, imgH, 1);
//        copyOneColorToOrigin(blueArray, pixels, imgW, imgH, 2);
//        return pixels;
    }

    public static byte[] motionBlurNew(byte[] pixels, int imgW, int imgH, int[] intArgs) {
        // The width of the row of 2D array;
        final int width = imgW * 4;
        byte[][] twoDPixels = convert1DTo2D(pixels, width, imgH);
        final int orientation = intArgs[0];
        final int radius = intArgs[1];
        twoDPixels = motionBlur(twoDPixels, width, imgH, orientation, radius);
        convert2DTo1D(twoDPixels, pixels, width, imgH);
        return pixels;
    }

    public static byte[] asciiArt(byte[] pixels, int imgW, int imgH){
        // Get the context which is used to create Bitmaps from drawables
        Context context = ArtService.getContext();
        if (context != null) {
            // The number of char images
            final int charCount = 36;
            // The format of ids for char drawables
            final String mCharId = "char%d";
            // Bitmap characters resource Ids.
            final Bitmap[] charBitmaps = new Bitmap[charCount];
            // The width of ascii image in pixels
            int asciiWidth = 0;
            // The Height of ascii image in pixels
            int asciiHeight = 0;
            // We use ARGB_8888 to create Bitmaps, each pixel take 32 bits
            BitmapFactory.Options opts = new BitmapFactory.Options();
            Resources res = context.getResources();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            // Read each images and convert them to Bitmap
            for(int i = 0; i < charCount; i++){
                String resName = String.format(mCharId, i);
                int resId = Utils.getResId(resName, R.drawable.class);
                Bitmap bmp = BitmapFactory.decodeResource(res, resId, opts);
                charBitmaps[i] = bmp;
                if(asciiWidth == 0){
                    asciiWidth = bmp.getWidth();
                    asciiHeight = bmp.getHeight();
                }
            }
            // TODO: Now we have 36 Bitmap objects in the array of 'charBitmaps'
            // TODO: We can get their pixels with ShareMemUtil.getBytes(bmp) which will give us a byte array of pixel

            // TODO: return the transformed byte array of pixels
            return asciiArtImpl(pixels, imgW, imgH, charCount, asciiWidth, asciiHeight, charBitmaps);
        } else {
            // Return the original pixels if can't get the context here
            // This won't happen if this method is called in the Service.
            return pixels;
        }
    }

    /**
     * Convert a 1D pixels array to a 2D pixels array
     * @param pixels
     * @param width -- number of mono-color pixels in each row of the image, equals to image_width * 4
     * @param imgH
     * @return
     */
    private static byte[][] convert1DTo2D(byte[] pixels, int width, int imgH) {
        byte[][] result = new byte[width][imgH];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < imgH; y++) {
                result[x][y] = pixels[y * width + x];
            }
        }
        return result;
    }

    private static void convert2DTo1D(byte[][] from, byte[] to, int width, int imgH) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < imgH; y++) {
                to[y * width + x] = from[x][y];
            }
        }
    }

    private static byte[][] motionBlur(byte[][] pixels, int width, int height, int orientation, int radius) {
        byte[][] newPixels = new byte[width][height];
        int count = 2 * radius + 1;
        for (int i = 0; i < width; i += 4) {
            for (int j = 0; j < height; j++) {
                int R, G, B;
                R = G = B = 0;
                int index;
                if (orientation == 0) {
                    // horizontal blur
                    for (int k = -radius; k <= radius; k++) {
                        index = i + k * 4;
                        if (index >= 0 && index < width) {
                            R += pixels[index][j] & 0xFF;
                            G += pixels[index + 1][j] & 0xFF;
                            B += pixels[index + 2][j] & 0xFF;
                        }
                    }
                } else {
                    // vertical blur
                    for (int k = -radius; k <= radius; k++) {
                        index = j + k;
                        if (index >= 0 && index < height) {
                            R += pixels[i][index] & 0xFF;
                            G += pixels[i][index + 1] & 0xFF;
                            B += pixels[i][index + 2] & 0xFF;
                        }
                    }
                }
                R = R / count;
                G = G / count;
                B = B / count;
                newPixels[i][j] = getByteColor(R);
                newPixels[i + 1][j] = getByteColor(G);
                newPixels[i + 2][j] = getByteColor(B);
            }
        }
        return newPixels;
    }

    private static byte getByteColor(int color) {
        if (color < 0) {
            color = 0;
        }
        if (color > 255) {
            color = 255;
        }
        return (byte) color;
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
                if (newPixel < 0) {
                    newPixel = 0;
                }
                if (newPixel > 255) {
                    newPixel = 255;
                }
                newPixels[i][j] = (byte) newPixel;
            }
        }
        return newPixels;
    }

    public static byte[] gaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs) {
        return nativeGaussianBlur(pixels, imgW, imgH, intArgs, floatArgs);
//        final int radius = intArgs[0];
//        final float delta = floatArgs[0];
//        final int vectorLen = 2 * radius + 1;
//        double[] gaussVector = new double[vectorLen];
//        double total = 0;
//        for (int i = 0; i < vectorLen; i++) {
//            gaussVector[i] = Math.exp(-(i - radius) * (i - radius) / (2 * delta * delta)) / Math.sqrt(2 * Math.PI * delta * delta);
//            total += gaussVector[i];
//        }
//        // normalize
//        for (int i = 0; i < vectorLen; i++) {
//            gaussVector[i] = gaussVector[i] / total;
//        }
//
//        for (int i = 0; i < 3; i++) {
//            gaussBlurOneColor(pixels, imgW, imgH, i, gaussVector, radius);
//        }
//
//        return pixels;
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
        byte[][] result = new byte[imgW*4][imgH];
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
                    pixel = (int) p;
                    if (pixel < 0) {
                        pixel = 0;
                    }
                    if (pixel > 255) {
                        pixel = 255;
                    }
                    result[x][y] = (byte) p;
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
                    pixel = (int) p;
                    if (pixel < 0) {
                        pixel = 0;
                    }
                    if (pixel > 255) {
                        pixel = 255;
                    }
                    result[x][y] = (byte) pixel;
                }
            }
        }
        return result;
    }


    /**
     *
     * @param pixels -- the byte array which contains all the mono-color byte color values
     *               in the original image to be transformed.
     * @param imgH -- in pixels, on pixel = 4 bytes
     * @param imgW -- in pixels, on pixel = 4 bytes
     * @param asciiCount -- the number of ascii character images
     * @param asciiWidth -- in pixels, on pixel = 4 bytes
     * @param asciiHeight -- in pixels, on pixel = 4 bytes
     * @param charBitmaps -- the array contains bitmaps for all the ascii character images.
     * @return
     */
    public static byte[] asciiArtImpl(byte[] pixels, int imgW, int imgH, int asciiCount, int asciiWidth, int asciiHeight, Bitmap[] charBitmaps){
        // Main method for asciiArt transform

        // TODO: we should have a one dimensional byte array as the return value
        // TODO: the size of the return array should be the same as the original one
        byte[] correctResult = new byte[pixels.length];

       // byte[][] results = new byte[imgW][imgH]; // Store output monocolor pixel array
        byte[][] results = convert1DTo2D(pixels,imgW*4,imgH);
        byte[][] outputImage2D = new byte[imgW*4][imgH];

        /*
        byte[][] redArray = copyOneColorFromOrigin(pixels, imgW, imgH, 0);
        byte[][] greenArray = copyOneColorFromOrigin(pixels, imgW, imgH, 1);
        byte[][] blueArray = copyOneColorFromOrigin(pixels, imgW, imgH, 2);
        */
        byte whiteValue = (byte) 15;
        double scalingFactor = 1;
        int colorAvg;
        // Array should be initialized here, or you won't get any data from the 'loadAsciiList' function
        // One pixels is represented with 4 bytes (r, g, b, a), so the second dimension of the following array is asciiWidth * 4
        byte[][][] asciiImageList = new byte[asciiCount][asciiWidth * 4][asciiHeight];
        double[] meanAscii = new double[asciiCount];

        //Number of blocks in each direction
        int imgRepCountX = (int) Math.floor(imgW/asciiWidth);
        int imgRepCountY = (int) Math.floor(imgH/asciiHeight);

/*
        // Avarage R,G,B channel of original image
        for (int i=0;i<imgRepCountX*asciiWidth;i++){
            for (int j=0;j<imgRepCountY*asciiHeight;j++){
                // j is the index of rows and i is the index of columns
                colorAvg = ((redArray[i][j]&0xFF)+(greenArray[i][j]&0xFF)+(blueArray[i][j]&0xFF))/3;
                results[i][j]= (byte)(colorAvg*scalingFactor);
            }
        }
*/
        // set edge of outputImage to white.
        for (int i=imgRepCountX*asciiWidth*4;i<imgW*4; i++){
            for (int j=imgRepCountY*asciiHeight;j<imgH;j++){
                outputImage2D[i][j] = whiteValue;
            }
        }

        // Load ascii image list
        loadAsciiList(asciiImageList,meanAscii, charBitmaps);


        // Find closest ascii image and make replacement.
        int imageIndex = 0;
        double blockAvgValue = 0;
        for (int i=0; i< imgRepCountX;i++){
            for (int j=0; j< imgRepCountY; j++) {
                blockAvgValue = blockAvg(results,i*asciiWidth,(i+1)*asciiWidth-1,j*asciiHeight,(j+1)*asciiHeight-1);
                imageIndex = sortMinImage(meanAscii,blockAvgValue);

                // Copy selected ascii image into results
                for (int k=0;k<asciiWidth;k++){
                    for (int l=0;l<asciiHeight;l++){
                        outputImage2D[i*asciiWidth*4+k*4][j*asciiHeight+l] = asciiImageList[imageIndex][k*4][l];
                        outputImage2D[i*asciiWidth*4+k*4+1][j*asciiHeight+l] = asciiImageList[imageIndex][k*4+1][l];
                        outputImage2D[i*asciiWidth*4+k*4+2][j*asciiHeight+l] = asciiImageList[imageIndex][k*4+2][l];
                        outputImage2D[i*asciiWidth*4+k*4+3][j*asciiHeight+l] = asciiImageList[imageIndex][k*4+3][l];
                    }
                }

            }
        }

        // TODO: fill return array with correct (r, g, b, a) mono-color byte values

        convert2DTo1D(outputImage2D,correctResult,imgW*4,imgH);
        return correctResult;
    }


    private static double blockAvg(byte[][] img, int indexX1, int indexX2, int indexY1, int indexY2){
        // Calculate avergae pixel value of a block in image
        double pixelAvg = 0;

        // i and j are pixel indices
        for(int i=indexX1;i<indexX2;i++){
            for(int j=indexY1; j<indexY2;j++) {
                pixelAvg = pixelAvg + img[4*i][j]+img[4*i+1][j]+img[4*i+2][j];
            }
        }

        return pixelAvg/Math.abs(3*(indexX2-indexX1+1)*(indexY1-indexY2+1));
    }

    private static void loadAsciiList(byte[][][] asciiImageList,double[] meanAscii, Bitmap[] charBitmaps){
        //load mono-color image list

        // Initialize the list of ascii image array -> asciiImageList.
        final int asciiCount = charBitmaps.length;
        for (int i = 0; i< asciiCount; i++) {
            Bitmap bmp = charBitmaps[i];
            byte[] pixels = ShareMemUtil.getBytes(bmp);
            int width = bmp.getWidth() * 4;
            int height = bmp.getHeight();
            byte[][] twoDPixels = convert1DTo2D(pixels, width, height);
            asciiImageList[i] = twoDPixels;
            // Initialize avarage pixel value of the image list -> meanAscii
            meanAscii[i] = blockAvg(twoDPixels, 0, width/4, 0, height);
        }
    }

    private static int sortMinImage(double[] distanceArray, double blockValue){
        // Select the right ascii Image index for original image block replacement
        int minIndex = 0;
        double distance = Math.abs(distanceArray[0] - blockValue);
        for(int i=1; i < distanceArray.length; i++){
            if(Math.abs(distanceArray[i] - blockValue)<distance) {
                distance = Math.abs(distanceArray[i]);
                // TODO: [remove comment] the indices are start from 0, not 1
                minIndex=i;
            }
        }
        return minIndex;
    }

    /*
    public static byte[] tiltShift(byte[] pixels, int imgW, int imgH, int[] progBlur, float sigma1, float sigma2){
        // Implementation of Tilt-Shift

        byte[][] outputImage = new byte[4*imgW][imgH];


        return convert2DTo1D(outputImage,4*imgW,imgH);
    }
    */
}


