package com.keshavg.reddit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.keshavg.reddit.Constants.BASE_URL;

public class CommentsActivity extends AppCompatActivity {
    private String url;
    private String title;

    private TextView textView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    private ListView commentsList;
    private CommentsAdapter commentsAdapter;

    private List<Comment> comments;

    private class FetchComments extends AsyncTask<String, Void, List<Comment>> {
        private Boolean ioExceptionFlag, jsonExceptionFlag;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ioExceptionFlag = false;
            jsonExceptionFlag = false;

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Comment> doInBackground(String... params) {
            List<Comment> comments = null;

            try {
                comments = new NetworkTasks().fetchCommentsListFromUrl(params[0]);
            } catch (IOException ioE) {
                ioE.printStackTrace();
                ioExceptionFlag = true;
            } catch (JSONException jsonE) {
                jsonE.printStackTrace();
                jsonExceptionFlag = true;
            }

            return comments;
        }

        @Override
        protected void onPostExecute(List<Comment> comments) {
            super.onPostExecute(comments);

            if (ioExceptionFlag == true) {
                Toast.makeText(CommentsActivity.this, getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                        .show();
            } else if(jsonExceptionFlag == true) {
                Toast.makeText(CommentsActivity.this, String.format(getString(R.string.json_exception), "comments"), Toast.LENGTH_SHORT)
                        .show();
            } else {
                commentsAdapter.addAll(comments);
            }

            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            title = extras.getString("Title");
            url = BASE_URL + "/api/v1" + extras.getString("Url");
            url = url.substring(0, url.length() - 1);
        }

        Log.d("Comment URL", url);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.comments_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewComments(url);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressbar_comments);

        textView = (TextView) findViewById(R.id.post_title);
        textView.setText(title);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getApplicationContext(), url, comments);

        commentsList = (ListView) findViewById(R.id.comments_list);
        commentsList.setAdapter(commentsAdapter);
        commentsList.setFastScrollEnabled(true);
        commentsList.setFastScrollAlwaysVisible(true);

        fetchNewComments(url);
    }

    /**
     * Function to fetch comments from the starting
     * @param url
     */
    public void fetchNewComments(String url) {
        this.url = url;
        commentsAdapter.clear();
        new FetchComments().execute(url);
    }
}