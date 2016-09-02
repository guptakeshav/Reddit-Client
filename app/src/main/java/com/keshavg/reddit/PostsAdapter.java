package com.keshavg.reddit;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

/**
 * Created by keshav.g on 23/08/16.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private static Context context;
    private static List<Post> objects;

    public PostsAdapter(Context context, List<Post> objects) {
        this.context = context;
        this.objects = objects;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView details;
        TextView score;
        TextView commentsCount;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.post_image);
            title = (TextView) itemView.findViewById(R.id.post_title);
            details = (TextView) itemView.findViewById(R.id.post_details);
            score = (TextView) itemView.findViewById(R.id.post_score);
            commentsCount = (TextView) itemView.findViewById(R.id.post_comments_count);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder imagePopup = new AlertDialog.Builder(context);
                    View view = LayoutInflater.from(context).inflate(R.layout.image_dialog, null);
                    Glide.with(context)
                            .load(objects.get(getAdapterPosition()).getThumbnail())
                            .into((ImageView) view.findViewById(R.id.image_popup));
                    imagePopup.setView(view);

                    imagePopup.create().show();
                }
            });

            title.setOnClickListener(new View.OnClickListener() {
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
                    view.getContext().startActivity(i);
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
            Glide.with(context)
                    .load(post.getThumbnail())
                    .asBitmap()
                    .override(1536, 384)
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

    public void addItem(Post dataObj) {
        objects.add(dataObj);
        notifyItemInserted(objects.size());
    }

    public void addAll(List<Post> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }
}