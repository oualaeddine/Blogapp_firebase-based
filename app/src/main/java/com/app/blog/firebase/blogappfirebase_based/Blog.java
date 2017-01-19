package com.app.blog.firebase.blogappfirebase_based;

/**
 * Created by berre on 1/18/2017.
 */

public class Blog {
    private String title, description, image,userName;

    public Blog() {
    }

    public Blog(String title, String description, String image, String userName) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.userName = userName;
    }

    public Blog(String title, String desc, String image) {
        this.title = title;
        this.description = desc;
        this.image = image;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
