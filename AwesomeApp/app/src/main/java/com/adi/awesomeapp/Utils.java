package com.adi.awesomeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;

public class Utils {
    private static final String fileName = "demo_image.jpg";

    public static Uri getOutputMediaFileUri(Context context) {
        File file = new File(context.getFilesDir(), fileName);
        return Uri.fromFile(file);
    }

    public static Bitmap getBitmapFromUri(Uri uri) {
        File file = new File(uri.getPath());
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}