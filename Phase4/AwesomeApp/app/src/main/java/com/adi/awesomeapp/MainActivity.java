package com.adi.awesomeapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends Activity {

    private Button mButton;
    private ImageView mImage;
    private Uri mUri;
    private EditText mMessageView;
    private Button mTweetButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage = (ImageView) findViewById(R.id.my_image);
        // If we have an image saved to disk, set it to our ImageView after converting to bitmap and resizing it
        if (Utils.doesSavedImageExist(this)) {
            mImage.setImageBitmap(Utils.getResizedBitmapFromFile(Utils.getSavedImage(this)));
        }

        // Get URI using Utils which does disk I/O that you don't have to worry about
        mUri = Utils.getOutputMediaFileUri(getApplicationContext());

        mButton = (Button) findViewById(R.id.camera_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create the intent
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // Add the output URI as an extra argument in the intent
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

                // Start the image capture Intent
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });

        // This is where the user will enter their tweet message
        mMessageView = (EditText) findViewById(R.id.message);
        final String message = mMessageView.getText().toString();

        /**
         * Clicking the Tweet button triggers a chain of events.
         * We get an instance of TwitterService and ask it to post a
         * tweet with our message and picture. If we're not logged in,
         * TweetService checks performs
         * the necessary OAuth steps and redirects us to a web browser where
         * we can log into Twitter. The browser redirects us back to the app
         * where we can try to tweet again now that we are logged in.
         */
        mTweetButton = (Button) findViewById(R.id.tweet_button);
        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on Tweet button");

                /**
                 * Get the saved image. If there isn't one,
                 * save a file with the current bitmap image
                 */
                File image = null;
                if (Utils.doesSavedImageExist(MainActivity.this)) {
                    image = Utils.getSavedImage(MainActivity.this);
                } else {
                    image = Utils.saveToFile(
                            MainActivity.this,
                            ((BitmapDrawable) mImage.getDrawable()).getBitmap()
                    );
                }

                // Tweet!
                TwitterService.getInstance(MainActivity.this).tweet(MainActivity.this, message, image);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                /**
                 * Image captured and saved to the URI specified in the Intent
                 * so we read the image from disk and set it to our ImageView mImage.
                 */
                Bitmap photo = Utils.getResizedBitmapFromUri(mUri);
                mImage.setImageBitmap(photo);
                Toast.makeText(this, "Picture Taken!", Toast.LENGTH_SHORT);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture, do nothing
            } else {
                // Image capture failed, do nothing
            }
        }
    }
}