package edu.asu.msrs.artcelerationlibrary.utils;

import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Lei on 11/5/2016.
 */

public class ShareMemUtil {

    public static byte[] getBytes(Bitmap bmp) {
        ByteBuffer bb = ByteBuffer.allocate(bmp.getByteCount());
        bmp.copyPixelsToBuffer(bb);
        return bb.array();
        // TODO: remove useless code
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//        return byteArray;
    }

    public static Bitmap createBitmapFromPfd(ParcelFileDescriptor pfd, int width, int height){
        try {
            ByteBuffer buffer = getBytesFromPfd(pfd);
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buffer);
            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ByteBuffer getBytesFromPfd(ParcelFileDescriptor pfd) throws IOException {
        FileInputStream fin = new FileInputStream(pfd.getFileDescriptor());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int read = -1;
        while ((read = fin.read(buff)) != -1) {
            bos.write(buff, 0, read);
        }
        ByteBuffer bb = ByteBuffer.wrap(bos.toByteArray());
        fin.close();
        bos.close();
        return bb;
    }

}
