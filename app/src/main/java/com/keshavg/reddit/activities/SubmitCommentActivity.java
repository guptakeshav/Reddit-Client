package com.keshavg.reddit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.models.Comment;
import com.keshavg.reddit.models.SubmitCommentResponse;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitCommentActivity extends AppCompatActivity {
    public static final String ID = "ID";
    public static final String PARENT_ID = "PARENT_ID";
    public static final String COMMENT = "COMMENT";

    private String id;
    private String commentBody;
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_comment);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras.containsKey(PARENT_ID)) {
                parentId = extras.getString(PARENT_ID);
            } else {
                parentId = null;
            }

            if (extras.containsKey(ID)) {
                id = extras.getString(ID);
                commentBody = extras.getString(COMMENT);
            } else {
                id = null;
                commentBody = null;
            }
        }

        final EditText text = (EditText) findViewById(R.id.comment);
        if (id != null) {
            text.setText(commentBody);
        }

        Button submit = (Button) findViewById(R.id.comment_submit);
        Button discard = (Button) findViewById(R.id.comment_discard);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id != null) {
                    editComment(text.getText().toString());
                } else {
                    submitComment(text.getText().toString());
                }
            }
        });

        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(SubmitCommentActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void editComment(String text) {
        RedditApiInterface apiClient = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<SubmitCommentResponse> call = apiClient.editComment(
                "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                1,
                "json",
                text,
                id
        );

        call.enqueue(new Callback<SubmitCommentResponse>() {
            @Override
            public void onResponse(Call<SubmitCommentResponse> call, Response<SubmitCommentResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getErrors().size() == 0) {
                        Comment editComment = response.body().getSubmittedComment();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(ID, id);
                        resultIntent.putExtra(COMMENT, editComment);

                        setResult(CommentsActivity.RESULT_EDIT_COMMENT, resultIntent);
                        finish();
                    } else {
                        for (List<String> error : response.body().getErrors()) {
                            String errorString = "";
                            for (String err : error) {
                                errorString += err + "\n";
                            }

                            showToast(errorString);
                        }
                    }
                } else {
                    showToast("Submitting - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubmitCommentResponse> call, Throwable t) {
                showToast(getString(R.string.server_error));
            }
        });
    }

    private void submitComment(String text) {
        RedditApiInterface apiClient = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<SubmitCommentResponse> call = apiClient.submitComment(
                "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                1,
                "json",
                text,
                parentId
        );

        call.enqueue(new Callback<SubmitCommentResponse>() {
            @Override
            public void onResponse(Call<SubmitCommentResponse> call, Response<SubmitCommentResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getErrors().size() == 0) {
                        Comment submittedComment = response.body().getSubmittedComment();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(PARENT_ID, parentId);
                        resultIntent.putExtra(COMMENT, submittedComment);

                        setResult(CommentsActivity.RESULT_SUBMIT_COMMENT, resultIntent);
                        finish();
                    } else {
                        for (List<String> error : response.body().getErrors()) {
                            String errorString = "";
                            for (String err : error) {
                                errorString += err + "\n";
                            }

                            showToast(errorString);
                        }
                    }
                } else {
                    showToast("Submitting - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubmitCommentResponse> call, Throwable t) {
                showToast(getString(R.string.server_error));
            }
        });
    }
}
