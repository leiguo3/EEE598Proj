package edu.asu.msrs.artcelerationlibrary.utils;

import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Lei on 11/5/2016.
 * Copied from https://github.com/roblkw-asu/Artceleration-EEE598-Assn2/blob/master/artcelerationlibrary/src/main/java/edu/asu/msrs/artcelerationlibrary/MemoryFileUtil.java
 */

public class MemoryFileUtil {
    private static final Method sMethodGetFileDescriptor;
    static {
        sMethodGetFileDescriptor = get("getFileDescriptor");
    }


    public static FileDescriptor getFileDescriptor(MemoryFile file) {
        try {
            return (FileDescriptor) sMethodGetFileDescriptor.invoke(file);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    public static ParcelFileDescriptor getParcelFileDescriptor(MemoryFile file) throws IOException {
        return ParcelFileDescriptor.dup(getFileDescriptor(file));

    }
    private static Method get(String name) {
        try {
            return MemoryFile.class.getDeclaredMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
