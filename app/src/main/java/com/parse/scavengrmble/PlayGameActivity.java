package com.parse.scavengrmble;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PlayGameActivity extends android.app.Activity {
    public static final String INTENT_EXTRA_PHOTO = "photo";
    android.app.Activity mActivity = this;
    Photo mPhoto;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int REQ_WIDTH = 560;
    private static final int REQ_HEIGHT = 560;
    private Photo photo;
    private Uri fileUri;
    private ParseFile image;
    private ParseFile thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        final ImageView photoView1 = (ImageView) findViewById(R.id.photo1);
        final ImageView fbPhotoView = (ImageView) findViewById(R.id.user_thumbnail2);
        final TextView usernameView = (TextView) findViewById(R.id.user_name2);
        final ProgressBar loadingProgress = (ProgressBar) findViewById(R.id.loading_progress2);
        final Button confirmButton = (Button) findViewById(R.id.btn_confirm);
        Intent intent = getIntent();
        String photoObjectId = intent.getStringExtra(INTENT_EXTRA_PHOTO);
        ParseQuery<Photo> query = new ParseQuery<Photo>(Photo.class.getSimpleName());
        query.whereEqualTo(ParseColumn.OBJECT_ID, photoObjectId);
        query.include(Photo.USER);
        query.getFirstInBackground(new GetCallback<Photo>() {
            @Override
            public void done(final Photo photo, ParseException e) {
                mPhoto = photo;
                ParseUser user = photo.getUser();
                Picasso.with(mActivity)
                        .load("https://graph.facebook.com/" + user.getString(ParseColumn.USER_FACEBOOK_ID)
                                + "/picture?type=square")
                        .into(fbPhotoView);
                usernameView.setText((String) user.get(ParseColumn.USER_DISPLAY_NAME));
                ParseFile photoFile = photo.getImage();
                if (photoFile != null) {
                    Picasso.with(mActivity)
                            .load(photoFile.getUrl())
                            .placeholder(new ColorDrawable(Color.LTGRAY))
                            .into(photoView1);
                } else { // Clear ParseImageView if an object doesn't have a photo
                    photoView1.setImageResource(android.R.color.transparent);
                }
                ParseQuery<Activity> likeQuery = new ParseQuery<Activity>(Activity.class.getSimpleName());
                likeQuery.whereEqualTo(Activity.TYPE, Activity.TYPE_LIKE);
                likeQuery.include(Activity.FROM_USER);
                likeQuery.whereExists(Activity.PHOTO);
                likeQuery.whereEqualTo(Activity.PHOTO, photo);
            }
        });
        // TODO: call newPhoto() when a button is pressed
        newPhoto(savedInstanceState);
        // TODO: grab the imageView and the fragmentContainer, convert them to bitmaps and compare them
    }

    private void newPhoto(Bundle savedInstanceState) {
        photo = new Photo();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_photo);

        // Open the camera or gallery using an Intent
        startCamera();

        // After taking a picture, open the NewPhotoFragment
        FragmentManager manager = getFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer2);

        if (fragment == null) {
            fragment = new NewPhotoFragment();
            // add id of the FrameLayout to fill, and the fragment that the layout will hold
            manager.beginTransaction().add(R.id.fragmentContainer2, fragment)
                    .commit();
        }
    }

    public void startCamera(){
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image

        // Camera intents
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            cameraIntents.add(intent);
        }

        // Filesystem intents (to upload from Gallery, Dropbox, etc.)
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

        // start the image capture Intent
        startActivityForResult(chooserIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private static Uri getOutputMediaFileUri(int type){
        File output = getOutputMediaFile(type);
        if(output!=null){
            return Uri.fromFile(output);
        } else {
            return null;
        }
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        Log.i(ScavengrMbleApplication.TAG, "entering getOutputMediaFile");

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Anypic");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        // Make sure you have the permission to write to the SD Card enabled
        // in order to do this!!
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.i(ScavengrMbleApplication.TAG, "getOutputMediaFile failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
