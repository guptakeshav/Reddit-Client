package com.keshavg.reddit;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.R.color.black;
import static android.R.color.white;

/**
 * Created by keshav.g on 29/08/16.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private Context context;
    private String url;
    private String sortByParam;
    private List<Comment> objects;
    private Boolean isCollapsed;

    public CommentsAdapter(Context context, String url, String sortByParam) {
        this.context = context;
        this.url = url;
        this.sortByParam = sortByParam;
        this.objects = new ArrayList<>();
        this.isCollapsed = false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout comment;

        RelativeLayout header;
        ImageView collapse;
        TextView author;
        TextView created;
        TextView upvotes;

        TextView commentBody;
        LinearLayout commentMenu;
        Button commentUpvote;
        Button commentDownvote;
        Button commentReply;

        RecyclerView subcommentsView;
        CommentsAdapter subcommentsAdapter;
        LinearLayoutManager llm;

        FrameLayout loadMore;
        ProgressBar progressBar;
        Button button;

        public ViewHolder(View v, Context context, String url, String sortByParam) {
            super(v);

            this.comment = (LinearLayout) v.findViewById(R.id.comment);

            this.header = (RelativeLayout) this.comment.findViewById(R.id.comment_header);
            this.collapse = (ImageView) this.header.findViewById(R.id.comment_collapse);
            this.author = (TextView) this.header.findViewById(R.id.comment_author);
            this.created = (TextView) this.header.findViewById(R.id.comment_created);
            this.upvotes = (TextView) this.header.findViewById(R.id.comment_score);

            this.commentBody = (TextView) this.comment.findViewById(R.id.comment_body);
            this.commentMenu = (LinearLayout) this.comment.findViewById(R.id.comment_menu);
            this.commentUpvote = (Button) this.commentMenu.findViewById(R.id.comment_upvote);
            this.commentDownvote = (Button) this.commentMenu.findViewById(R.id.comment_downvote);
            this.commentReply = (Button) this.commentMenu.findViewById(R.id.comment_reply);

            this.subcommentsView = (RecyclerView) v.findViewById(R.id.subcomments_list);
            this.subcommentsAdapter = new CommentsAdapter(context, url, sortByParam);
            this.subcommentsView.setAdapter(this.subcommentsAdapter);
            this.llm = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            this.subcommentsView.setLayoutManager(llm);

            this.loadMore = (FrameLayout) v.findViewById(R.id.load_more);
            this.progressBar = (ProgressBar) this.loadMore.findViewById(R.id.progressbar_loadmore);
            this.button = (Button) this.loadMore.findViewById(R.id.button_loadmore);
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.comment_row, parent, false);

        makeRandomColorLine(view);
        return new ViewHolder(view, context, url, sortByParam);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Comment comment = objects.get(position);
        viewHolder.author.setText(comment.getAuthor());
        viewHolder.commentBody.setText(Html.fromHtml(comment.getBody()));
        viewHolder.created.setText(comment.getCreated());
        viewHolder.commentMenu.setVisibility(View.GONE);
        setScoreInformation(viewHolder, comment);

        if (comment.getReplies() != null && viewHolder.subcommentsAdapter.getItemCount() == 0) {
            // do not add the replies again and again on binding of view
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    viewHolder.subcommentsAdapter.addAll(comment.getReplies());
                }
            });
        }

        viewHolder.header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCommentCollapse(viewHolder);
            }
        });

        viewHolder.commentBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickComment(viewHolder);
            }
        });

        viewHolder.commentUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVote(position, 1, viewHolder);
            }
        });

        viewHolder.commentDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVote(position, -1, viewHolder);
            }
        });

        viewHolder.commentReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickReply(comment.getName());
            }
        });

        final Queue<String> moreIds = comment.getMoreReplyIds();
        if (moreIds != null && !moreIds.isEmpty()) {
            viewHolder.button.setVisibility(View.VISIBLE);
            viewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLoadMore(viewHolder, moreIds);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }


    /**
     * Function to clear the contents of the adapter
     * And update the view
     */
    public void clear() {
        this.objects.clear();
        notifyDataSetChanged();
    }

    /**
     * Function to add object to the adapter
     * And update the view
     * @param comment
     */
    public void add(Comment comment) {
        this.objects.add(comment);
        notifyDataSetChanged();
    }

    /**
     * Function to add objects to the adapter
     * And update the view
     * @param objects
     */
    public void addAll(List<Comment> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void setScoreInformation(ViewHolder viewHolder, Comment comment) {
        viewHolder.upvotes.setText(comment.getScore());
        if (comment.getLikes() == 1) {
            viewHolder.commentUpvote.setBackgroundColor(context.getColor(R.color.colorAccent));
            viewHolder.commentUpvote.setTextColor(context.getColor(white));
            viewHolder.commentDownvote.setBackgroundColor(context.getColor(white));
            viewHolder.commentDownvote.setTextColor(context.getColor(black));
        } else if (comment.getLikes() == -1) {
            viewHolder.commentUpvote.setBackgroundColor(context.getColor(white));
            viewHolder.commentUpvote.setTextColor(context.getColor(black));
            viewHolder.commentDownvote.setBackgroundColor(context.getColor(R.color.colorAccent));
            viewHolder.commentDownvote.setTextColor(context.getColor(white));
        } else {
            viewHolder.commentUpvote.setBackgroundColor(context.getColor(white));
            viewHolder.commentUpvote.setTextColor(context.getColor(black));
            viewHolder.commentDownvote.setBackgroundColor(context.getColor(white));
            viewHolder.commentDownvote.setTextColor(context.getColor(black));
        }
    }

    /**
     *
     * @param viewHolder
     */
    private void onClickCommentCollapse(ViewHolder viewHolder) {
        if (isCollapsed == false) {
            viewHolder.loadMore.setVisibility(View.GONE);
            viewHolder.subcommentsView.setVisibility(View.GONE);
            viewHolder.commentMenu.setVisibility(View.GONE);
            viewHolder.commentBody.setVisibility(View.GONE);

            viewHolder.collapse.setImageDrawable(context.getDrawable(R.drawable.ic_keyboard_arrow_right));
        } else {
            viewHolder.commentBody.setVisibility(View.VISIBLE);
            viewHolder.subcommentsView.setVisibility(View.VISIBLE);
            viewHolder.loadMore.setVisibility(View.VISIBLE);

            viewHolder.collapse.setImageDrawable(context.getDrawable(R.drawable.ic_keyboard_arrow_down));
        }

        isCollapsed = !isCollapsed;
    }

    /**
     * On click listener on the comment
     * Opens up a menu to perform various actions
     * @param viewHolder
     */
    private void onClickComment(ViewHolder viewHolder) {
        if (viewHolder.commentMenu.getVisibility() == View.VISIBLE) {
            viewHolder.commentMenu.setVisibility(View.GONE);
        } else {
            viewHolder.commentMenu.setVisibility(View.VISIBLE);
        }
    }

    /**
     * On click listener for up voting or down voting
     * @param position
     * @param likes
     * @param viewHolder
     */
    private void onClickVote(int position, int likes, final ViewHolder viewHolder) {
        if (!MainActivity.AuthPrefManager.isLoggedIn()) {
            showToast(context.getString(R.string.login_error));
            return;
        }

        ApiInterface apiService = ApiClient.getOAuthClient().create(ApiInterface.class);

        final Comment comment = objects.get(position);
        final int prevLikes = comment.getLikes();
        comment.setLikes((comment.getLikes() ^ likes) == 0 ? 0 : likes);
        final int delta = comment.getLikes() - prevLikes;
        comment.updateScore(delta);
        setScoreInformation(viewHolder, comment);

        Call<Void> call = apiService.votePost(
                "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                comment.getName(),
                comment.getLikes()
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    showToast("Comments Voting - " + response.message());

                    comment.setLikes(prevLikes);
                    comment.updateScore(-delta);
                    setScoreInformation(viewHolder, comment);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(context.getString(R.string.server_error));
            }
        });
    }

    private void onClickReply(final String parentId) {
        if (!MainActivity.AuthPrefManager.isLoggedIn()) {
            showToast(context.getString(R.string.login_error));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Submit Comment")
                .setView(R.layout.edit_text)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText) ((AlertDialog) dialog).findViewById(R.id.text);
                        submitComment(parentId, editText.getText().toString());
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void submitComment(String parentId, String text) {
        ApiInterface apiClient = ApiClient.getOAuthClient().create(ApiInterface.class);
        Call<Void> call = apiClient.submitComment(
                "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                "json",
                text,
                parentId
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // TODO: add the comment to the adapter
                } else {
                    showToast("Submitting - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(context.getString(R.string.server_error));
            }
        });
    }

    /**
     * On click listener for the load more button
     * Fetches more comments from the REST api and adds to the adapter
     * @param viewHolder
     * @param moreIds
     */
    private void onClickLoadMore(final ViewHolder viewHolder, final Queue<String> moreIds) {
        viewHolder.button.setVisibility(View.GONE);
        viewHolder.progressBar.setVisibility(View.VISIBLE);

        ApiInterface apiService;
        Call<List<CommentResponse>> callMore;

        if (MainActivity.AuthPrefManager.isLoggedIn()) {
            apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
            callMore = apiService.getMoreOAuthComments(
                    "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                    url,
                    moreIds.peek(),
                    sortByParam,
                    1
            );
        } else {
            apiService = ApiClient.getClient().create(ApiInterface.class);
            callMore = apiService.getMoreComments(url, moreIds.peek(), sortByParam, 1);
        }

        callMore.enqueue(new Callback<List<CommentResponse>>() {
            @Override
            public void onResponse(Call<List<CommentResponse>> call, Response<List<CommentResponse>> response) {
                if (response.isSuccessful()) {
                    viewHolder.subcommentsAdapter.addAll(response.body().get(1).getComments());

                    viewHolder.progressBar.setVisibility(View.GONE);
                    moreIds.remove();
                    if (moreIds != null && !moreIds.isEmpty()) {
                        viewHolder.button.setVisibility(View.VISIBLE);
                    }
                } else {
                    showToast("Load More - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<CommentResponse>> call, Throwable t) {
                showToast(context.getString(R.string.server_error));
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.button.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Generate a random background color for the comment start line
     * @param convertView
     */
    private void makeRandomColorLine(View convertView) {
        Random rnd = new Random();
        int color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        convertView.findViewById(R.id.comment_start_line).setBackgroundColor(color);
    }
}