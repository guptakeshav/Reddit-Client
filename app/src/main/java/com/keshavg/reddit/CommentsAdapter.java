package com.keshavg.reddit;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by keshav.g on 29/08/16.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private Context context;
    private String url;
    private List<Comment> objects;

    public CommentsAdapter(Context context, String url, List<Comment> objects) {
        this.context = context;
        this.url = url;
        this.objects = objects;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView author;
        TextView comment;
        TextView created;
        TextView upvotes;
        LinearLayout subcomments;
        Button loadMore;
        ProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);

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
        private Boolean ioExceptionFlag, jsonExceptionFlag;

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

            ioExceptionFlag = false;
            jsonExceptionFlag = false;

            loadMore.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Comment> doInBackground(String... params) {
            List<Comment> comments = null;

            try {
                comments = new NetworkTasks().fetchCommentsListFromUrl(params[0]);
            } catch (IOException ioE) {
                ioE.printStackTrace();
                ioExceptionFlag = true;
            } catch (JSONException jsonE) {
                jsonE.printStackTrace();
                jsonExceptionFlag = true;
            }

            return comments;
        }

        @Override
        protected void onPostExecute(List<Comment> comments) {
            super.onPostExecute(comments);

            if (ioExceptionFlag == true) {
                Toast.makeText(context, context.getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                        .show();
            } else if(jsonExceptionFlag == true) {
                Toast.makeText(context, String.format(context.getString(R.string.json_exception), "comments"), Toast.LENGTH_SHORT)
                        .show();
            } else {
                createThreadedComments(subcomments, comments);
                moreRepliesId.remove(0);
            }

            progressBar.setVisibility(View.GONE);
            if (moreRepliesId.size() > 0) {
                loadMore.setVisibility(View.VISIBLE);
            }
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.comment_row, parent, false);
        makeRandomColorLine(view);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = objects.get(position);
        setViewData(comment, holder);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    /**
     * Generate a random background color for the comment start line
     *
     * @param convertView
     */
    private void makeRandomColorLine(View convertView) {
        Random rnd = new Random();
        int color = Color.argb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        convertView.findViewById(R.id.comment_start_line).setBackgroundColor(color);
    }

    /**
     * Function to set the data for each of the view elements
     *
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
     * Recursive code to create threaded comments
     *
     * @param subcomments
     * @param replies
     */
    private void createThreadedComments(LinearLayout subcomments, final List<Comment> replies) {
        if (replies.size() > 0) {
            for (final Comment reply : replies) {
                View view = LayoutInflater.from(context).inflate(R.layout.comment_row, subcomments, false);
                makeRandomColorLine(view);
                setViewData(reply, new ViewHolder(view));
                subcomments.addView(view);
            }
        }
    }

    public void clear() {
        objects.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Comment> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }
}
