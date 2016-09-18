package com.keshavg.reddit;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        LinearLayout postContent;
        TextView title;
        TextView author;
        TextView created;
        Button scoreUp;
        TextView scoreCount;
        Button scoreDown;
        Button commentsCount;

        public ViewHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar_image);
            image = (ImageView) itemView.findViewById(R.id.post_image);
            subreddit = (TextView) itemView.findViewById(R.id.post_subreddit);
            postContent = (LinearLayout) itemView.findViewById(R.id.post_content);
            title = (TextView) postContent.findViewById(R.id.post_title);
            author = (TextView) postContent.findViewById(R.id.post_author);
            created = (TextView) postContent.findViewById(R.id.post_created);
            scoreUp = (Button) itemView.findViewById(R.id.post_score_up);
            scoreCount = (TextView) itemView.findViewById(R.id.post_score_count);
            scoreDown = (Button) itemView.findViewById(R.id.post_score_down);
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

    public void clear() {
        objects.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
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
                        public boolean onException(Exception e,
                                                   String model,
                                                   Target<Bitmap> target,
                                                   boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource,
                                                       String model,
                                                       Target<Bitmap> target,
                                                       boolean isFromMemoryCache,
                                                       boolean isFirstResource) {
                            holder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.image);
        } else {
            holder.image.setImageBitmap(null);
        }

        holder.subreddit.setText(post.getFormattedSubreddit());
        holder.title.setText(post.getTitle());
        holder.author.setText(post.getPostedBy());
        holder.scoreCount.setText(post.getScore());
        holder.created.setText(post.getRelativeCreatedTimeSpan());
        holder.commentsCount.setText(post.getCommentsCount());

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

        holder.scoreUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVote(position, 1, holder);
            }
        });

        holder.scoreDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVote(position, -1, holder);
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

    private void onClickVote(final int position, final int vote, final ViewHolder holder) {
        SharedPreferences pref = activity.getSharedPreferences("AuthPref", Context.MODE_PRIVATE);
        if (!pref.contains("ACCESS_TOKEN")) {
            showToast(activity.getString(R.string.login_error));
            return;
        }

        ApiInterface apiService = ApiClient.getOauthClient().create(ApiInterface.class);
        Call<Void> call = apiService.votePost(
                "bearer " + pref.getString("ACCESS_TOKEN", ""),
                objects.get(position).getName(),
                vote
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    objects.get(position).updateScore(vote);
                    holder.scoreCount.setText(objects.get(position).getScore());
                } else {
                    showToast(response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.server_error));
            }
        });
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

    private void showToast(String message) {
        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}