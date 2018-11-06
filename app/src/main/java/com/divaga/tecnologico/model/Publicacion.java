package com.divaga.tecnologico.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Publicacion POJO.
 */
@IgnoreExtraProperties
public class Publicacion {

    private String description;
    private String photo;
    private String username;
    private String user_photo;
    private int numComments;
    private @ServerTimestamp Date timestamp;

    //  private @ServerTimestamp Date timestamp;


    public Publicacion() {
    }

    public Publicacion(String description, String photo, String username, String user_photo, int numComments, int numLikes) {

        this.description = description;
        this.photo = photo;
        this.username = username;
        this.user_photo = user_photo;
        this.numComments = numComments;

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
