Android Tutorial
================

Android Tutorial for ADI

Phase 1
- Install Android Studio
- Create New Project
- Name your application `AwesomeApp`
- Click Next on "Select the form factors your app will run on"
- Choose Blank Activity with Fragment
- Put `MainActivity` under Activity Name and click Next

Phase 2
- You now have an app with one Activity
- Let's make it say a message and display an image
- Open `AwesomeApp/app/src/main/res/layout/activity_main.xml` and change `android:text="@string/hello_world"` to `android:text="@string/awesome_message"`
- Open `AwesomeApp/app/src/main/res/values/strings.xml` and add a new string `<string name="awesome_message">You're awesome!</string>`
- So you modified a TextView to display your own message. How about we take that up a notch and display a picture too. Save your favorite image (I used `puppy.jpg`) to `AwesomeApp/app/src/main/res/drawable-mdpi`. NOTE: In a real app, you would want to have larger sized copies of the image in `res/drawable-hdpi` and `res/drawable-xhdpi` to accomodate a wide variety of screen resolutions.
- In `activity_main.xml`, create an ImageView to display an image. Copy the following xml block below the TextView and nested within the RelativeLayout.
`<ImageView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:src="puppy.jpg" />`

Phase 3
- Puppies are nice, but we want a picture of you. Let's fix that.
- Go to 
- 

Pre-requisites:
- Knowledge of Java (1004, 1007)
- Desire to learn
- ~~Hatred for iOS~~

Android Terminology:

Views are anthing you can see on your screen.
eg - ImageView, TextView, etc.

Layouts are a means of grouping and organizing views.

Activities are Java classes that loosely corresponds to a screen in your app.

Broadcast receivers "catch" broadcasted messages and respond to them.

Services are tasks that run in the background of your app.

Resources include images, fixed strings, XML data, predefined layouts, etc.
