package com.divaga.tecnologico.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Creado por Sixto el 09/09/18.
 */
public class Aviso {

    private String description;
    private String username;
    private String user_photo;
    private @ServerTimestamp
    Date datepublic;


    public Aviso() {
    }

    public Aviso(String description, String username, String user_photo, Date datepublic, String category, String type, String path, Date datelimit) {
        this.description = description;
        this.username = username;
        this.user_photo = user_photo;
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

}
