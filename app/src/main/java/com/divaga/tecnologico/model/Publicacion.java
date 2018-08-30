package com.divaga.tecnologico.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Publicacion POJO.
 */
@IgnoreExtraProperties
public class Publicacion {

    public static final String FIELD_CITY = "city";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_PRICE = "price";
    public static final String FIELD_POPULARITY = "numRatings";
    public static final String FIELD_AVG_RATING = "avgRating";

    private String description;
    private String photo;
    private String username;
    private String user_photo;
    private int numComments;
    private int numLikes;
    private long timecreated;


    public Publicacion() {
    }

    public Publicacion(String description, String photo, String username, String user_photo, int numComments, int numLikes, long timecreated) {

        this.description = description;
        this.photo = photo;
        this.username = username;
        this.user_photo = user_photo;
        this.numComments = numComments;
        this.numLikes = numLikes;
        this.timecreated = timecreated;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public long getTimecreated() {
        return timecreated;
    }

    public void setTimecreated(long timecreated) {
        this.timecreated = timecreated;
    }
}
