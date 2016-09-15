package com.keshavg.reddit;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
        ProgressBar progressBar;
        ImageView image;
        TextView subreddit;
        RelativeLayout postContent;
        TextView title;
        TextView author;
        TextView score;
        TextView created;
        Button commentsCount;

        public ViewHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar_image);
            image = (ImageView) itemView.findViewById(R.id.post_image);
            subreddit = (TextView) itemView.findViewById(R.id.post_subreddit);
            postContent = (RelativeLayout) itemView.findViewById(R.id.post_content);
            title = (TextView) postContent.findViewById(R.id.post_title);
            author = (TextView) postContent.findViewById(R.id.post_author);
            score = (TextView) postContent.findViewById(R.id.post_score);
            created = (TextView) postContent.findViewById(R.id.post_created);
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
            holder.progressBar.setVisibility(View.VISIBLE);

            requestManager
                    .load(post.getThumbnail())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(1536, 512) // TODO: dynamically set the dimensions
                    .centerCrop()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.image);
        } else {
            holder.image.setImageBitmap(null);
        }

        holder.subreddit.setText(post.getSubreddit());
        holder.title.setText(post.getTitle());
        holder.author.setText(post.getAuthor());
        holder.score.setText(post.getScore());
        holder.created.setText(post.getCreated());
        holder.commentsCount.setText(post.getNumComments());

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickImage(position);
            }
        });

        holder.postContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickContent(post);
            }
        });

        holder.commentsCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCommentsCount(post, holder);
            }
        });
    }

    private void onClickImage(int position) {
        Intent i = new Intent(activity, ImageViewActivity.class);
        i.putExtra("Image", objects.get(position).getThumbnail());
        activity.startActivity(i);
    }

    private void onClickContent(Post post) {
        String url = post.getUrl();
        Intent i = new Intent(activity, WebViewActivity.class);
        i.putExtra("Url", url);
        activity.startActivity(i);
    }

    private void onClickCommentsCount(Post post, ViewHolder holder) {
        Intent i = new Intent(activity, CommentsActivity.class);
        i.putExtra("Title", post.getTitle());
        i.putExtra("Url", post.getPermalink());
        i.putExtra("Image", post.getThumbnail());
        activity.startActivity(i,
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