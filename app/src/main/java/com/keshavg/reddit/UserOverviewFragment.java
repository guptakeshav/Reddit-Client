package com.keshavg.reddit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 9/23/16.
 */

public class UserOverviewFragment extends Fragment {
    private String name;

    private TextView username;
    private TextView joined;
    private TextView postKarma;
    private TextView commentKarma;

    public static UserOverviewFragment newInstance(String username) {
        UserOverviewFragment fragment = new UserOverviewFragment();
        Bundle args = new Bundle();
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = getArguments().getString("USERNAME");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        username = (TextView) view.findViewById(R.id.username);
        joined = (TextView) view.findViewById(R.id.joined);
        postKarma = (TextView) view.findViewById(R.id.post_karma);
        commentKarma = (TextView) view.findViewById(R.id.comment_karma);

        fetchUserData();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void fetchUserData() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UserResponse> call = apiService.getUserOverview(name);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    User user = response.body().getUser();
                    username.setText(user.getName());
                    joined.setText("Redditor since " + user.getCreated());
                    postKarma.setText(user.getPostKarma());
                    commentKarma.setText(user.getCommentKarma());
                } else {
                    showToast("Overivew - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showToast(getContext().getString(R.string.server_error));
            }
        });
    }
}