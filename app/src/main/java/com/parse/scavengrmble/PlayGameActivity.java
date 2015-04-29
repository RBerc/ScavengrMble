package com.parse.scavengrmble;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PlayGameActivity extends android.app.Activity {
    public static String INTENT_EXTRA_PHOTO = "photo";
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
//        Bitmap currentImg = BitmapFactory.decodeResource(getResources(), photoView1.getId());
//        Bundle prevBundle = getIntent().getBundleExtra("data");
//        Bitmap prevImgBM = (Bitmap) prevBundle.get("data");
//        photoView1.setImageBitmap(prevImgBM);
//        Bitmap b = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("data"),0,getIntent().getByteArrayExtra("data").length);
//        photoView1.setImageBitmap(b);

        final ImageView fbPhotoView = (ImageView) findViewById(R.id.user_thumbnail2);
        final TextView usernameView = (TextView) findViewById(R.id.user_name2);
        final ProgressBar loadingProgress = (ProgressBar) findViewById(R.id.loading_progress2);
        final Button confirmButton = (Button) findViewById(R.id.btn_confirm);
       loadingProgress.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startCamera();
           }
       });
        Intent intent = getIntent();
        String photoObjectId = intent.getStringExtra(INTENT_EXTRA_PHOTO);
        ParseQuery<Photo> query = new ParseQuery<Photo>(Photo.class.getSimpleName());
        try {
            Photo img = query.get(photoObjectId);
            ParseFile imgFile = img.getImage();
            loadImages(imgFile, photoView1);
        } catch (ParseException e) {
        }


        Toast toast = Toast.makeText(this, photoObjectId, Toast.LENGTH_LONG);
        toast.show();
        // TODO: call newPhoto() when a button is pressed


      //  newPhoto(savedInstanceState);
        // TODO: grab the imageView and the fragmentContainer, convert them to bitmaps and compare them
    }

    private void loadImages(ParseFile thumbnail, final ImageView img) {
        if (thumbnail != null) {
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,
                                data.length);
                        img.setImageBitmap(bmp);
                    } else {
                    }
                }
            });
        } else {
            img.setImageResource(R.drawable.scavengrmobile);
        }

    }// load image

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
