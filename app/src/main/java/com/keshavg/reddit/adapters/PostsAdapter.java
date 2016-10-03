package com.keshavg.reddit.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.keshavg.reddit.R;
import com.keshavg.reddit.activities.CommentsActivity;
import com.keshavg.reddit.activities.ImageViewActivity;
import com.keshavg.reddit.activities.SearchActivity;
import com.keshavg.reddit.activities.WebViewActivity;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.models.Post;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;
import com.keshavg.reddit.utils.DeviceUtil;
import com.keshavg.reddit.utils.ValidateUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshav.g on 23/08/16.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder>
        implements SectionTitleProvider {
    private CoordinatorLayout coordinatorLayout;
    private Activity activity;
    private List<Post> objects;
    private RequestManager requestManager;
    private Boolean clearOnHide;
    private Boolean clearOnUnHide;

    public PostsAdapter(Activity activity,
                        RequestManager requestManager,
                        Boolean clearOnHide,
                        Boolean clearOnUnHide) {
        this.activity = activity;
        this.requestManager = requestManager;
        this.clearOnHide = clearOnHide;
        this.clearOnUnHide = clearOnUnHide;

        this.objects = new ArrayList<>();
        this.coordinatorLayout = (CoordinatorLayout) activity.findViewById(R.id.coordinator_layout);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        Toolbar toolbar;
        MenuItem goToAuthor;
        MenuItem goToSubreddit;
        MenuItem share;
        MenuItem save;
        MenuItem hide;
        MenuItem delete;
        MenuItem markNsfw;

        ImageView image;
        TextView subreddit;
        RelativeLayout postContent;
        TextView title;
        TextView author;
        TextView created;
        TextView nsfw;
        ImageButton scoreUp;
        TextView scoreCount;
        ImageButton scoreDown;
        Button commentsCount;

        public ViewHolder(View itemView) {
            super(itemView);

            progressBar = (ProgressBar) itemView.findViewById(R.id.progressbar_image);

            toolbar = (Toolbar) itemView.findViewById(R.id.toolbar);
            toolbar.inflateMenu(R.menu.post_functions);

            goToAuthor = toolbar.getMenu().findItem(R.id.go_to_author);
            goToSubreddit = toolbar.getMenu().findItem(R.id.go_to_subreddit);
            share = toolbar.getMenu().findItem(R.id.share);
            save = toolbar.getMenu().findItem(R.id.save);
            hide = toolbar.getMenu().findItem(R.id.hide);
            delete = toolbar.getMenu().findItem(R.id.delete);
            markNsfw = toolbar.getMenu().findItem(R.id.nsfw);

            image = (ImageView) itemView.findViewById(R.id.post_image);
            subreddit = (TextView) itemView.findViewById(R.id.post_subreddit);
            postContent = (RelativeLayout) itemView.findViewById(R.id.post_content);
            title = (TextView) postContent.findViewById(R.id.post_title);
            author = (TextView) toolbar.findViewById(R.id.post_author);
            created = (TextView) toolbar.findViewById(R.id.post_created);
            nsfw = (TextView) toolbar.findViewById(R.id.post_nsfw);
            scoreUp = (ImageButton) itemView.findViewById(R.id.post_score_up);
            scoreCount = (TextView) itemView.findViewById(R.id.post_score_count);
            scoreDown = (ImageButton) itemView.findViewById(R.id.post_score_down);
            commentsCount = (Button) itemView.findViewById(R.id.post_comments_count);
        }
    }

    @Override
    public String getSectionTitle(int position) {
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

    public void addAll(List<Post> objects, Boolean isHiddenPostsShown) {
       for (Post post : objects) {
            if (isHiddenPostsShown || post.getIsHidden() == -1) {
                this.objects.add(post);
            }
        }

        notifyDataSetChanged();
    }

    public void remove(int idx) {
        this.objects.remove(idx);
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Post post = objects.get(position);

        setToolbar(holder);

        setPostImage(holder);

        holder.subreddit.setText(post.getFormattedSubreddit());
        holder.title.setText(post.getTitle());
        holder.author.setText(post.getPostedBy());

        setScoreInformation(holder, post.getScore(), post.getIsLiked());

        holder.created.setText(post.getRelativeCreatedTimeSpan());
        holder.commentsCount.setText(post.getCommentsCount());

        holder.image.setOnClickListener(view -> onClickImage(holder.getAdapterPosition()));

        holder.postContent.setOnClickListener(view -> onClickContent(post));

        holder.scoreUp.setOnClickListener(v -> onClickVote(holder.getAdapterPosition(), 1, holder));

        holder.scoreDown.setOnClickListener(v -> onClickVote(holder.getAdapterPosition(), -1, holder));

        holder.commentsCount.setOnClickListener(view -> onClickCommentsCount(post, holder));
    }

    private void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    private void setToolbar(final ViewHolder holder) {
        final Post post = objects.get(holder.getAdapterPosition());

        holder.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.go_to_author) {
                Intent i = new Intent(activity, SearchActivity.class);
                i.putExtra(SearchActivity.TYPE, SearchActivity.USERS);
                i.putExtra(SearchActivity.SEARCH_QUERY, post.getAuthor());
                activity.startActivity(i);
            } else if (item.getItemId() == R.id.go_to_subreddit) {
                Intent i = new Intent(activity, SearchActivity.class);
                i.putExtra(SearchActivity.TYPE, SearchActivity.SUBREDDIT_POSTS);
                i.putExtra(SearchActivity.SEARCH_QUERY, post.getFormattedSubreddit());
                activity.startActivity(i);
            } else if (item.getItemId() == R.id.save) {
                onClickSave(post, holder.save);
                return true;
            } else if (item.getItemId() == R.id.hide) {
                onClickHide(post, holder.hide, holder.getAdapterPosition());
                return true;
            } else if (item.getItemId() == R.id.delete) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity)
                        .setTitle(item.getTitle())
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            onClickDelete(post.getName(), holder.getAdapterPosition());
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                        .create();

                alertDialog.show();
                return true;
            } else if (item.getItemId() == R.id.nsfw) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity)
                        .setTitle(item.getTitle())
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            onClickNsfw(holder, post, holder.markNsfw, holder.getAdapterPosition());
                            dialog.dismiss();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                        .create();

                alertDialog.show();
                return true;
            } else if (item.getItemId() == R.id.share) {
                startShareIntent(post);
            }

            return false;
        });

        if (AuthSharedPrefHelper.isLoggedIn()) {
            holder.save.setVisible(true);
            holder.hide.setVisible(true);

            if (AuthSharedPrefHelper.getUsername().equals(post.getAuthor())) {
                holder.delete.setVisible(true);
                holder.markNsfw.setVisible(true);
            } else {
                holder.markNsfw.setVisible(false);
                holder.delete.setVisible(false);
            }
        } else {
            holder.hide.setVisible(false);
            holder.save.setVisible(false);
        }

        if (post.getIsSaved() == 1) {
            holder.save.setTitle("Unsave");
        } else {
            holder.save.setTitle("Save");
        }

        if (post.getIsHidden() == 1) {
            holder.hide.setTitle("Unhide");
        } else {
            holder.hide.setTitle("Hide");
        }

        if (post.getIsNsfw() == 1) {
            holder.markNsfw.setTitle("Unmark NSFW");
            holder.nsfw.setVisibility(View.VISIBLE);
        } else {
            holder.markNsfw.setTitle("Mark NSFW");
            holder.nsfw.setVisibility(View.GONE);
        }
    }

    private void startShareIntent(Post post) {
        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                post.getTitle() +
                        "\n\n" +
                        RedditApiClient.BASE_URL + post.getPermalink());
        sendIntent.setType("text/plain");

        Intent chooser = Intent.createChooser(sendIntent, "Share with...");
        activity.startActivity(chooser);
    }

    private void setPostImage(final ViewHolder holder) {
        Post post = objects.get(holder.getAdapterPosition());

        int width = activity.getWindowManager().getDefaultDisplay().getWidth() - DeviceUtil.dpToPx(20);
        int height = 384;

        if (post.getImage().startsWith("http")) {
            holder.progressBar.setVisibility(View.VISIBLE);
            requestManager
                    .load(post.getImage())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(width, height)
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
    }

    private void setScoreInformation(ViewHolder holder, long score, int likes) {
        holder.scoreCount.setText(Long.toString(score));

        if (likes == 1) {
            holder.scoreUp.setColorFilter(activity.getColor(R.color.colorAccent));
            holder.scoreDown.setColorFilter(activity.getColor(android.R.color.black));
        } else if (likes == -1) {
            holder.scoreUp.setColorFilter(activity.getColor(android.R.color.black));
            holder.scoreDown.setColorFilter(activity.getColor(R.color.colorAccent));
        } else {
            holder.scoreUp.setColorFilter(activity.getColor(android.R.color.black));
            holder.scoreDown.setColorFilter(activity.getColor(android.R.color.black));
        }
    }

    private void onClickSave(final Post post, final MenuItem save) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);

        Call<Void> call;
        if (post.getIsSaved() == 1) {
            call = apiService.unsaveThing(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName()
            );
        } else {
            call = apiService.saveThing(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName()
            );
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (post.getIsSaved() == 1) {
                        showToast("Unsaved");
                        save.setTitle("Save");
                        post.setIsSaved(-1);
                    } else {
                        showToast("Saved");
                        save.setTitle("Unsave");
                        post.setIsSaved(1);
                    }
                } else {
                    showToast("Save - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.error_server_connect));
            }
        });
    }

    private void onClickHide(final Post post, final MenuItem hide, final int position) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);

        Call<Void> call;
        if (post.getIsHidden() == 1) {
            call = apiService.unhideThing(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName()
            );
        } else {
            call = apiService.hideThing(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName()
            );
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (post.getIsHidden() == 1) {
                        hide.setTitle("Hide");
                        post.setIsHidden(-1);

                        if (clearOnUnHide) {
                            remove(position);
                        }
                    } else {
                        hide.setTitle("Unhide");
                        post.setIsHidden(1);

                        if (clearOnHide) {
                            remove(position);
                        }
                    }
                } else {
                    showToast("Save - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.error_server_connect));
            }
        });
    }

    private void onClickDelete(String id, final int position) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
        Call<Void> call = apiService.deleteThing(
                "bearer " + AuthSharedPrefHelper.getAccessToken(),
                id
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    remove(position);
                } else {
                    showToast("Delete - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.error_server_connect));
            }
        });
    }

    private void onClickNsfw(final ViewHolder viewHolder, final Post post, final MenuItem nsfw, final int position) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);

        Call<Void> call;
        if (post.getIsNsfw() == 1) {
            call = apiService.unmarkNsfw(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName()
            );
        } else {
            call = apiService.markNsfw(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName()
            );
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (post.getIsNsfw() == 1) {
                        nsfw.setTitle("Mark NSFW");
                        post.setIsNsfw(-1);
                        viewHolder.nsfw.setVisibility(View.GONE);
                    } else {
                        nsfw.setTitle("Unmark NSFW");
                        post.setIsNsfw(1);
                        viewHolder.nsfw.setVisibility(View.VISIBLE);
                    }
                } else {
                    showToast("NSFW - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.error_server_connect));
            }
        });
    }

    private void onClickImage(int position) {
        Intent i = new Intent(activity, ImageViewActivity.class);
        i.putExtra(ImageViewActivity.IMAGE_URL, objects.get(position).getImage());
        activity.startActivity(i);
    }

    private void onClickContent(Post post) {
        String url = post.getUrl();
        Intent i = new Intent(activity, WebViewActivity.class);
        i.putExtra("URL", url);
        activity.startActivity(i);
    }

    private void onClickVote(int position, final int likes, final ViewHolder holder) {
        if (ValidateUtil.loginValidation(coordinatorLayout, activity)) {

            RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);

            final Post post = objects.get(position);
            final int prevLikes = post.getIsLiked();
            post.setIsLiked((post.getIsLiked() == likes) ? 0 : likes);
            final int delta = post.getIsLiked() - prevLikes;
            post.updateScore(delta);
            setScoreInformation(holder, post.getScore(), post.getIsLiked());

            Call<Void> call = apiService.votePost("bearer " + AuthSharedPrefHelper.getAccessToken(),
                    post.getName(),
                    post.getIsLiked()
            );
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        if (response.code() == 400) {
                            showToast(activity.getString(R.string.error_archive_post));
                        } else {
                            showToast("Voting - " + response.message());
                        }

                        post.setIsLiked(prevLikes);
                        post.updateScore(-delta);
                        setScoreInformation(holder, post.getScore(), post.getIsLiked());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showToast(activity.getString(R.string.error_server_connect));
                }
            });
        }
    }

    private void onClickCommentsCount(Post post, ViewHolder holder) {
        Intent i = new Intent(activity, CommentsActivity.class);
        i.putExtra(CommentsActivity.TITLE, post.getTitle());
        i.putExtra(CommentsActivity.URL, post.getPermalink());
        i.putExtra("IMAGE", post.getImage());
        activity.startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(activity,
                        holder.title,
                        "comment_transition"
                ).toBundle()
        );
    }
}