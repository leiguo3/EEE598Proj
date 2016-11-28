package edu.asu.msrs.artcelerationlibrary.graphics;

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
    private static native byte[] nativeGaussianBlur(byte[] pixels, int imgW, int imgH, int[] intArgs, float[] floatArgs);

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

}
