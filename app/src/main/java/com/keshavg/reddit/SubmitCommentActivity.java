package com.keshavg.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fiberlink.maas360.android.richtexteditor.RichEditText;
import com.fiberlink.maas360.android.richtexteditor.RichTextActions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitCommentActivity extends AppCompatActivity {
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_comment);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            parentId = extras.getString("PARENT_ID");
        }

        final RichEditText richEditText = (RichEditText) findViewById(R.id.rich_edit_text);
        final RichTextActions richTextActions = (RichTextActions) findViewById(R.id.rich_text_actions);
        richEditText.setRichTextActionsView(richTextActions);
        richEditText.setHint("Enter your comment here...");

        final Button submit = (Button) findViewById(R.id.comment_submit);
        final Button discard = (Button) findViewById(R.id.comment_discard);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment(richEditText.getHtml());
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

    private void submitComment(String text) {
        ApiInterface apiClient = ApiClient.getOAuthClient().create(ApiInterface.class);
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
                    if (response.body().getError() == null) {
                        Comment submittedComment = response.body().getSubmittedComment();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("PARENT_ID", parentId);
                        resultIntent.putExtra("COMMENT", submittedComment);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        for (String error : response.body().getError()) {
                            showToast(error);
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
