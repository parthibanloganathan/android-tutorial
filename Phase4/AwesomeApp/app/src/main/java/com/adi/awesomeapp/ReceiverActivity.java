package com.adi.awesomeapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * The sole purpose of this Activity is to be started
 * and respond to the Intent when the browser redirects
 * the user to the callback url. This Activity captures the intent
 * thanks to the Intent Filter we added in the AndroidManifest.
 * It extracts the oauth_verifier from the intent and authenticates the user.
 */
public class ReceiverActivity extends Activity {
    private static final String TAG = "ReceiverActivity";

    /**
     * Handle Twitter callback after signing in
     */
    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "Received intent");
        super.onNewIntent(intent);
        parseTwitterCallback(intent);
    }

    private void parseTwitterCallback(Intent intent) {
        Uri uri = intent.getData();
        final String CALLBACK_URL = this.getString(R.string.callback_url);
        if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
            String oauthVerifier = uri.getQueryParameter("oauth_verifier");
            TwitterService.getInstance(this).authenticate(this, oauthVerifier);
        }

        Toast.makeText(this, "You're logged in! Try pressing 'Tweet' again.", Toast.LENGTH_LONG).show();

        startActivity(new Intent(this, MainActivity.class));
    }
}
