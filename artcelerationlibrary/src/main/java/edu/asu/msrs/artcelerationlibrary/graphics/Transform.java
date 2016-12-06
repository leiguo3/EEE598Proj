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
 * Transform algorithms implementation - YZ
 */

public class Transform {

    static {
        System.loadLibrary("transform-lib");
    }

    private static final String TAG = "Transform";

    private static native byte[] nativeColorFilter(byte[] pixels, int[] intArgs);
    private static native byte[] nativeMotionBlur(byte[] pixels, int[] intArgs, int imgW, int imgH);
    private static native  byte[] nativeGaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs);

    public static byte[] colorFilter(byte[] pixels, int[] intArgs) {
        return nativeColorFilter(pixels, intArgs);
    }

    public static byte[] motionBlur(byte[] pixels, int imgW, int imgH, int[] intArgs) {
        return nativeMotionBlur(pixels, intArgs, imgW, imgH);
    }

    /**
     * The implementation of Ascii Art algorithm
     * @param pixels -- pixels of the original image
     * @param imgW -- image height
     * @param imgH --  image width
     * @return -- pixels of the image after transform
     */
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
            //  Now we have 36 Bitmap objects in the array of 'charBitmaps'
            // return the transformed byte array of pixels
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
     * @param width  number of mono-color pixels in each row of the image, equals to image_width * 4
     * @param imgH image height
     * @return a two dimension array which contains all the pixels in the original one dimension pixels array
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

    /**
     * Convert a 2D pixels array to a 1D pixels array
     * @param from the input 2D pixels array
     * @param to the output 1D pixels array
     * @param width umber of mono-color pixels in each row of the image, equals to image width * 4
     * @param imgH image height
     */
    private static void convert2DTo1D(byte[][] from, byte[] to, int width, int imgH) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < imgH; y++) {
                to[y * width + x] = from[x][y];
            }
        }
    }

    public static byte[] gaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs) {
        // Call the native Gaussian Blur method
        return nativeGaussianBlur(pixels, imgW, imgH, intArgs, floatArgs);
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
     * @return the one dimension byte array which contains the pixels after transform
     */
    public static byte[] asciiArtImpl(byte[] pixels, int imgW, int imgH, int asciiCount, int asciiWidth, int asciiHeight, Bitmap[] charBitmaps){
        // Main method for asciiArt transform

        // the size of the return array should be the same as the original one
        byte[] finalResult = new byte[pixels.length];

        // Store output mono-color pixel array
        byte[][] results = convert1DTo2D(pixels,imgW*4,imgH);
        byte[][] outputImage2D = new byte[imgW*4][imgH];

        byte whiteValue = (byte) 15;

        // One pixels is represented with 4 bytes (r, g, b, a), so the second dimension of the following array is asciiWidth * 4
        byte[][][] asciiImageList = new byte[asciiCount][asciiWidth * 4][asciiHeight];
        double[] meanAscii = new double[asciiCount];

        //Number of blocks in each direction
        int imgRepCountX = (int) Math.floor(imgW/asciiWidth);
        int imgRepCountY = (int) Math.floor(imgH/asciiHeight);

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

        // fill return array with correct (r, g, b, a) mono-color byte values
        convert2DTo1D(outputImage2D,finalResult,imgW*4,imgH);
        return finalResult;
    }


    /**
     * Calculate average pixel value of a block in image
     * @param img all the pixels in the original image
     * @param indexX1 the left edge of the block
     * @param indexX2 the top edge of the block
     * @param indexY1 the right edge of the block
     * @param indexY2 the bottom edge of the block
     * @return the average pixel value of the block
     */
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

    /**
     * get the pixels data from all the ascii Bitmaps and store them in the asciiImageList for further processing
     * @param asciiImageList 3D array, 1st dimension for the index of ascii images, 2nd dimension for ascii image width (* 4)
     *                       3rd dimension for ascii image height
     * @param meanAscii average pixels values for each ascii image.
     * @param charBitmaps input, the array which stores all the bitmaps of ascii images
     */
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

    /**
     * find the most close ascii image for a block on the original image.
     * @param distanceArray average pixels values for each ascii image.
     * @param blockValue the average pixels value of a block on the original image.
     * @return the index of the ascii image which is a match
     */
    private static int sortMinImage(double[] distanceArray, double blockValue){
        // Select the right ascii Image index for original image block replacement
        int minIndex = 0;
        double distance = Math.abs(distanceArray[0] - blockValue);
        for(int i=1; i < distanceArray.length; i++){
            if(Math.abs(distanceArray[i] - blockValue)<distance) {
                distance = Math.abs(distanceArray[i]);
                minIndex=i;
            }
        }
        return minIndex;
    }

}


