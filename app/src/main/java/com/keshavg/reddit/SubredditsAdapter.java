package com.keshavg.reddit;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 9/11/16.
 */
public class SubredditsAdapter extends RecyclerView.Adapter<SubredditsAdapter.ViewHolder>
        implements SectionTitleProvider {
    private Activity activity;
    private List<Subreddit> objects;

    public SubredditsAdapter(Activity activity) {
        this.activity = activity;
        this.objects = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton subscribe;
        TextView name;
        TextView subscribers;
        TextView created;
        TextView description;

        public ViewHolder(View itemView) {
            super(itemView);

            subscribe = (ImageButton) itemView.findViewById(R.id.subreddit_subscribe);
            name = (TextView) itemView.findViewById(R.id.subreddit_name);
            subscribers = (TextView) itemView.findViewById(R.id.subreddit_subscribers);
            created = (TextView) itemView.findViewById(R.id.subreddit_created);
            description = (TextView) itemView.findViewById(R.id.subreddit_description);
        }
    }

    @Override
    public String getSectionTitle(int position) {
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Subreddit subreddit = objects.get(position);

        if (subreddit.getIsSubscribed() != null && subreddit.getIsSubscribed().equals(false)) {
            holder.subscribe.setBackgroundColor(activity.getColor(R.color.darkGrey));
        } else {
            holder.subscribe.setBackgroundColor(activity.getColor(R.color.colorAccent));
        }

        holder.subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubscribe(subreddit, holder.subscribe);
            }
        });

        holder.name.setText(subreddit.getSubreddit());
        holder.subscribers.setText(subreddit.getSubscribers() + " subscribers");
        holder.created.setText(subreddit.getCreated());

        if (subreddit.getDescription() != null) {
            holder.description.setText(Html.fromHtml(subreddit.getDescription()));
        }

        holder.description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubreddit(subreddit.getSubreddit());
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

    private void showToast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    private void onClickSubscribe(final Subreddit subreddit, final ImageButton subscribe) {
        if (!MainActivity.AuthPrefManager.isLoggedIn()) {
            showToast(activity.getString(R.string.login_error));
            return;
        }

        String action;
        if (subreddit.getIsSubscribed().equals(false)) {
            subscribe.setBackgroundColor(activity.getColor(R.color.colorAccent));
            subreddit.setIsSubscribed(true);
            action = "sub";
        } else {
            subscribe.setBackgroundColor(activity.getColor(R.color.darkGrey));
            subreddit.setIsSubscribed(false);
            action = "unsub";
        }

        ApiInterface apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
        Call<Void> call = apiService.subscribeSubreddit(
                "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                action,
                subreddit.getName()
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    showToast("Subscribing - " + response.message());

                    if (subreddit.getIsSubscribed().equals(true)) {
                        subscribe.setBackgroundColor(activity.getColor(R.color.colorAccent));
                        subreddit.setIsSubscribed(true);
                    } else {
                        subscribe.setBackgroundColor(activity.getColor(R.color.darkGrey));
                        subreddit.setIsSubscribed(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(activity.getString(R.string.server_error));
            }
        });
    }
}