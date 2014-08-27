package com.adi.awesomeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
    private static final String fileName = "demo_image.jpg";
    private static final int WIDTH = 600;

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
        final int HEIGHT = WIDTH*bitmap.getHeight()/bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, WIDTH, HEIGHT, false);
    }

    /**
     * Base63 encode bitmap
     */
    public static String encodeBitmap(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    /**
     * Percent encode text
     */
    public static String percentEncode(String s) {
        if (s == null) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8")
                    .replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException wow) {
            throw new RuntimeException(wow.getMessage(), wow);
        }
    }

    /**
     * SHA1 functions
     */
    public static byte[] calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);

        return mac.doFinal(data.getBytes());
    }
}
