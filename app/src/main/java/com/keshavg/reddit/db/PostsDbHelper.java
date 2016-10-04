package com.keshavg.reddit.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.keshavg.reddit.models.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/8/16.
 */
public class PostsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Reddit.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Posts";

    private static final String COLUMN_REDDIT_LINK = "reddit_link";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AUTHOR = "author";
    private static final String COLUMN_CREATED = "created";
    private static final String COLUMN_IS_HIDDEN = "is_hidden";
    private static final String COLUMN_IS_SAVED = "is_saved";
    private static final String COLUMN_IS_LIKED = "is_liked";
    private static final String COLUMN_IS_NSFW = "is_nsfw";
    private static final String COLUMN_NUM_COMMENTS = "num_comments";
    private static final String COLUMN_PERMALINK = "permalink";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_SUBREDDIT = "subreddit";
    private static final String COLUMN_IMAGE = "image";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_URL = "url";

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_INT = "integer";

    public PostsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_REDDIT_LINK + " " + TYPE_TEXT + ", " +
                        COLUMN_ID + " " + TYPE_TEXT + ", " +
                COLUMN_AUTHOR + " " + TYPE_TEXT + ", " +
                COLUMN_CREATED + " " + TYPE_INT + ", " +
                COLUMN_IS_HIDDEN + " " + TYPE_INT + ", " +
                COLUMN_IS_SAVED + " " + TYPE_INT + ", " +
                COLUMN_IS_LIKED + " " + TYPE_INT + ", " +
                COLUMN_IS_NSFW + " " + TYPE_INT + ", " +
                COLUMN_NUM_COMMENTS + " " + TYPE_INT + ", " +
                COLUMN_PERMALINK + " " + TYPE_TEXT + ", " +
                COLUMN_SCORE + " " + TYPE_INT + ", " +
                COLUMN_SUBREDDIT + " " + TYPE_TEXT + ", " +
                COLUMN_IMAGE + " " + TYPE_TEXT + ", " +
                COLUMN_TITLE + " "+ TYPE_TEXT + ", " +
                COLUMN_URL + " " + TYPE_TEXT + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        onCreate(db);
    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void insertPosts(List<Post> posts, String redditLink) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Post post : posts) {
            ContentValues values = new ContentValues();

            values.put(COLUMN_REDDIT_LINK, redditLink);
            values.put(COLUMN_ID, post.getId());
            values.put(COLUMN_AUTHOR, post.getAuthor());
            values.put(COLUMN_CREATED, post.getCreated());
            values.put(COLUMN_IS_HIDDEN, post.getIsHidden());
            values.put(COLUMN_IS_SAVED, post.getIsSaved());
            values.put(COLUMN_IS_LIKED, post.getLikes());
            values.put(COLUMN_IS_NSFW, post.getIsNsfw());
            values.put(COLUMN_NUM_COMMENTS, post.getNumComments());
            values.put(COLUMN_PERMALINK, post.getPermalink());
            values.put(COLUMN_SCORE, post.getScore());
            values.put(COLUMN_SUBREDDIT, post.getSubreddit());
            values.put(COLUMN_IMAGE, post.getImage());
            values.put(COLUMN_TITLE, post.getTitle());
            values.put(COLUMN_URL, post.getUrl());

            db.insert(TABLE_NAME, null, values);
        }

        db.close();
    }

    public List<Post> getPosts(String redditLink) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_ID,
                COLUMN_AUTHOR,
                COLUMN_CREATED,
                COLUMN_IS_HIDDEN,
                COLUMN_IS_SAVED,
                COLUMN_IS_LIKED,
                COLUMN_IS_NSFW,
                COLUMN_NUM_COMMENTS,
                COLUMN_PERMALINK,
                COLUMN_SCORE,
                COLUMN_SUBREDDIT,
                COLUMN_IMAGE,
                COLUMN_TITLE,
                COLUMN_URL
        };

        String selection = COLUMN_REDDIT_LINK + " = ?";
        String[] selectionArgs = {redditLink};

        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<Post> posts = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Post post = new Post(
                    cursor.getString(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_AUTHOR)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_IS_SAVED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_IS_HIDDEN)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_IS_LIKED)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_IS_NSFW)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_NUM_COMMENTS)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PERMALINK)),
                    cursor.getLong(cursor.getColumnIndex(COLUMN_SCORE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SUBREDDIT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_URL))
            );
            posts.add(post);

            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return posts;
    }

    public void removePosts(String redditLink) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_REDDIT_LINK + " = ?";
        String[] selectionArgs = {redditLink};
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}