package com.keshavg.reddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/8/16.
 */
public class RedditPostsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Reddit.db";
    public static final int DATABASE_VERSION = 25;

    public RedditPostsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Posts" +
                "(redditLink text, " +
                "name text, " +
                "author text, " +
                "created integer, " +
                "likes integer, " +
                "numComments integer, " +
                "permalink text, " +
                "score integer, " +
                "subreddit text, " +
                "thumbnail text, " +
                "title text, " +
                "url text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        onCreate(db);
    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS Posts");
    }

    public void insertPosts(List<Post> posts, String redditLink) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Post post : posts) {
            ContentValues values = new ContentValues();

            values.put("redditLink", redditLink);
            values.put("name", post.getName());
            values.put("author", post.getAuthor());
            values.put("created", post.getCreated());
            values.put("likes", post.getLikes());
            values.put("numComments", post.getNumComments());
            values.put("permalink", post.getPermalink());
            values.put("score", post.getScore());
            values.put("subreddit", post.getSubreddit());
            values.put("thumbnail", post.getThumbnail());
            values.put("title", post.getTitle());
            values.put("url", post.getUrl());

            db.insert("Posts", null, values);
        }

        db.close();
    }

    public List<Post> getPosts(String redditLink) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "name",
                "author",
                "created",
                "likes",
                "numComments",
                "permalink",
                "score",
                "subreddit",
                "thumbnail",
                "title",
                "url"
        };

        String selection = "redditLink = ?";
        String[] selectionArgs = {redditLink};

        Cursor cursor = db.query(
                "Posts",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<Post> posts = new ArrayList<>();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Post post = new Post(cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("author")),
                    cursor.getLong(cursor.getColumnIndex("created")),
                    cursor.getInt(cursor.getColumnIndex("likes")),
                    cursor.getInt(cursor.getColumnIndex("numComments")),
                    cursor.getString(cursor.getColumnIndex("permalink")),
                    cursor.getInt(cursor.getColumnIndex("score")),
                    cursor.getString(cursor.getColumnIndex("subreddit")),
                    cursor.getString(cursor.getColumnIndex("thumbnail")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("url")));
            posts.add(post);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return posts;
    }

    public void removePosts(String redditLink) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = "redditLink = ?";
        String[] selectionArgs = {redditLink};
        db.delete("Posts", selection, selectionArgs);
        db.close();
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Posts", null, null);
        db.close();
    }
}