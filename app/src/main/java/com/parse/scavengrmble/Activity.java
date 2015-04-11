package com.parse.scavengrmble;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ParseFile;


/**
 * Created by Rob on 4/10/2015.
 */

@ParseClassName("Activity")
public class Activity extends ParseObject{
    public Activity() {
        // A default constructor is required.
    }

    public ParseUser getFromUser(){
        return getParseUser("fromUser");
    }

    public void setFromUser(ParseUser user){
        put("fromUser", user);
    }

    public ParseUser getToUser(){
        return getParseUser("toUser");
    }

    public void setToUser(ParseUser user){
        put("toUser", user);
    }

    public String getType(){
        return getString("type");
    }

    public void setType(String t){
        put("type", t);
    }

    public String getContent(){
        return getString("content");
    }

    public void setContent(String c){
        put("content", c);
    }

    public ParseFile getPhoto(){
        return getParseFile("photo");
    }

    public void setPhoto(ParseFile pf){
        put("photo", pf);
    }

}
