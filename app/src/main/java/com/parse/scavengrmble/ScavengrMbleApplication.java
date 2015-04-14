package com.parse.scavengrmble;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.PushService;
import com.facebook.FacebookSdk;






/**
 * Created by Rob on 4/10/2015.
 */
public class ScavengrMbleApplication extends Application {
    static final String Tag = "Scavengr";

    public void onCreate() {
        super.onCreate();

		/*
		 * In this tutorial, we'll subclass ParseObjects for convenience to
		 * create and modify Photo objects.
		 *
		 * Also, we'll use an Activity class to keep track of the relationships
		 * of ParseUsers with each other and Photos. Every time a user follows, likes
		 * or comments, a new activity is created to represent the relationship.
		 */
        ParseObject.registerSubclass(Photo.class);
        ParseObject.registerSubclass(Activity.class);

		/*
		 * Fill in this section with your Parse credentials
		 */
        Parse.initialize(this, "TRAtaPKr8X8DrosUrftKnvmjhfwU00FV4JqoyqL6", "P5GrPz6OB5ZOW3UlT57AgC91Nd0x3o4aPnsOTsiy");

        // Set your Facebook App Id in strings.xml
      //  ParseFacebookUtils.initialize(getString(R.string.app_id));
        ParseFacebookUtils.initialize(this);
        FacebookSdk.sdkInitialize(this.getApplicationContext());




		/*
		 * For more information on app security and Parse ACL:
		 * https://www.parse.com/docs/android_guide#security-recommendations
		 */
        ParseACL defaultACL = new ParseACL();

		/*
		 * If you would like all objects to be private by default, remove this
		 * line
		 */
        defaultACL.setPublicReadAccess(true);

		/*
		 * Default ACL is public read access, and user read/write access
		 */
        ParseACL.setDefaultACL(defaultACL, true);

		/*
		 *  Register for push notifications.
		 */
        PushService.setDefaultPushCallback(this, LoginActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
