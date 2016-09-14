package com.keshavg.reddit;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav.g on 23/08/16.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private Activity activity;
    private List<Post> objects;
    private RequestManager requestManager;

    public PostsAdapter(Activity activity, RequestManager requestManager) {
        this.activity = activity;
        this.objects = new ArrayList<>();
        this.requestManager = requestManager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        RelativeLayout postContent;
        TextView title;
        TextView details;
        TextView score;
        Button commentsCount;

        public ViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.post_image);
            postContent = (RelativeLayout) itemView.findViewById(R.id.post_content);
            title = (TextView) postContent.findViewById(R.id.post_title);
            details = (TextView) postContent.findViewById(R.id.post_details);
            score = (TextView) postContent.findViewById(R.id.post_score);
            commentsCount = (Button) itemView.findViewById(R.id.post_comments_count);
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(position);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.post_row, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Post post = objects.get(position);

        if (post.getThumbnail().startsWith("http")) {
            requestManager
                    .load(post.getThumbnail())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(1536, 512) // TODO: dynamically set the dimensions
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageBitmap(null);
        }

        holder.title.setText(post.getTitle());
        holder.details.setText(post.getDetails());
        holder.score.setText(post.getScore());
        holder.commentsCount.setText(post.getNumComments());

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickImage(v, position);
            }
        });

        holder.postContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickContent(view, post);
            }
        });

        holder.commentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCommentsCount(view, post, holder);
            }
        });
    }

    private void onClickImage(View v, int position) {
        Intent i = new Intent(activity, ImageViewActivity.class);
        i.putExtra("Image", objects.get(position).getThumbnail());
        activity.startActivity(i);
    }

    private void onClickContent(View view, Post post) {
        String url = post.getUrl();
        Intent i = new Intent(view.getContext(), WebViewActivity.class);
        i.putExtra("Url", url);
        view.getContext().startActivity(i);
    }

    private void onClickCommentsCount(View view, Post post, ViewHolder holder) {
        Intent i = new Intent(view.getContext(), CommentsActivity.class);
        i.putExtra("Title", post.getTitle());
        i.putExtra("Url", post.getPermalink());
        i.putExtra("Image", post.getThumbnail());
        view.getContext().startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(activity,
                        holder.title,
                        "comment_transition"
                ).toBundle()
        );
    }

    public void clear() {
        objects.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }
}