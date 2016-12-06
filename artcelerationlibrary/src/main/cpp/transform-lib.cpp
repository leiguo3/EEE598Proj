//
// Created by Lei on 11/10/2016.
// This is the implementation of Color filter, Motion Blur and Guassian Blur in NDK. - YZ
//
#include <jni.h>
#include <string>
#include <cmath>
#include "transform-lib.h"

extern "C"

jbyteArray
Java_edu_asu_msrs_artcelerationlibrary_graphics_Transform_nativeColorFilter(
        JNIEnv *env,
        jobject This,
        jbyteArray initialPixels,
        jintArray intArgs) {

    int length = env->GetArrayLength(initialPixels);
    jbyte *pixels = env->GetByteArrayElements(initialPixels, 0);
    jint *args = env->GetIntArrayElements(intArgs, 0);
    // Used to map the initial color to the new color value.
    // The indices of those arrays are the initial color value, the value at those indices are the new color value.
    // Use those arrays to improve the speed of color transform.
    jbyte *colorMapsR = new jbyte[256];
    jbyte *colorMapsG = new jbyte[256];
    jbyte *colorMapsB = new jbyte[256];
    for (int i = 0; i < 256; i++) {
        colorMapsR[i] = (jbyte) applyColorFilter(i, args, 0);
        colorMapsG[i] = (jbyte) applyColorFilter(i, args, 8);
        colorMapsB[i] = (jbyte) applyColorFilter(i, args, 16);
    }
    for (int i = 0; i < length; i += 4) {
        // Note: the data order is rgba in the pixels array.
        // Also, the data range is [-128, 127], we need to translate it to [0, 255] before do the transform.
        // transform R
        pixels[i] = colorMapsR[pixels[i] & 0xFF];
        // transform G
        pixels[i + 1] = colorMapsG[pixels[i + 1] & 0xFF];
        // transform B
        pixels[i + 2] = colorMapsB[pixels[i + 2] & 0xFF];
    }
    // copy the pixels back
    env->SetByteArrayRegion(initialPixels, 0, length, pixels);
    // release resources
    env->ReleaseByteArrayElements(initialPixels, pixels, 0);
    env->ReleaseIntArrayElements(intArgs, args, 0);
    delete[] colorMapsR;
    delete[] colorMapsG;
    delete[] colorMapsB;
    return initialPixels;
}

extern "C"
jbyteArray
Java_edu_asu_msrs_artcelerationlibrary_graphics_Transform_nativeMotionBlur(
        JNIEnv *env,
        jobject This,
        jbyteArray initialPixels,
        jintArray initialArgs,
        int imgW,
        int imgH) {
    const int length = env->GetArrayLength(initialPixels);
    jbyte *pixels = env->GetByteArrayElements(initialPixels, 0);
    int *intArgs = env->GetIntArrayElements(initialArgs, 0);
    const int orientation = intArgs[0];
    const int radius = intArgs[1];
    // Create two reused 2 dimension jbyte array
    jbyte** originalPixelArray = createOneBlock2DArray(imgW, imgH);
    jbyte** transformedPixelArray = createOneBlock2DArray(imgW, imgH);
    // Blur the red channel
    jbyte **redArray = copyOneColorFromOrigin(pixels, imgW, imgH, 0, originalPixelArray);
    redArray = motionBlurOneColor(redArray, imgW, imgH, orientation, radius, transformedPixelArray);
    copyOneColorToOrigin(redArray, pixels, imgW, imgH, 0);
    // Blur the green channel
    jbyte **greenArray = copyOneColorFromOrigin(pixels, imgW, imgH, 1, originalPixelArray);
    greenArray = motionBlurOneColor(greenArray, imgW, imgH, orientation, radius, transformedPixelArray);
    copyOneColorToOrigin(greenArray, pixels, imgW, imgH, 1);
    // blur the blue channel
    jbyte **blueArray = copyOneColorFromOrigin(pixels, imgW, imgH, 2, originalPixelArray);
    blueArray = motionBlurOneColor(blueArray, imgW, imgH, orientation, radius, transformedPixelArray);
    copyOneColorToOrigin(blueArray, pixels, imgW, imgH, 2);
    // copy the pixels back
    env->SetByteArrayRegion(initialPixels, 0, length, pixels);
    // release resources
    env->ReleaseByteArrayElements(initialPixels, pixels, 0);
    env->ReleaseIntArrayElements(initialArgs, intArgs, 0);
    deleteOneBlock2DArray(originalPixelArray);
    deleteOneBlock2DArray(transformedPixelArray);
    return initialPixels;
}

/**
 * b - brightness of a color, the value in [0, 255]
 * return value is in [0, 255]
 */
int applyColorFilter(int b, int *intArgs, int offset) {
    if (b == 0 || b == 255) {
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
    if (result < 0) {
        result = 0;
    }
    if (result > 255) {
        result = 255;
    }
    return result;
}

/**
 *  pixels and reuse2DArray must be different array
 *  return reuse2DArray if it's not NULL, otherwise return a new created 2D jbyte array.
 */
jbyte **motionBlurOneColor(jbyte **pixels, int imgW, int imgH, int orientation, int radius, jbyte** reuse2DArray) {
    jbyte **newPixels;
    if (reuse2DArray != NULL) {
        newPixels = reuse2DArray;
    }else{
        newPixels = createOneBlock2DArray(imgW, imgH);
    }
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
            newPixels[i][j] = (jbyte) newPixel;
        }
    }
    return newPixels;
}

jbyte **copyOneColorFromOrigin(jbyte *pixels, int imgW, int imgH, int offset, jbyte** reuse2DArray) {
    // offset: r = 0, g = 1, b = 2, a = 3;
    jbyte **result;
    if (reuse2DArray != NULL) {
        result = reuse2DArray;
    }else{
        result = createOneBlock2DArray(imgW, imgH);
    }
    for (int x = 0; x < imgW; x++) {
        for (int y = 0; y < imgH; y++) {
            int index = (y * imgW + x) * 4 + offset;
            result[x][y] = pixels[index];
        }
    }
    return result;
}

// Create a 2D jbyte array with one block memory
jbyte **createOneBlock2DArray(int width, int height) {
    jbyte **result = new jbyte *[width];
    result[0] = new jbyte[width * height];
    for (int i = 1; i < width; i++) {
        result[i] = result[i - 1] + height;
    }
    return result;
}

// Delete a on Block 2D jbyte array
void deleteOneBlock2DArray(jbyte** array){
    delete[] array[0];
    delete[] array;
}

void copyOneColorToOrigin(jbyte **from, jbyte *to, int imgW, int imgH, int offset) {
    // offset: r = 0, g = 1, b = 2, a = 3;
    for (int x = 0; x < imgW; x++) {
        for (int y = 0; y < imgH; y++) {
            int index = (y * imgW + x) * 4 + offset;
            to[index] = from[x][y];
        }
    }
}
extern "C"
jbyteArray
Java_edu_asu_msrs_artcelerationlibrary_graphics_Transform_nativeGaussianBlur(
        JNIEnv *env,
        jobject This,
        jbyteArray initialPixels,
        int imgW,
        int imgH,
        jintArray initialIntArgs,
        jfloatArray initialFloatArgs) {
    int *intArgs = env->GetIntArrayElements(initialIntArgs, 0);
    float *floatArgs = env->GetFloatArrayElements(initialFloatArgs, 0);
    jbyte *pixels = env->GetByteArrayElements(initialPixels, 0);
    const int length = env->GetArrayLength(initialPixels);
    const int radius = intArgs[0];
    const float delta = floatArgs[0];
    const int vectorLen = 2 * radius + 1;
    double *gaussVector = new double[vectorLen];
    double total = 0;
    for (int i = 0; i < vectorLen; i++) {
        gaussVector[i] = exp(-(i - radius) * (i - radius) / (2 * delta * delta)) /
                         sqrt(2 * M_PI * delta * delta);
        total += gaussVector[i];
    }
    // normalize
    for (int i = 0; i < vectorLen; i++) {
        gaussVector[i] = gaussVector[i] / total;
    }
    // create to reused 2D jbyte array
    jbyte **reused2DArray1 = createOneBlock2DArray(imgW, imgH);
    jbyte **reused2DArray2 = createOneBlock2DArray(imgW, imgH);

    for (int i = 0; i < 3; i++) {
        gaussBlurOneColor(pixels, imgW, imgH, i, gaussVector, radius, reused2DArray1,
                          reused2DArray2);
    }
    // set pixels back
    env->SetByteArrayRegion(initialPixels, 0, length, pixels);
    // release resources
    env->ReleaseByteArrayElements(initialPixels, pixels, 0);
    env->ReleaseIntArrayElements(initialIntArgs, intArgs, 0);
    env->ReleaseFloatArrayElements(initialFloatArgs, floatArgs, 0);
    deleteOneBlock2DArray(reused2DArray1);
    deleteOneBlock2DArray(reused2DArray2);
    delete[] gaussVector;
    return initialPixels;
}

void gaussBlurOneColor(jbyte *pixels, int imgW, int imgH, int offset, double *gaussVector,
                       int radius, jbyte **reuse2DArray1, jbyte **reuse2DArray2) {
    // oneColor = reuse2DArray1, if reuse2DArray1 is not NULL
    jbyte **oneColor = copyOneColorFromOrigin(pixels, imgW, imgH, offset, reuse2DArray1);
    // blur x orientation
    // newColor = reuse2DArray2, if reuse2DArray2 is not NULL
    jbyte **newColor = gaussianBlurOneDirection(oneColor, gaussVector, radius, imgW, imgH, 0,
                                                reuse2DArray2);
    // blur y orientation
    newColor = gaussianBlurOneDirection(newColor, gaussVector, radius, imgW, imgH, 1, reuse2DArray1);
    copyOneColorToOrigin(newColor, pixels, imgW, imgH, offset);
}

/**
 *  pixels and reuse2DArray must be different array
 *  return reuse2DArray if it's not NULL, otherwise return a new created 2D jbyte array.
 */
jbyte **gaussianBlurOneDirection(jbyte **pixels, double *gaussVector, int radius, int imgW,
                                 int imgH, int orientation, jbyte **reuse2DArray) {
    // orientation: 0 = horizontal, other = vertical
    jbyte **result;
    if (reuse2DArray != NULL) {
        result = reuse2DArray;
    } else {
        result = createOneBlock2DArray(imgW, imgH);
    }
    int vectorLen = 2 * radius + 1;
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
                result[x][y] = (jbyte) p;
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
                result[x][y] = (jbyte) pixel;
            }
        }
    }
    return result;
}

