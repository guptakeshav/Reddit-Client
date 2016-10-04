package com.keshavg.reddit.models;

import android.text.format.DateUtils;

import com.google.gson.annotations.SerializedName;
import com.keshavg.reddit.interfaces.Thing;

import java.util.List;

import lombok.Getter;

/**
 * Created by keshav.g on 22/08/16.
 */
public class Post implements Thing {
    private class Preview {
        private class Types {
            private class Image {

                @Getter
                private String url;
            }

            private class ImageTypes {

                @Getter
                private Image gif;
            }

            @Getter
            private Image source;

            @Getter
            private ImageTypes variants;
        }

        private List<Types> images;

        public String getImageUrl() {
            return images.get(0).getSource().getUrl();
        }

        public String getGifUrl() {
            if (images.get(0).getVariants().getGif() != null) {
                return images.get(0).getVariants().getGif().getUrl();
            }

            return null;
        }
    }

    /**
     * JSON model member variables
     */

    @Getter
    @SerializedName("name") private String id;

    @Getter
    private String author;

    @Getter
    @SerializedName("created_utc") private long created;

    @SerializedName("saved") private Boolean isSaved;

    @SerializedName("hidden") private Boolean isHidden;

    @SerializedName("likes") private Boolean isLiked;

    @SerializedName("over_18") private Boolean isNsfw;

    @Getter
    @SerializedName("num_comments") private long numComments;

    @Getter
    private String permalink;

    @Getter
    private long score;

    @Getter
    private String subreddit;

    private Preview preview;

    @Getter
    private String title;

    @Getter
    private String url;

    /**
     * Extra member variables
     */

    @Getter
    private String image;

    /**
     * Constructor
     */

    public Post(String Id,
                String author,
                long created,
                int isSaved,
                int isHidden,
                int isLiked,
                int isNsfw,
                long numComments,
                String permalink,
                long score,
                String subreddit,
                String image,
                String title,
                String url) {
        this.id = Id;
        this.author = author;
        this.created = created;
        this.isSaved = convertIntToBool(isSaved);
        this.isHidden = convertIntToBool(isHidden);
        this.isLiked = convertIntToBool(isLiked);
        this.isNsfw = convertIntToBool(isNsfw);
        this.numComments = numComments;
        this.permalink = permalink;
        this.score = score;
        this.subreddit = subreddit;
        this.image = image;
        this.title = title;
        this.url = url;
    }

    /**
     * Utility functions
     */

    private int convertBoolToInt(Boolean bool) {
        if (bool == null) {
            return 0;
        } else if (bool.equals(true)) {
            return 1;
        }
        return - 1;
    }

    private Boolean convertIntToBool(int number) {
        if (number == 1) {
            return true;
        } else if (number == -1) {
            return false;
        }

        return null;
    }

    /**
     * Getters and Setters
     */

    public String getPostedBy() {
        return "Posted by " + author;
    }

    public String getRelativeCreatedTimeSpan() {
        return DateUtils.getRelativeTimeSpanString(created * 1000L).toString();
    }

    public int getIsSaved() {
        return convertBoolToInt(isSaved);
    }

    public void setIsSaved(int saved) {
        isSaved = convertIntToBool(saved);
    }

    public int getIsHidden() {
        return convertBoolToInt(isHidden);
    }

    public void setIsHidden(int hidden) {
        isHidden = convertIntToBool(hidden);
    }

    public int getLikes() {
        return convertBoolToInt(isLiked);
    }

    public void setIsLiked(int isLiked) {
        this.isLiked = convertIntToBool(isLiked);
    }

    public int getIsNsfw() {
        return convertBoolToInt(isNsfw);
    }

    public void setIsNsfw(int nsfw) {
        isNsfw = convertIntToBool(nsfw);
    }

    public String getCommentsCount() {
        return Long.toString(numComments) + " comments";
    }

    public void updateScore(int delta) {
        this.score += delta;
    }

    public String getFormattedSubreddit() {
        return "r/" + subreddit;
    }

    /**
     * Fixing the model
     */

    public void fixPermalink() {
        int idx = permalink.lastIndexOf('/');
        permalink = permalink.substring(0, idx);
    }

    public void fixImage() {
        if (preview != null) {
            if (preview.getGifUrl() != null) {
                image = preview.getGifUrl();
            } else {
                image = preview.getImageUrl();
            }
        } else {
            image = "";
        }
    }
}