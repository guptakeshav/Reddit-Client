package com.keshavg.reddit;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

    public CommentsAdapter(Context context, String url, String sortByParam) {
        this.context = context;
        this.url = url;
        this.sortByParam = sortByParam;
        this.objects = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout comment;

        TextView author;
        TextView commentBody;
        TextView created;
        TextView upvotes;

        LinearLayout commentMenu;
        Button commentUpvote;
        Button commentDownvote;
        Button commentReply;

        RecyclerView subcommentsView;
        CommentsAdapter subcommentsAdapter;
        LinearLayoutManager llm;

        ProgressBar progressBar;
        Button button;

        public ViewHolder(View v, Context context, String url, String sortByParam) {
            super(v);

            this.comment = (LinearLayout) v.findViewById(R.id.comment);

            this.author = (TextView) this.comment.findViewById(R.id.comment_author);
            this.commentBody = (TextView) this.comment.findViewById(R.id.comment_body);
            this.created = (TextView) this.comment.findViewById(R.id.comment_created);
            this.upvotes = (TextView) this.comment.findViewById(R.id.comment_score);

            this.commentMenu = (LinearLayout) this.comment.findViewById(R.id.comment_menu);
            this.commentUpvote = (Button) this.commentMenu.findViewById(R.id.comment_upvote);
            this.commentDownvote = (Button) this.commentMenu.findViewById(R.id.comment_downvote);
            this.commentReply = (Button) this.commentMenu.findViewById(R.id.comment_reply);

            this.subcommentsView = (RecyclerView) v.findViewById(R.id.subcomments_list);

            this.subcommentsAdapter = new CommentsAdapter(context, url, sortByParam);
            this.subcommentsView.setAdapter(this.subcommentsAdapter);

            this.llm = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            this.subcommentsView.setLayoutManager(llm);

            this.progressBar = (ProgressBar) v.findViewById(R.id.progressbar_loadmore);
            this.button = (Button) v.findViewById(R.id.load_more);
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

        viewHolder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickComment(viewHolder);
            }
        });

        viewHolder.author.setText(comment.getAuthor());
        viewHolder.commentBody.setText(Html.fromHtml(comment.getBody()));
        viewHolder.created.setText(comment.getCreated());

        viewHolder.commentMenu.setVisibility(View.GONE);

        setScoreInformation(viewHolder, comment);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                viewHolder.subcommentsAdapter.addAll(comment.getReplies());
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
//                onClickReply(viewHolder);
            }
        });

        final Queue<String> moreIds = comment.getMoreRepliesId();
        if (!moreIds.isEmpty()) {
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

    /**
     * On click listener for the load more button
     * Fetches more comments from the REST api and adds to the adapter
     * @param viewHolder
     * @param moreIds
     */
    private void onClickLoadMore(final ViewHolder viewHolder, final Queue<String> moreIds) {
        viewHolder.button.setVisibility(View.GONE);
        viewHolder.progressBar.setVisibility(View.VISIBLE);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<CommentResponse> callMore;
        if (MainActivity.AuthPrefManager.isLoggedIn()) {
            callMore = apiService.getMoreOAuthComments(
                    "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                    url,
                    moreIds.peek(),
                    sortByParam,
                    1
            );
        } else {
            callMore = apiService.getMoreComments(url, moreIds.peek(), sortByParam, 1);
        }

        callMore.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> response) {
                if (response.isSuccessful()) {
                    viewHolder.subcommentsAdapter.addAll(response.body().getComments());

                    viewHolder.progressBar.setVisibility(View.GONE);
                    moreIds.remove();
                    if (!moreIds.isEmpty()) {
                        viewHolder.button.setVisibility(View.VISIBLE);
                    }
                } else {
                    showToast("Load More - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
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

    /**
     * Function to clear the contents of the adapter
     * And update the view
     */
    public void clear() {
        objects.clear();
        notifyDataSetChanged();
    }

    /**
     * Function to objects to the adapter
     * And update the view
     * @param objects
     */
    public void addAll(List<Comment> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }
}