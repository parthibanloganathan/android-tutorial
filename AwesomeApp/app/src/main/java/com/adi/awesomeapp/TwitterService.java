package com.adi.awesomeapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterService {

    private static TwitterService instance;
    private static Twitter twitter;
    private static AccessToken accessToken;
    private static RequestToken requestToken;
    private static final String TAG = "TwitterService";
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String ACCESS_TOKEN_SECRET = "ACCESS_TOKEN_SECRET";
    private static String CALLBACK_URL;
    private static boolean isLoggedIn = false;

    /**
     * Returns a singleton of Twitter object
     */
    public static TwitterService getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "Created new instance");
            instance = new TwitterService(context);
            return instance;
        }
        Log.d(TAG, "Recycling instance");
        return instance;
    }

    /**
     * Private constructor
     */
    private TwitterService(final Context context) {
        twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(context.getString(R.string.oauth_consumer_key),
                context.getString(R.string.oauth_consumer_secret));

        CALLBACK_URL = context.getString(R.string.callback_url);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        twitter.setOAuthAccessToken(new AccessToken(
                preferences.getString(ACCESS_TOKEN, ""),
                preferences.getString(ACCESS_TOKEN_SECRET, "")
        ));

        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Verifying");
                try {
                    Observable.just(twitter.verifyCredentials())
                            .subscribeOn(Schedulers.io())
                            .doOnError(new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    isLoggedIn = false;
                                    requestToken(context);
                                }
                            })
                            .subscribe(new Action1<User>() {
                                @Override
                                public void call(User user) {
                                    if (user == null) {
                                        isLoggedIn = false;
                                        requestToken(context);
                                    } else {
                                        Log.d(TAG, "We're already logged in");
                                        isLoggedIn = true;
                                    }
                                }
                            });
                } catch (TwitterException e) {
                    isLoggedIn = false;
                    requestToken(context);
                }
            }
        }.start();
    }

    private static void requestToken(Context context) {
        Log.d(TAG, "Requesting token");
        try {
            twitter.setOAuthAccessToken(null);
            requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Authenticate user and get access token
     */
    public void authenticate(final Context context, final String oauthVerifier) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Authenticating");
                    Observable.just(twitter.getOAuthAccessToken(requestToken, oauthVerifier))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<AccessToken>() {
                                @Override
                                public void call(AccessToken token) {
                                    Log.d(TAG, "Got access token!");
                                    accessToken = token;
                                    SharedPreferences preferences =
                                            PreferenceManager.getDefaultSharedPreferences(context);
                                    preferences.edit()
                                            .putString(ACCESS_TOKEN, accessToken.getToken())
                                            .putString(ACCESS_TOKEN_SECRET, accessToken.getTokenSecret())
                                            .apply();
                                    isLoggedIn = true;
                                }
                            });
                } catch (TwitterException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Tweet image with text
     */
    public static void tweet(final Context context, String message, File image) {
        StatusUpdate statusUpdate = new StatusUpdate(message);
        statusUpdate.setMedia(image);

        Log.d(TAG, "Attempting to tweet");

        final StatusUpdate status = statusUpdate;

        if (isLoggedIn) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Tweeting...");
                        Observable.just(twitter.updateStatus(status))
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Action1<Status>() {
                                    @Override
                                    public void call(Status status) {
                                        Log.d(TAG, "Tweeted!");
                                        Toast.makeText(context, "Tweeted! Check your feed to see if it worked.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            Log.d(TAG, "Oh no! We're not logged in");
            Toast.makeText(context, "Log in to tweet", Toast.LENGTH_SHORT).show();
        }
    }
}
