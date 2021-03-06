package com.parse.scavengrmble;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
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
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

    private static final int CAMERA_CAPTURE = 1;
    private static Uri picUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        final ImageView photoView1 = (ImageView) findViewById(R.id.photo1);
        final ImageView photoView2 = (ImageView) findViewById(R.id.photo2);
        final ImageView fbPhotoView = (ImageView) findViewById(R.id.user_thumbnail2);
        final TextView usernameView = (TextView) findViewById(R.id.user_name2);
        final Button confirmButton = (Button) findViewById(R.id.btn_confirm);
        Intent intent = getIntent();
        final String photoObjectId = intent.getStringExtra(INTENT_EXTRA_PHOTO);
        ParseQuery<Photo> query = new ParseQuery<Photo>(Photo.class.getSimpleName());
        try {
            Photo img = query.get(photoObjectId);
            ParseFile imgFile = img.getImage();
            loadImages(imgFile, photoView1);
        } catch (ParseException e) {
            Toast toast = Toast.makeText(this, "no image found", Toast.LENGTH_LONG);
            toast.show();
        }

        Toast toast = Toast.makeText(this, photoObjectId, Toast.LENGTH_LONG);
        toast.show();
        // TODO: call newPhoto() when a button is pressed
        //newPhoto(savedInstanceState);
        // TODO: grab the imageView and the fragmentContainer, convert them to bitmaps and compare them
        try {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(captureIntent, CAMERA_CAPTURE);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast toast2 = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast2.show();
        }

        // confirm button
        confirmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bm1 = ((BitmapDrawable)photoView1.getDrawable()).getBitmap();
                Bitmap bm2 = ((BitmapDrawable)photoView2.getDrawable()).getBitmap();
//                int height = (bm1.getHeight() + bm2.getHeight())/2;
//                int width = (bm1.getWidth() + bm2.getWidth())/2;
//                bm1 = Bitmap.createScaledBitmap(bm1, width, height, false);
//                bm2 = Bitmap.createScaledBitmap(bm2, width, height, false);
                bm2 = Bitmap.createScaledBitmap(bm2, bm1.getWidth(), bm1.getHeight(), false);
                double diff = ImageCompare.compareImagesRGBAll(bm1, bm2);
//                Toast toastComp = Toast.makeText(getApplicationContext(), diff+"% difference", Toast.LENGTH_LONG);
//                toastComp.show();
                if (diff < 15.0) {
                    int points = (int) (100/diff);
                    addPoints(points);
                    Toast successToast = Toast.makeText(getApplicationContext(), "Congrats! "+points+" points added!", Toast.LENGTH_LONG);
                    successToast.show();
                    Intent i = new Intent(PlayGameActivity.this, HomeListActivity.class);
                    startActivity(i);
                }
                else {
                    Toast failToast = Toast.makeText(getApplicationContext(), "Comparison failed, try again!", Toast.LENGTH_LONG);
                    failToast.show();
                    Intent i = new Intent(PlayGameActivity.this, PhotoActivity.class);
                    i.putExtra(PlayGameActivity.INTENT_EXTRA_PHOTO, photoObjectId);
                    startActivity(i);
                }
            }
        });
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //user is returning from capturing an image using the camera
            if(requestCode == CAMERA_CAPTURE){
                //get the Uri for the captured image
                picUri = data.getData();
            }
        }
        //get the returned data
        Bundle extras = data.getExtras();
        //get the cropped bitmap
        Bitmap thePic = extras.getParcelable("data");

        //retrieve a reference to the ImageView
        ImageView picView = (ImageView)findViewById(R.id.photo2);
        //display the returned cropped image
        picView.setImageBitmap(thePic);
    }

    private static void addPoints(int points) {
        ParseQuery<ParseUser> query = new ParseQuery<ParseUser>(ParseUser.class.getSimpleName());
        ParseUser currentUser = ParseUser.getCurrentUser();
        points += currentUser.getInt("userScore");
        currentUser.put("userScore", points);
    }
}