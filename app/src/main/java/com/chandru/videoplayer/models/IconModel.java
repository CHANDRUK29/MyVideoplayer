package com.chandru.videoplayer.models;

public class IconModel {
    private int imageView;
    private String iconTitile;

    public IconModel(int imageView, String iconTitile) {
        this.imageView = imageView;
        this.iconTitile = iconTitile;
    }

    public int getImageView() {
        return imageView;
    }

    public void setImageView(int imageView) {
        this.imageView = imageView;
    }

    public String getIconTitile() {
        return iconTitile;
    }

    public void setIconTitile(String iconTitile) {
        this.iconTitile = iconTitile;
    }
}
