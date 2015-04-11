package com.parse.scavengrmble;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Rob on 4/10/2015.
 */
@ParseClassName("Photo")

public class Photo extends ParseObject {

    public Photo(){
        //Default constructor
    }
    public ParseFile getImage(){return getParseFile("image");}
    public void setImage(ParseFile file){put("image",file);}
    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public ParseFile getThumbnail() {
        return getParseFile("thumbnail");
    }

    public void setThumbnail(ParseFile file) {
        put("thumbnail", file);
    }

}
