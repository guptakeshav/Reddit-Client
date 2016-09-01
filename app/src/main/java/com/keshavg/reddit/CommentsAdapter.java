package com.keshavg.reddit;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

/**
 * Created by keshav.g on 29/08/16.
 */
public class CommentsAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private List<Comment> objects;

    public CommentsAdapter(Context context, List<Comment> objects) {
        super(context, 0, objects);
        this.context = context;
        this.objects = objects;
    }

    private static class ViewHolder {
        TextView author;
        TextView comment;
        TextView created;
        TextView upvotes;
        ListView subcomments;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Comment comment = getItem(position);

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_row, parent, false);

            makeRandomColorLine(convertView);

            viewHolder.author = (TextView) convertView.findViewById(R.id.comment_author);
            viewHolder.comment = (TextView) convertView.findViewById(R.id.comment_body);
            viewHolder.created = (TextView) convertView.findViewById(R.id.comment_created);
            viewHolder.upvotes = (TextView) convertView.findViewById(R.id.comment_upvotes);
            viewHolder.subcomments = (ListView) convertView.findViewById(R.id.subcomments_list);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.author.setText(comment.getAuthor());
        viewHolder.comment.setText(comment.getBody());
        viewHolder.created.setText(comment.getCreated());
        viewHolder.upvotes.setText(comment.getUps());

        if (comment.getReplies().size() > 0) {
            viewHolder.subcomments.setAdapter(new CommentsAdapter(context, comment.getReplies()));
        }

        return convertView;
    }

    private void makeRandomColorLine(View convertView) {
        Random rnd = new Random();
        int color = Color.argb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        convertView.findViewById(R.id.comment_start_line).setBackgroundColor(color);
    }
}
