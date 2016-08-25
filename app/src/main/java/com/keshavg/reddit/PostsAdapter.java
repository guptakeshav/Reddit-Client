package com.keshavg.reddit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        TextView title;
        TextView details;
        TextView score;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.post_title);
            holder.details = (TextView) convertView.findViewById(R.id.post_details);
            holder.score = (TextView) convertView.findViewById(R.id.post_score);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Post post = objects.get(position);
        holder.title.setText(post.getTitle());
        holder.details.setText(post.getAuthor());
        holder.score.setText(""+post.getScore());

        return convertView;
    }
}
