package com.divaga.tecnologico.model;

/**
 * Creado por dakedroid el 03/09/18.
 */
public class Documento {

    private String name;
    private String category;
    private String type;
    private String username;
    private String path;

    public Documento() {
    }

    public Documento(String name, String category, String type, String username, String path) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.username = username;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
