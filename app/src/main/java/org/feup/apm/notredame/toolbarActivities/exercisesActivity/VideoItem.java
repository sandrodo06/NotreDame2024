package org.feup.apm.notredame.toolbarActivities.exercisesActivity;

public class VideoItem {

    public String title;
    public String description;
    public String thumbnailAddress;
    public String id;

    public VideoItem(String title, String description, String thumbnailAddress, String id) {
        this.title = title;
        this.description = description;
        this.thumbnailAddress = thumbnailAddress;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailAddress() {
        return thumbnailAddress;
    }

    public String getId() {
        return id;
    }
}
