//
// Created by Lei on 11/11/2016.
//

#ifndef EEE598PROJ_ASSN2_TRANSFORM_LIB_H
#define EEE598PROJ_ASSN2_TRANSFORM_LIB_H

#endif //EEE598PROJ_ASSN2_TRANSFORM_LIB_H

int applyColorFilter(int b, int *intArgs, int offset);
jbyte **createOneBlock2DArray(int width, int height);
void deleteOneBlock2DArray(jbyte** array);
jbyte **copyOneColorFromOrigin(jbyte *pixels, int imgW, int imgH, int offset, jbyte** reuse2DArray);
void copyOneColorToOrigin(jbyte **from, jbyte *to, int imgW, int imgH, int offset);
jbyte **motionBlurOneColor(jbyte **pixels, int imgW, int imgH, int orientation, int radius, jbyte** reuse2DArray);
jbyte** gaussianBlurOneDirection(jbyte** pixels, double* gaussVector, int radius, int imgW, int imgH, int orientation, jbyte** reuse2DArray);
void gaussBlurOneColor(jbyte *pixels, int imgW, int imgH, int offset, double *gaussVector,
                       int radius, jbyte **reuse2DArray1, jbyte **reuse2DArray2);