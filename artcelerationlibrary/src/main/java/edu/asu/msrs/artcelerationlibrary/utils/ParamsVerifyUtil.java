package edu.asu.msrs.artcelerationlibrary.utils;

/**
 * Created by Lei on 11/6/2016.
 */

public class ParamsVerifyUtil {
    public static boolean verifyColorFilterParams(int[] intArgs) {
        if (intArgs == null || intArgs.length != 24) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 6; j += 2) {
                int index = j + i * 8;
                if (intArgs[index + 2] <= intArgs[index]) {
                    // p0 should be in increasing order.
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean verifyMotionBlurParams(int[] intArgs){
        if (intArgs == null || intArgs.length != 2) {
            return false;
        }
        if(intArgs[0] != 0 && intArgs[0] != 1){
            return false;
        }
        if(intArgs[1] < 0){
            return false;
        }
        return true;
    }
}
