package com.divaga.tecnologico.model;

import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Creado por Sixto el 09/09/18.
 */
public class Convocatoria {


    private String postId;
    private String username;
    private String user_photo;
    private String description;
    private @ServerTimestamp Date datepublic;

    private String type;
    private String path;
    private Date datelimit;

    public Convocatoria() {
    }

    public Convocatoria(FirebaseUser user, String description, String username, String user_photo, Date datepublic, String category, String type, String path, Date datelimit) {
        this.username = user.getDisplayName();
        if (TextUtils.isEmpty(this.username)) {
            this.username = user.getEmail();
        }

        this.user_photo = user_photo;
        this.description = description;
        this.datepublic = datepublic;
        this.type = type;
        this.path = path;
        this.datelimit = datelimit;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Date getDatepublic() {
        return datepublic;
    }

    public void setDatepublic(Date datepublic) {
        this.datepublic = datepublic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDatelimit() {
        return datelimit;
    }

    public void setDatelimit(Date datelimit) {
        this.datelimit = datelimit;
    }
}
