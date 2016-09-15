package com.keshavg.reddit;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/11/16.
 */
public class SubredditsAdapter extends RecyclerView.Adapter<SubredditsAdapter.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private Activity activity;
    private List<Subreddit> objects;

    public SubredditsAdapter(Activity activity) {
        this.activity = activity;
        this.objects = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView name;
        TextView description;

        public ViewHolder(View itemView) {
            super(itemView);

            linearLayout = (LinearLayout) itemView.findViewById(R.id.subreddit);
            name = (TextView) linearLayout.findViewById(R.id.subreddit_name);
            description = (TextView) linearLayout.findViewById(R.id.subreddit_description);
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
                .inflate(R.layout.subreddit_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Subreddit subreddit = objects.get(position);

        holder.name.setText(subreddit.getName());
        holder.description.setText(subreddit.getDescription());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubreddit(subreddit.getName());
            }
        });
    }

    private void onClickSubreddit(String subreddit) {
        ((SearchActivity) activity).showSubredditPosts(subreddit);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public void clear() {
        objects.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Subreddit> objects) {
        this.objects.addAll(objects);
        notifyDataSetChanged();
    }
}