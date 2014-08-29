package com.adi.awesomeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * TwitterService uses the libraries
 * twitter4j (http://twitter4j.org/en/) and
 * RxJava (https://github.com/ReactiveX/RxJava)
 * to make API requests to Twitter simple and neat.
 */
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
        /**
         * Get a twitter object and set the consumer key and consumer secret.
         * These values can be found at https://apps.twitter.com/ if you've
         * created a Twitter app.
         */
        twitter = TwitterFactory.getSingleton();
        twitter.setOAuthConsumer(context.getString(R.string.oauth_consumer_key),
                context.getString(R.string.oauth_consumer_secret));

        /**
         * Set the callback url so that Twitter knows where to redirect to
         * once we sign in via the browser
         */
        CALLBACK_URL = context.getString(R.string.callback_url);

        /**
         * If we already have an access token, there's no need
         * to sign in again. So check the SharedPreferences for a cached access token.
         */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        twitter.setOAuthAccessToken(new AccessToken(
                preferences.getString(ACCESS_TOKEN, ""),
                preferences.getString(ACCESS_TOKEN_SECRET, "")
        ));

        /**
         * Network calls have to be made on a new thread since the
         * main thread handles UI and you never want to do synchronous
         * blocking operations there.
         */
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Verifying");
                try {
                    /**
                     * This is an Observable. They help us do lots of nifty
                     * function stuff which is helpful when dealing with asynchronous tasks.
                     * We don't really use the full power of Observables here, but this is a
                     * good intro to use them. Read more about them at
                     * https://github.com/ReactiveX/RxJava/wiki/Observable
                     *
                     * Here we observe the API call
                     * verifyCredentials and we subscribe an action
                     * to react to the output of the call when it is
                     * complete. We decide whether to request a token or not.
                     */
                    Observable.just(twitter.verifyCredentials())
                            .subscribeOn(Schedulers.io())
                            .doOnError(new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    requestToken(context);
                                }
                            })
                            .subscribe(new Action1<User>() {
                                @Override
                                public void call(User user) {
                                    if (user == null) {
                                        requestToken(context);
                                    } else {
                                        Log.d(TAG, "We're already logged in");
                                        isLoggedIn = true;
                                    }
                                }
                            });
                } catch (TwitterException e) {
                    requestToken(context);
                }
            }
        }.start();
    }

    /**
     * Gets a request token
     *
     * If we decided that we need to do OAuth again to sign in,
     * we get a request token from Twitter via this call.
     */
    private static void requestToken(Context context) {
        Log.d(TAG, "Requesting token");
        isLoggedIn = false;
        try {
            /**
             * We say that we don't have an access token and then
             * ask for a request token. We also tell Twitter to redirect
             * us to the callback url when the user finishes signing in via the browser.
             * Twitter gives us the request token and a url to redirect the user to
             * for them to sign in. This is all part of the OAuth process.
             */
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

                    /**
                     * Now that the user has signed in, we use the OAuth verifier and
                     * the request token we got earlier to get an access token. With
                     * this access token, we can freely post tweets and do other fun
                     * stuff on Twitter.
                     *
                     * Here, we create an Observable from the getOAuthAccessToken result.
                     * When the API call responds, our Observable emits an AccessToken object.
                     * Since we subscribed to the Observable, we are ready to perform an action
                     * as soon as we get teh AccessToken.
                     */
                    Observable.just(twitter.getOAuthAccessToken(requestToken, oauthVerifier))
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<AccessToken>() {
                                @Override
                                public void call(AccessToken token) {
                                    Log.d(TAG, "Got access token!");
                                    accessToken = token;

                                    /**
                                     * Save the access token to SharedPreferences for future use
                                     */
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
        /**
         * Put the tweet message and image in the status object
         */
        StatusUpdate statusUpdate = new StatusUpdate(message);
        statusUpdate.setMedia(image);
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            statusUpdate.setLocation(new GeoLocation(latitude, longitude));
        }

        Log.d(TAG, "Attempting to tweet");

        final StatusUpdate status = statusUpdate;

        /**
         * If the user is logged in, we tweet!
         * Once again, we use the Observable pattern.
         */
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
                                        if (context instanceof Activity) {
                                            ((Activity) context).runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(context, "Tweeted! Check your feed to see if it worked.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
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
