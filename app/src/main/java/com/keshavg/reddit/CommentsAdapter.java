package com.keshavg.reddit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<String, LinearLayout> parentReplies;
    private Activity activity;
    private String url;
    private String sortByParam;
    private List<Comment> objects;
    private Boolean isCollapsed;

    public CommentsAdapter(Activity activity, String url, String sortByParam) {
        parentReplies = new HashMap<>();
        this.activity = activity;
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
        Button commentDelete;

        LinearLayout subcommentsView;

        FrameLayout loadMore;
        ProgressBar progressBar;
        Button button;

        public ViewHolder(View v) {
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
            this.commentDelete = (Button) this.commentMenu.findViewById(R.id.comment_delete);

            this.subcommentsView = (LinearLayout) v.findViewById(R.id.subcomments_list);

            this.loadMore = (FrameLayout) v.findViewById(R.id.load_more);
            this.progressBar = (ProgressBar) this.loadMore.findViewById(R.id.progressbar);
            this.button = (Button) this.loadMore.findViewById(R.id.button);
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Comment comment = objects.get(position);
        setViewData(viewHolder, comment);
    }

    private void setViewData(final ViewHolder viewHolder, final Comment comment) {
        viewHolder.author.setText(comment.getPostedBy());
        viewHolder.commentBody.setText(Html.fromHtml(comment.getBody()));
        viewHolder.created.setText(comment.getCreated());
        viewHolder.commentMenu.setVisibility(View.GONE);
        setScoreInformation(viewHolder, comment);

        if (!parentReplies.containsKey(comment.getName())) {
            // do not repeat this on notifydatasetchanged
            createThreadedComments(viewHolder.subcommentsView, comment.getReplies());
        }
        parentReplies.put(comment.getName(), viewHolder.subcommentsView);

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
                onClickVote(comment, 1, viewHolder);
            }
        });

        viewHolder.commentDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVote(comment, -1, viewHolder);
            }
        });

        viewHolder.commentReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickReply(comment.getName());
            }
        });

        if (MainActivity.AuthPrefManager.isLoggedIn()) {
            if (comment.getAuthor().equals(MainActivity.AuthPrefManager.getUsername())) {
                viewHolder.commentDelete.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.commentDelete.setVisibility(View.GONE);
        }
        viewHolder.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDelete(viewHolder, comment.getName());
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
    public void add(int index, Comment comment) {
        this.objects.add(index, comment);
        notifyDataSetChanged();
    }

    /**
     *
     * Function to add a single reply to a comment
     * @param parentId
     * @param comment
     */
    public void add(String parentId, Comment comment) {
        View view = LayoutInflater.from(activity).inflate(R.layout.comment_row, null);
        makeRandomColorLine(view);
        setViewData(new ViewHolder(view), comment);
        parentReplies.get(parentId).addView(view, 0);
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
        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setScoreInformation(ViewHolder viewHolder, Comment comment) {
        viewHolder.upvotes.setText(comment.getScore());
        if (comment.getLikes() == 1) {
            viewHolder.commentUpvote.setBackgroundColor(activity.getColor(R.color.colorAccent));
            viewHolder.commentUpvote.setTextColor(activity.getColor(white));
            viewHolder.commentDownvote.setBackgroundColor(activity.getColor(white));
            viewHolder.commentDownvote.setTextColor(activity.getColor(black));
        } else if (comment.getLikes() == -1) {
            viewHolder.commentUpvote.setBackgroundColor(activity.getColor(white));
            viewHolder.commentUpvote.setTextColor(activity.getColor(black));
            viewHolder.commentDownvote.setBackgroundColor(activity.getColor(R.color.colorAccent));
            viewHolder.commentDownvote.setTextColor(activity.getColor(white));
        } else {
            viewHolder.commentUpvote.setBackgroundColor(activity.getColor(white));
            viewHolder.commentUpvote.setTextColor(activity.getColor(black));
            viewHolder.commentDownvote.setBackgroundColor(activity.getColor(white));
            viewHolder.commentDownvote.setTextColor(activity.getColor(black));
        }
    }

    private void createThreadedComments(LinearLayout subcomments, List<Comment> replies) {
        if (replies != null && replies.size() > 0) {
            for (final Comment reply : replies) {
                View view = LayoutInflater.from(activity).inflate(R.layout.comment_row, null);
                makeRandomColorLine(view);
                setViewData(new ViewHolder(view), reply);
                subcomments.addView(view);
            }
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

            viewHolder.collapse.setImageDrawable(activity.getDrawable(R.drawable.ic_keyboard_arrow_right));
        } else {
            viewHolder.commentBody.setVisibility(View.VISIBLE);
            viewHolder.subcommentsView.setVisibility(View.VISIBLE);
            viewHolder.loadMore.setVisibility(View.VISIBLE);

            viewHolder.collapse.setImageDrawable(activity.getDrawable(R.drawable.ic_keyboard_arrow_down));
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
     * @param comment
     * @param likes
     * @param viewHolder
     */
    private void onClickVote(final Comment comment, int likes, final ViewHolder viewHolder) {
        if (!MainActivity.AuthPrefManager.isLoggedIn()) {
            showToast(activity.getString(R.string.login_error));
            return;
        }

        ApiInterface apiService = ApiClient.getOAuthClient().create(ApiInterface.class);

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
                showToast(activity.getString(R.string.server_error));
            }
        });
    }

    private void onClickReply(final String parentId) {
        if (!MainActivity.AuthPrefManager.isLoggedIn()) {
            showToast(activity.getString(R.string.login_error));
            return;
        }

        Intent i = new Intent(activity, SubmitCommentActivity.class);
        i.putExtra("PARENT_ID", parentId);
        activity.startActivityForResult(i, CommentsActivity.COMMENT_SUBMIT_REQUEST_CODE);
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
                    createThreadedComments(viewHolder.subcommentsView, response.body().get(1).getComments());

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
                showToast(activity.getString(R.string.server_error));
                viewHolder.progressBar.setVisibility(View.GONE);
                viewHolder.button.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onClickDelete(final ViewHolder viewHolder, final String id) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle("Confirm Delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteComment(viewHolder, id);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void deleteComment(final ViewHolder viewHolder, String id) {
        ApiInterface apiClient = ApiClient.getOAuthClient().create(ApiInterface.class);
        Call<Void> call = apiClient.deleteThing(
                "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                id
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    viewHolder.commentBody.setText("[deleted]");
                } else {
                    showToast("Deleting - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.server_error));
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