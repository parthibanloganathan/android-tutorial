Android Tutorial
================

Android Tutorial for ADI

Phase 1
- Install Android Studio.
- Create New Project.
- Name your application `AwesomeApp`.
- Click Next on "Select the form factors your app will run on".
- Choose Blank Activity.
- Put `MainActivity` under Activity Name and click Next.

Phase 2
- You now have an app with one Activity.
- Let's make it say a message and display an image.
- Open `AwesomeApp/app/src/main/res/layout/activity_main.xml` and delete everything in it. Replace its content with the XML below:
```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <TextView
        android:text="@string/hello_world"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</LinearLayout>
```
We changed the layout from a RelativeLayout to a LinearLayout to make formating easier for the purpose of this tutorial.

- Next, to make a custom message, change `android:text="@string/hello_world"` to `android:text="@string/awesome_message"`.
- Open `AwesomeApp/app/src/main/res/values/strings.xml` and add a new string `<string name="awesome_message">You\'re awesome!</string>`.
- So you modified a TextView to display your own message. How about we take that up a notch and display a picture too. Save your favorite image (I used `puppy.jpg`) to `AwesomeApp/app/src/main/res/drawable-mdpi`. NOTE: In a real app, you would want to have larger sized copies of the image in `res/drawable-hdpi` and `res/drawable-xhdpi` to accomodate a wide variety of screen resolutions.
- In `activity_main.xml`, create an ImageView to display an image. Copy the following XML block below the TextView and nested within the RelativeLayout.
```
<ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="puppy.jpg" />
```

Phase 3
- Puppies are nice, but how about we get a picture of you. We're going to replace that puppy image with your photo using the camera.
- In `activity_main.xml`, create a Button by copying the following XML block above the TextView:
```
<Button
      android:id="@+id/camera_button"
      android:drawableLeft="@android:drawable/ic_menu_camera"
      android:text="Take a Picture"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"/>
```

- Now that we have the layout setup with our view, let's figure out how to actually do something when someone presses the button. Go to `AwesomeApp/app/src/main/java/com/adi/awesomeapp/MainActivity.java` and create a listener for the new button we created so we can respond to button presses. 
- Create a Button field in MainActivity. `private Button mButton;`
- The method `onCreate(Bundle)` handles all the stuff that needs to be done as soon as the activity is created. For more info about Activity life cycle, read [the Android docs](http://developer.android.com/training/basics/activity-lifecycle/starting.html). So we want to point `mButton` to the button view we created in `activity_main.xml` as soon as the user sees this screen. Add `mButton = (Button) findViewById(R.id.camera_button);` after the content view is set to `activity_main`.
- Similarly, create an ImageView field `private ImageView mImage;` and in `onCreate()`, add `mImage = (ImageView) findViewById(R.id.my_image);`. this will let us change the ImageView containing the puppy picture.
- We also need to tell the camera where to save the image it captures. That's where the URI comes in. So go ahead and create an ImageView field `private Uri mUri;` and in `onCreate()`, add `mUri = Utils.getOutputMediaFileUri(getApplicationContext());`
- Now that we have a Button, an ImageView and a file URI, we can add an `OnClickListener` to respond to button clicks. We're going to create an (Intent)[http://developer.android.com/guide/components/intents-filters.html], an object that lets us go to other Activities or apps. Our intent will allow us to take a picture using the camera. (In Android, each app exists in its own process. We could use the (Camera API)[http://developer.android.com/guide/topics/media/camera.html#custom-camera] to build a custom camera, but that's too hard for this tutorial).
- After assigning values to `mUri`, `mImage` and `mButton`, add the following listener for the button:
```
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
```
- Notice that we called the camera using `startActivityForResult`. This means that once the camera finishes taking the picture, it will return to this Activity of our app. We have to be ready to handle this result. So let's create a method `onActivityResult` in our class as follows:
```
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
                Toast.makeText(this, "Worked", Toast.LENGTH_SHORT);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture, do nothing
            } else {
                // Image capture failed, do nothing
            }
        }
    }
```
- It essentially checks if we successfully took a picture and then gets a scaled down bitmap image from the uri that the camera saved its picture to. This bitmap is set to our ImageView. We finally pop up a message saying that the picture was taken.

Phase 4
- Right click on `AwesomeApp/app/src/main/java/com.adi.awesomeapp` (or whatever your project is named). Create a new Activity that is Blank and named `TwitterActivity`.
- To tweet, we're going to need access to the internet.
- You might be wondering how we accessed the camera without permisssions for the Camera `android.permission.CAMERA`. This is because we just offloaded the work to the camera by creating an intent. We never actually accessed the camera directly from our app.

Pre-requisites:
- Knowledge of Java (1004, 1007)
- Desire to learn
- ~~A hatred for iOS~~

Android Terminology:

Views are anthing you can see on your screen.
eg - ImageView, TextView, etc.

Layouts are a means of grouping and organizing views.

Activities are Java classes that loosely corresponds to a screen in your app.

Broadcast receivers "catch" broadcasted messages and respond to them.

Services are tasks that run in the background of your app.

Resources include images, fixed strings, XML data, predefined layouts, etc.
