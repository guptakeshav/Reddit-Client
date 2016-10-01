package com.keshavg.reddit.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.keshavg.reddit.R;
import com.keshavg.reddit.models.UserTrophyResponse;
import com.keshavg.reddit.utils.DeviceUtil;

import java.util.List;

/**
 * Created by keshavgupta on 10/1/16.
 */

public class UserTrophyAdapter extends RecyclerView.Adapter<UserTrophyAdapter.ViewHolder> {
    private Context context;
    private List<UserTrophyResponse.Trophy> trophyList;

    public UserTrophyAdapter(Context context, List<UserTrophyResponse.Trophy> trophyList) {
        this.context = context;
        this.trophyList = trophyList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.usertrophy_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UserTrophyResponse.Trophy trophy = trophyList.get(position);

        Glide.with(context)
                .load(trophy.getData().getIcon())
                .override(DeviceUtil.dpToPx(70), DeviceUtil.dpToPx(70))
                .into(holder.trophyView);

        holder.trophyName.setText(trophy.getData().getName());
        holder.trophyDescription.setText(trophy.getData().getDescription());
    }

    @Override
    public int getItemCount() {
        return trophyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView trophyView;
        TextView trophyName;
        TextView trophyDescription;

        public ViewHolder(View itemView) {
            super(itemView);

            trophyView = (ImageView) itemView.findViewById(R.id.trophy_view);
            trophyName = (TextView) itemView.findViewById(R.id.trophy_name);
            trophyDescription = (TextView) itemView.findViewById(R.id.trophy_description);
        }
    }
}
