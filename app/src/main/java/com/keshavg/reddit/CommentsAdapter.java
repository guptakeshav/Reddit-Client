package com.keshavg.reddit;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        TextView author;
        TextView comment;
        TextView created;
        TextView upvotes;

        RecyclerView subcommentsView;
        CommentsAdapter subcommentsAdapter;
        LinearLayoutManager llm;

        ProgressBar progressBar;
        Button button;

        public ViewHolder(View v, Context context, String url, String sortByParam) {
            super(v);

            this.author = (TextView) v.findViewById(R.id.comment_author);
            this.comment = (TextView) v.findViewById(R.id.comment_body);
            this.created = (TextView) v.findViewById(R.id.comment_created);
            this.upvotes = (TextView) v.findViewById(R.id.comment_upvotes);

            this.subcommentsView = (RecyclerView) v.findViewById(R.id.subcomments_list);
            this.subcommentsAdapter = new CommentsAdapter(context, url, sortByParam);
            this.subcommentsView.setAdapter(this.subcommentsAdapter);
            this.llm = new LinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL,
                    false);
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
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final Comment comment = objects.get(position);

        viewHolder.author.setText(comment.getAuthor());
        viewHolder.comment.setText(comment.getBody());
        viewHolder.created.setText(comment.getCreated());
        viewHolder.upvotes.setText(comment.getUps());

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                viewHolder.subcommentsAdapter.addAll(comment.getReplies());
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

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
        Call<CommentResponse> callMore =
                apiService.getMoreComments(url, moreIds.peek(), sortByParam);

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

    @Override
    public int getItemCount() {
        return objects.size();
    }

    /**
     * Generate a random background color for the comment start line
     * @param convertView
     */
    private void makeRandomColorLine(View convertView) {
        Random rnd = new Random();
        int color = Color.argb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
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