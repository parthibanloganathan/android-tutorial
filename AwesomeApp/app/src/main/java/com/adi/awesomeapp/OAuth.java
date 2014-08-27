package com.adi.awesomeapp;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class OAuth {
    private static final String TAG = "OAuth";

    public static String generateHeader(String httpMethod, String baseUrl, List<Pair<String, String>> params, Context context) {
        Map<String, String> map = buildHeaderMap(httpMethod, baseUrl, params, context);
        return "OAuth " + getMapString(map);
    }

    private static Map<String, String> buildHeaderMap(String httpMethod, String baseUrl, List<Pair<String, String>> params, Context context) {
        final String TIMESTAMP = String.valueOf(System.currentTimeMillis()/1000);
        final String OAUTH_TOKEN = context.getString(R.string.oauth_token);
        final String OAUTH_CONSUMER_KEY = context.getString(R.string.oauth_consumer_key);
        final String SIGNATURE_METHOD = "HMAC-SHA1";
        final String VERSION = "1.0";
        byte[] randBytes = new byte[32];
        new Random().nextBytes(randBytes);
        final String NONCE = Base64.encodeToString(
                ByteBuffer.allocate(32).put(randBytes).array(),
                Base64.DEFAULT).replaceAll("[^A-Za-z0-9]", "");

        Map<String, String> map = new TreeMap<String, String>();
        encodePut(map, "oauth_consumer_key", OAUTH_CONSUMER_KEY);
        encodePut(map, "oauth_nonce", NONCE);
        encodePut(map, "oauth_signature_method", SIGNATURE_METHOD);
        encodePut(map, "oauth_timestamp", TIMESTAMP);
        encodePut(map, "oauth_token", OAUTH_TOKEN);
        encodePut(map, "oauth_version", VERSION);
        encodePut(map, "oauth_callback", context.getString(R.string.callback_url));

        // Make copy for Signature
        Map<String, String> signatureMap = new TreeMap<String, String>();
        signatureMap.putAll(map);

        // Put params into map
//        for(Pair<String, String> pair : params) {
//            encodePut(signatureMap, pair.first, pair.second);
//        }

        final String SIGNATURE = getSignature(context, httpMethod, baseUrl, getSignatureMapString(signatureMap));
        encodePut(map, "oauth_signature", SIGNATURE);

        return map;
    }

    private static void encodePut(Map map, String key, String value) {
        map.put(Utils.percentEncode(key), Utils.percentEncode(value));
    }

    private static String getMapString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();

        int size = map.size();
        int count = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            count++;
            if (count < size) {
                builder.append(getKVPString(entry.getKey(), entry.getValue(), false));
            } else {
                builder.append(getKVPString(entry.getKey(), entry.getValue(), true));
            }
        }

        return builder.toString();
    }

    private static String getSignatureMapString(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();

        int size = map.size();
        int count = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            count++;
            if (count < size) {
                builder.append(getKVPSignatureString(entry.getKey(), entry.getValue(), false));
            } else {
                builder.append(getKVPSignatureString(entry.getKey(), entry.getValue(), true));
            }
        }

        return builder.toString();
    }

    private static String getKVPSignatureString(String key, String value, boolean isFinal) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(key)
                .append("=")
                .append(value);

        if (!isFinal) {
            builder.append("&");
        }

        return builder.toString();
    }

    private static String getKVPString(String key, String value, boolean isFinal) {
        StringBuilder builder = new StringBuilder();
        builder
                .append(key)
                .append("=")
                .append("\"")
                .append(value)
                .append("\"");

        if (!isFinal) {
            builder.append(", ");
        }

        return builder.toString();
    }

    private static String getSignature(Context context, String httpMethod, String url, String params) {
        StringBuilder builder = new StringBuilder(httpMethod.toUpperCase());
        builder
                .append("&")
                .append(Utils.percentEncode(url))
                .append("&")
                .append(Utils.percentEncode(params));

        String unhashedSignature = builder.toString();

        StringBuilder keyBuilder = new StringBuilder(Utils.percentEncode(context.getString(R.string.consumer_secret)));
        keyBuilder
                .append("&")
                .append(Utils.percentEncode(context.getString(R.string.oauth_token_secret)));

        String key = keyBuilder.toString();

        String signature = null;
        try {
            signature = Base64.encodeToString(Utils.calculateRFC2104HMAC(unhashedSignature, key), Base64.DEFAULT);
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        Log.e(TAG, unhashedSignature);

        return signature;
    }
}
