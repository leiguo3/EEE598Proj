//
// Created by Lei on 11/10/2016.
//
#include <jni.h>
#include <string>
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
        colorMapsR[i] = (jbyte)transformOneColor(i, args, 0);
        colorMapsG[i] = (jbyte)transformOneColor(i, args, 8);
        colorMapsB[i] = (jbyte)transformOneColor(i, args, 16);
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
    env->SetByteArrayRegion(initialPixels, 0, length, pixels);
    env->ReleaseByteArrayElements(initialPixels, pixels, 0);
    env->ReleaseIntArrayElements(intArgs, args, 0);
    return initialPixels;
}

/**
 * b - brightness of a color, the value in [0, 255]
 * return value is in [0, 255]
 */
int transformOneColor(int b, int *intArgs, int offset) {
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

