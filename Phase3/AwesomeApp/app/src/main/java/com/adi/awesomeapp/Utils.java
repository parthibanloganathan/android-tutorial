package com.adi.awesomeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;

public class Utils {
    private static final String fileName = "demo_image.jpg";
    private static final int WIDTH = 600;
    private static final int HEIGHT = 480;

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
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        return Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
    }
}