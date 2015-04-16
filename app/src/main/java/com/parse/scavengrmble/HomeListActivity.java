package com.parse.scavengrmble;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
public class HomeListActivity extends ListActivity{
    private HomeViewAdapter mHomeViewAdapter;
    private UserViewAdapter mUserViewAdapter;

    // navigation drawer variables
//    private ListView mDrawerList;
//    private ArrayAdapter<String> mAdapter;
//    private ActionBarDrawerToggle mDrawerToggle;
//    private DrawerLayout mDrawerLayout;
//    private String mActivityTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_list);
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
// Photo clicked == parent.getItemAtPosition(position)
                Toast.makeText(getApplicationContext(),
                        "Item clicked: " + parent.getItemAtPosition(position).getClass().getName(), Toast.LENGTH_SHORT).show();
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

        // navigation drawer create
        //drawerSetup();
        //actionBarSetup();
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
                            currentUser.put("facebookId", user.getId());
                            currentUser.put("displayName", user.getName());
                            currentUser.saveInBackground();
// Make another facebook request to auto follow all of
// the current user's facebook friends who are using Anypic
                            if( currentUser.get("userAlreadyAutoFollowedFacebookFriends")!=null &&
                                    ((Boolean) currentUser.get("userAlreadyAutoFollowedFacebookFriends")) ){
// do nothing
                                Log.i(ScavengrMbleApplication.TAG, "Already followed facebook friends");
                            } else{
                                autoFollowFacebookFriendsRequest();
                            }
// Associate the device with a user
                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                            installation.put("user", currentUser);
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
    /**
     * This function performs a request to the Facebook Graph API, which
     * finds all the friends of the current ParseUser and checks if any
     * of them currently use Anypic. If so, then it automatically follows
     * those friends on Anypic, by creating new Activity relationships.
     */
    private void autoFollowFacebookFriendsRequest(){
        Request request = Request.newMyFriendsRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserListCallback() {
                    @Override
                    public void onCompleted(List<GraphUser> friendList, Response response) {
                        if(friendList != null){
                            List<String> ids = toIdsList(friendList);
// Select * From Users Where User.facebookID is contained in
// the list of IDs of users returned from the GraphApi
                            ParseQuery<ParseUser> friendsQuery = ParseUser.getQuery();
                            friendsQuery.whereContainedIn("facebookId", ids);
                            friendsQuery.findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(List<ParseUser> objects, ParseException e) {
                                    if(e == null && objects!=null){
// friendsQuery successful, follow these users
                                        ParseUser currentUser = ParseUser.getCurrentUser();
                                        for(ParseUser friend : objects){
                                            com.parse.scavengrmble.Activity followActivity = new com.parse.scavengrmble.Activity();
                                            followActivity.setFromUser(currentUser);
                                            followActivity.setToUser(friend);
                                            followActivity.setType("follow");
                                            followActivity.saveEventually();
                                        }
                                        currentUser.put("userAlreadyAutoFollowedFacebookFriends", true);
                                        currentUser.saveInBackground();
                                    } else {
// friendsQuery failed
                                        Log.i(ScavengrMbleApplication.TAG, "Query to find facebook friends in Parse failed");
                                    }
                                }
                            }); // end findInBackground()
// handle errors from facebook
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
                });// end GraphUserListCallback
        request.executeAsync();
    }
    // Take a list of Facebook GraphUsers and return a list of their IDs
    private List<String> toIdsList(List<GraphUser> fbUsers){
        List<String> ids = new ArrayList<String>();
        for(GraphUser user : fbUsers){
            ids.add(user.getId());
        }
        return ids;
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

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        mDrawerToggle.syncState();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
//    }
//
//    // drawer setup
//    public void drawerSetup()
//    {
//        mDrawerList = (ListView)findViewById(R.id.navListHome);
//        String[] optionsArray = { "Create Game", "Account", "Log Out" };
//        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, optionsArray);
//        mDrawerList.setAdapter(mAdapter);
//
//
//        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // home is selected
//                if (position == 0) {
//                    Intent intent = new Intent(view.getContext(), NewPhotoActivity.class);
//                    startActivity(intent);
//                }
//                // Create Game is Selected
//                else if (position == 1) {
//                    Intent intent = new Intent(view.getContext(), AccountActivity.class);
//                    startActivity(intent);
//                }
//                // Log Out is selected
//                //TODO: log out when "Log Out" is pushed
//                else if (position == 2) {
//                    Intent intent = new Intent(view.getContext(), LoginActivity.class);
//                    startActivity(intent);
//                }
//            }
//        });
//    }

//    private void actionBarSetup() {
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout_account);
//        mActivityTitle = getTitle().toString();
//        setupDrawer();
//    }
//
//    private void setupDrawer() {
//        mDrawerToggle = new ActionBarDrawerToggle(this,
//                mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                getSupportActionBar().setTitle("Navigation");
//                invalidateOptionsMenu();
//            }
//            public void onDrawerClosed(View view) {
//                super.onDrawerOpened(view);
//                getSupportActionBar().setTitle(mActivityTitle);
//                invalidateOptionsMenu();
//            }
//        };
//        mDrawerToggle.setDrawerIndicatorEnabled(true);
//        mDrawerLayout.setDrawerListener(mDrawerToggle);
//    }

    // end drawer setup
}
