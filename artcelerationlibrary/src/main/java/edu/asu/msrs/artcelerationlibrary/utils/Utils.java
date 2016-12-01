package edu.asu.msrs.artcelerationlibrary.utils;

import java.lang.reflect.Field;

/**
 * Created by Lei on 11/29/2016.
 */

public class Utils {

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}
