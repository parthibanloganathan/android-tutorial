package com.adi.awesomeapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button mButton;
    private ImageView mImage;
    private Uri mUri;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImage = (ImageView) findViewById(R.id.my_image);

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