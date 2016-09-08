package com.keshavg.reddit;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

/**
 * Created by keshav.g on 23/08/16.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private static Activity activity;
    private static Context context;
    private static List<Post> objects;

    public PostsAdapter(Activity activity, Context context, List<Post> objects) {
        this.activity = activity;
        this.context = context;
        this.objects = objects;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView details;
        TextView score;
        Button commentsCount;

        public ViewHolder(View itemView) {
            super(itemView);

            image = (ImageView) itemView.findViewById(R.id.post_image);
            title = (TextView) itemView.findViewById(R.id.post_title);
            details = (TextView) itemView.findViewById(R.id.post_details);
            score = (TextView) itemView.findViewById(R.id.post_score);
            commentsCount = (Button) itemView.findViewById(R.id.post_comments_count);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder imagePopup = new AlertDialog.Builder(v.getContext());
                    View view = LayoutInflater.from(v.getContext()).inflate(R.layout.image_dialog, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.image_popup);
                    GlideDrawableImageViewTarget glideDrawableImageViewTarget = new GlideDrawableImageViewTarget(imageView);

                    // TODO: fix issue with size of image as compared to the size of dialog box
                    Glide.with(v.getContext())
                            .load(objects.get(getAdapterPosition()).getThumbnail())
                            .into(glideDrawableImageViewTarget);
                    imagePopup.setView(view);

                    imagePopup.create().show();
                }
            });

            (itemView.findViewById(R.id.post_content)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    String url = objects.get(pos).getUrl();
                    Intent i = new Intent(view.getContext(), WebViewActivity.class);
                    i.putExtra("Url", url);
                    view.getContext().startActivity(i);
                }
            });

            commentsCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(view.getContext(), CommentsActivity.class);
                    i.putExtra("Title", objects.get(getAdapterPosition()).getTitle());
                    i.putExtra("Url", objects.get(getAdapterPosition()).getPermalink());
                    i.putExtra("Image", objects.get(getAdapterPosition()).getThumbnail());
                    view.getContext().startActivity(i,
                            ActivityOptions.makeSceneTransitionAnimation(activity,
                                    title,
                                    "comment_transition"
                            ).toBundle()
                    );
                }
            });
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = objects.get(position);

        if (post.getThumbnail().startsWith("http")) {
            Log.d("Pics Thumbnail", post.getThumbnail());
            Glide.with(context)
                    .load(post.getThumbnail())
                    .asBitmap()
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