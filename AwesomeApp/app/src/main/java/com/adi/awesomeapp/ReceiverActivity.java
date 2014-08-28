package com.adi.awesomeapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
