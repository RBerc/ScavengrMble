package com.parse.scavengrmble;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
public class HomeListActivity extends ListActivity {
    private Activity mActivity = this;
    private HomeViewAdapter mHomeViewAdapter;
    private UserViewAdapter mUserViewAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Photo photo =
                        (Photo) parent.getItemAtPosition(position);
                Intent intent = new Intent(mActivity, PhotoActivity.class);
                intent.putExtra(PhotoActivity.INTENT_EXTRA_PHOTO, photo.getObjectId());
                mActivity.startActivity(intent);
            }
        });
// Subclass of ParseQueryAdapter
        mHomeViewAdapter = new HomeViewAdapter(this);
        mUserViewAdapter = new UserViewAdapter(this);
// Default view
        setListAdapter(mHomeViewAdapter);
// Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
//Log.i(AnypicApplication.TAG, "Entered HomeListActivity onResume()");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
// Check if the user is currently logged
// and show any cached content
        } else {
// If the user is not logged in, go to the
// activity showing the login view.
            startLoginActivity();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_menu, menu);
        return true;
    }
    /*
    * Posting pictures and refreshing the list will be controlled from the Action
    * Bar.
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                updateHomeList();
                break;
            }
            case R.id.action_person: {
                showUser();
                break;
            }
            case R.id.action_new: {
                newPhoto();
                break;
            }
            case R.id.logout: {
                onLogoutButtonClicked();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateHomeList() {
        mHomeViewAdapter.loadObjects();
        setListAdapter(mHomeViewAdapter);
    }
    private void showUser() {
        mUserViewAdapter.loadObjects();
        setListAdapter(mUserViewAdapter);
    }
    private void newPhoto() {
        Intent i = new Intent(this, NewPhotoActivity.class);
        startActivityForResult(i, 0);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
// If a new post has been added, update
// the list of posts
            updateHomeList();
        }
    }
    /**
     * Requesting and setting user data. Essentially, this is the User constructor
     */
    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
// get the relevant data using the GraphAPI
// and store them as fields in our ParseUser
/*
* User Model
*
* displayName : String
* email : string
* profilePictureMedium : File
* profilePictureSmall : File
* facebookId : String
* facebookFriends : Array
* channel : String
* userAlreadyAutoFollowedFacebookFriends : boolean
*/
                            ParseUser currentUser = ParseUser
                                    .getCurrentUser();
                            currentUser.put(ParseColumn.USER_FACEBOOK_ID, user.getId());
                            currentUser.put(ParseColumn.USER_DISPLAY_NAME, user.getName());
                            currentUser.saveInBackground();
// Associate the device with a user
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put(ParseColumn.INSTALLATION_USER, currentUser);
                            installation.saveInBackground();
// handle errors accessing data from facebook
                        } else if (response.getError() != null) {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
                                    || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                Log.i(ScavengrMbleApplication.TAG,
                                        "The facebook session was invalidated.");
                                onLogoutButtonClicked();
                            } else {
                                Log.i(ScavengrMbleApplication.TAG,
                                        "Some other error: "
                                                + response.getError()
                                                .getErrorMessage());
                            }
                        }
                    }
                });
        request.executeAsync();
    }
    private void onLogoutButtonClicked() {
// close this user's session
        ParseFacebookUtils.getSession().closeAndClearTokenInformation();
// Log the user out
        ParseUser.logOut();
// Go to the login view
        startLoginActivity();
    }
    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
