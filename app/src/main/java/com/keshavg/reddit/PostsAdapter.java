package com.keshavg.reddit;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by keshav.g on 23/08/16.
 */
public class PostsAdapter extends ArrayAdapter<Post> {

    Context context;
    int resource;
    List<Post> objects;

    public PostsAdapter(Context context, int resource, List<Post> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    class ViewHolder {
        ImageView image;
        TextView title;
        TextView details;
        TextView score;
        TextView commentsCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);

            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.post_image);
            holder.title = (TextView) convertView.findViewById(R.id.post_title);
            holder.details = (TextView) convertView.findViewById(R.id.post_details);
            holder.score = (TextView) convertView.findViewById(R.id.post_score);
            holder.commentsCount = (TextView) convertView.findViewById(R.id.post_comments_count);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Post post = objects.get(position);

        if (post.getThumbnail().startsWith("http")) {
            Picasso.with(context).load(post.getThumbnail()).into(holder.image);
        }
        holder.title.setText(post.getTitle());
        holder.details.setText(post.getDetails());
        holder.score.setText(post.getScore());
        holder.commentsCount.setText(post.getNumComments());

        return convertView;
    }
}
