package com.keshavg.reddit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/8/16.
 */
public class RedditPostsDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Reddit.db";
    public static final int DATABASE_VERSION = 1;

    public RedditPostsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Creating db", "CREATE TABLE Posts" +
                "(author text, " +
                "created integer, " +
                "numComments integer, " +
                "permalink text, " +
                "score integer, " +
                "subreddit text, " +
                "thumbnail text, " +
                "title text, " +
                "url text)");

        db.execSQL("CREATE TABLE Posts" +
                "(author text, " +
                "created integer, " +
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
        dropTable();
        onCreate(db);
    }

    public void dropTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS Posts");
    }

    public void insertPosts(List<Post> posts) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Post post : posts) {
            ContentValues values = new ContentValues();

            values.put("author", post.getAuthor());
            values.put("created", post.getCreated());
            values.put("numComments", post.getNumComments());
            values.put("permalink", post.getPermalink());
            values.put("score", post.getScore());
            values.put("subreddit", post.getSubreddit());
            values.put("thumbnail", post.getThumbnail());
            values.put("title", post.getTitle());
            values.put("url", post.getUrl());

            db.insert("Posts", null, values);
        }
    }

    public List<Post> getPosts() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "author",
                "created",
                "numComments",
                "permalink",
                "score",
                "subreddit",
                "thumbnail",
                "title",
                "url"
        };

        Cursor cursor = db.query(
                "Posts",
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<Post> posts = new ArrayList<>();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Post post = new Post(cursor.getString(cursor.getColumnIndex("author")),
                    cursor.getLong(cursor.getColumnIndex("created")),
                    cursor.getInt(cursor.getColumnIndex("numComments")),
                    cursor.getString(cursor.getColumnIndex("permalink")),
                    cursor.getInt(cursor.getColumnIndex("score")),
                    cursor.getString(cursor.getColumnIndex("subreddit")),
                    cursor.getString(cursor.getColumnIndex("thumbnail")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("url")));
            posts.add(post);
        }

        return posts;
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Posts", null, null);
    }
}