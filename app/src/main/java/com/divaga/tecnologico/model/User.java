package com.divaga.tecnologico.model;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Creado por dakedroid el 03/09/18.
 */
public class User {

    private String userId;
    private String userName;
    private String photo;
    private String email;
    private String permisos;

    public User() {
    }

    public User(FirebaseUser user, String userName, String permisos, String photoUrl) {

        this.userId = user.getUid();
        this.email = user.getEmail();
        this.photo = photoUrl;
        this.userName = userName;
        this.permisos = permisos;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
