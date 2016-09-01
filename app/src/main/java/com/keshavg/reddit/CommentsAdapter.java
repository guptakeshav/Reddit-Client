package com.keshavg.reddit;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

/**
 * Created by keshav.g on 29/08/16.
 */
public class CommentsAdapter extends ArrayAdapter<Comment> {

    private Context context;
    private String url;
    private List<Comment> objects;

    public CommentsAdapter(Context context, String url, List<Comment> objects) {
        super(context, 0, objects);
        this.context = context;
        this.url = url;
        this.objects = objects;
    }

    private class ViewHolder {
        TextView author;
        TextView comment;
        TextView created;
        TextView upvotes;
        LinearLayout subcomments;
        Button loadMore;
        ProgressBar progressBar;

        public ViewHolder(View v) {
            this.author = (TextView) v.findViewById(R.id.comment_author);
            this.comment = (TextView) v.findViewById(R.id.comment_body);
            this.created = (TextView) v.findViewById(R.id.comment_created);
            this.upvotes = (TextView) v.findViewById(R.id.comment_upvotes);
            this.subcomments = (LinearLayout) v.findViewById(R.id.subcomments_list);
            this.loadMore = (Button) v.findViewById(R.id.load_more);
            this.progressBar = (ProgressBar) v.findViewById(R.id.progressbar_replies);
        }
    }

    private class FetchComments extends AsyncTask<String, Void, List<Comment>> {

        private LinearLayout subcomments;
        private Button loadMore;
        private ProgressBar progressBar;
        private List<String> moreRepliesId;

        public FetchComments(ViewHolder viewHolder, List<String> moreRepliesId) {
            this.subcomments = viewHolder.subcomments;
            this.loadMore = viewHolder.loadMore;
            this.progressBar = viewHolder.progressBar;
            this.moreRepliesId = moreRepliesId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadMore.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Comment> doInBackground(String... params) {
            Log.d("Load More", params[0]);
            return new NetworkTasks().fetchCommentsList(params[0]);
        }

        @Override
        protected void onPostExecute(List<Comment> comments) {
            super.onPostExecute(comments);

            progressBar.setVisibility(View.GONE);
            createThreadedComments(subcomments, comments);

            moreRepliesId.remove(0);
            if (moreRepliesId.size() > 0) {
                loadMore.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        final Comment comment = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_row, parent, false);
            viewHolder = new ViewHolder(convertView);
            makeRandomColorLine(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewData(comment, viewHolder);

        return convertView;
    }

    /**
     * Recursive code to create threaded comments
     * @param subcomments
     * @param replies
     */
    private void createThreadedComments(LinearLayout subcomments, final List<Comment> replies) {
        if (replies.size() > 0) {
            for (final Comment reply : replies) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.comment_row, subcomments, false);
                makeRandomColorLine(view);
                setViewData(reply, new ViewHolder(view));
                subcomments.addView(view);
            }
        }
    }

    /**
     * Function to set the data for each of the view elements
     * @param comment
     * @param viewHolder
     */
    private void setViewData(Comment comment, final ViewHolder viewHolder) {
        viewHolder.author.setText(comment.getAuthor());
        viewHolder.comment.setText(comment.getBody());
        viewHolder.created.setText(comment.getCreated());
        viewHolder.upvotes.setText(comment.getUps());

        createThreadedComments(viewHolder.subcomments, comment.getReplies());

        final List<String> moreRepliesId = comment.getMoreReplies();
        if (moreRepliesId.size() > 0) {
            viewHolder.loadMore.setVisibility(View.VISIBLE);
            viewHolder.loadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FetchComments(viewHolder,
                            moreRepliesId)
                            .execute(url + "/" + moreRepliesId.get(0));
                }
            });
        }
    }

    /**
     * Generate a random background color for the comment start line
     * @param convertView
     */
    private void makeRandomColorLine(View convertView) {
        Random rnd = new Random();
        int color = Color.argb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        convertView.findViewById(R.id.comment_start_line).setBackgroundColor(color);
    }
}
