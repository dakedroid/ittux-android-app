package com.divaga.tecnologico.model;

/**
 * Creado por dakedroid el 11/21/18.
 */
public class Notificacion {


    String title;
    String description;


    public Notificacion() {
    }

    public Notificacion(String title, String description) {
        this.title = title;
        this.description = description;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
