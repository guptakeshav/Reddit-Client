package com.keshavg.reddit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by keshav.g on 23/08/16.
 */
public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

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

            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    String url = objects.get(pos).getUrl();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                }
            });

            commentsCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("Fetching..", "Comments");
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = objects.get(position);

        if (post.getThumbnail().startsWith("http")) {
            Picasso.with(context).load(post.getThumbnail())
                    .resize(1536, 384)
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
