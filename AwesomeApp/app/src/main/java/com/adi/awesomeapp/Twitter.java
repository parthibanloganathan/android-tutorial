package com.adi.awesomeapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class Twitter {
    private static final String TAG = "Twitter";
    private static final String URL = "https://api.twitter.com";
    private static boolean isAuthenticated = false;
    private static String oauthToken;
    private static String oauthTokenSecret;
    private static TwitterService service;
    private static Twitter instance;

    /**
     * Returns a singleton of Twitter object
     */
    public static Twitter getInstance(Context context) {
        if (instance == null) {
            instance = new Twitter(context);
            return instance;
        }
        return instance;
    }

    /**
     * Private constructor
     */
    private Twitter(final Context context) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(URL)
                .build();

        service = adapter.create(TwitterService.class);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
        Log.e(TAG, "Requesting token");
        final String MOBILE_OAUTH_CALLBACK = context.getString(R.string.callback_url);
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
//        params.add(new Pair<String, String>("oauth_callback", MOBILE_OAUTH_CALLBACK));
//        service.requestToken(MOBILE_OAUTH_CALLBACK,
        service.requestToken(
                OAuth.generateHeader("POST", URL + "/oauth/request_token", params, context))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage());
                    }
                })
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> oauthDetails) {
                        for (String s : oauthDetails) {
                            Log.e(TAG, s);
                        }
                        boolean oauthCallbackConfirmed = Boolean.getBoolean(oauthDetails.get(2));

                        if (oauthCallbackConfirmed) {
                            Log.e(TAG, "Got oauth token");
                            oauthToken = oauthDetails.get(0);
                            oauthTokenSecret = oauthDetails.get(1);
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(URL + "/oauth/authenticate?oauth_token=" + oauthToken));
                            context.startActivity(browserIntent);
                        } else {
                            Log.e(TAG, "Request for token failed");
                        }

                    }
                });


//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }).start();
    }
//
//    /**
//     * Authenticate the user with the pin
//     */
//    private static void checkPin(String pin, final String status, final String encodedImage) {
//        Log.e(TAG, "Called check pin");
//        service.accessToken(pin)
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Action1<List<String>>() {
//                    @Override
//                    public void call(List<String> accessDetails) {
//                        oauthToken = accessDetails.get(0);
//                        oauthTokenSecret = accessDetails.get(1);
//                        Log.e(TAG, "tweeting pic");
//                        //service.tweetPic(status, encodedImage);
//                    }
//                });
//    }
//
//    /**
//     * Pop-up input dialog to receive pin
//     */
//    private static void openDialogForInput(Context context, final String status, final String encodedImage) {
//        final EditText input = new EditText(context);
//        new AlertDialog.Builder(context)
//                .setTitle("Authorize AwesomeApp to tweet")
//                .setMessage("Enter your Twitter pin from the browser:")
//                .setView(input)
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    checkPin(String.valueOf(input.getText()), status, encodedImage);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).start();
//                        isAuthenticated = true;
//                    }
//                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                dialog.cancel();
//            }
//        }).show();
//        Log.e(TAG, "Showed the pin dialog");
//    }

    /**
     * Tweet image with text
     */
    public static void tweet(final Context context, final String status, final Bitmap image) {
        Log.e(TAG, "tweet called");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        try {
            String encodedImage = Utils.encodeBitmap(image);
            if (isAuthenticated) {
                Log.e(TAG, "tweeting pic");
                List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
                params.add(new Pair<String, String>("status", status));
                params.add(new Pair<String, String>("media[]", encodedImage));
                service.tweetPic(status, encodedImage,
                        OAuth.generateHeader("POST", URL + "/1.1/statuses/update_with_media.json", params, context));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//            }
//        }).start();
    }
}