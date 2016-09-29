package com.keshavg.reddit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.List;

import retrofit2.Call;

/**
 * Created by keshavgupta on 9/29/16.
 */

public class SubredditAutocompleteAdapter extends ArrayAdapter<String> {
    private List<String> mData;

    public SubredditAutocompleteAdapter(Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults filterResults = new FilterResults();

                if (constraint != null) {
                    ApiInterface apiService = ApiClient.getOAuthClient().create(ApiInterface.class);
                    Call<NamesList> call = apiService.getRedditNames(
                            "bearer " + MainActivity.AuthPrefManager.getAccessToken(),
                            constraint.toString()
                    );

                    try {
                        mData = call.execute().body().getNames();
                        filterResults.values = mData;
                        filterResults.count = mData.size();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                clear();

                if (results.count > 0) {
                    for (String subredditName : (List<String>) results.values) {
                        add(subredditName);
                    }
                }

                notifyDataSetChanged();
            }
        };

        return filter;
    }
}
