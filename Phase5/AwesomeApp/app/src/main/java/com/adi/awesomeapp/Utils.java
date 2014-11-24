package com.adi.awesomeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {
    private static final String fileName = "demo_image.jpg";
    private static final int WIDTH = 400;

    /**
     * Create a new file in external memory and return its URI
     */
    public static Uri getOutputMediaFileUri(Context context) {
        File file = new File(context.getExternalCacheDir(), fileName);
        return Uri.fromFile(file);
    }

    /**
     * Get the image file as a bitmap from the given URI and resize it
     */
    public static Bitmap getResizedBitmapFromUri(Uri uri) {
        File file = new File(uri.getPath());
        return resizeBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
    }

    /**
     * Resize bitmap
     */
    private static Bitmap resizeBitmap(Bitmap bitmap) {
        final int HEIGHT = WIDTH*bitmap.getHeight()/bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
    }

    /**
     * Check if saved image exists
     */
    public static boolean doesSavedImageExist(Context context) {
        File file = new File(context.getExternalCacheDir() + "/" + fileName);
        if(file.exists() && !file.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * Get saved image
     */
    public static File getSavedImage(Context context) {
        return new File(context.getExternalCacheDir() + "/" + fileName);
    }

    /**
     * Convert file to bitmap
     */
    public static Bitmap getResizedBitmapFromFile(File file) {
        return resizeBitmap(BitmapFactory.decodeFile(file.getPath()));
    }

    /**
     * Save bitmap to file
     */
    public static File saveToFile(Context context, Bitmap bitmap) {
        File file = new File(context.getExternalCacheDir(), fileName);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }
}
