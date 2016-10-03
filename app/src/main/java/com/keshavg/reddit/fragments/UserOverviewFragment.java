package com.keshavg.reddit.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.UserTrophyAdapter;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.models.AddFriend;
import com.keshavg.reddit.models.User;
import com.keshavg.reddit.models.UserResponse;
import com.keshavg.reddit.models.UserTrophyResponse;
import com.keshavg.reddit.network.RedditApiClient;
import com.keshavg.reddit.network.RedditApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keshavgupta on 9/23/16.
 */

public class UserOverviewFragment extends Fragment {
    public static final String USERNAME = "USERNAME";

    private String username;

    private ProgressBar progressBar;
    private LinearLayout userOverview;

    private TextView usernameView;
    private ImageButton addFriend;
    private TextView joined;
    private TextView postKarma;
    private TextView commentKarma;
    private TextView noTrophyAvaiable;
    private RecyclerView trophyCase;

    public static UserOverviewFragment newInstance(String username) {
        UserOverviewFragment fragment = new UserOverviewFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        username = getArguments().getString(USERNAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_overview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        userOverview = (LinearLayout) view.findViewById(R.id.user_overview);

        usernameView = (TextView) view.findViewById(R.id.username);
        addFriend = (ImageButton) view.findViewById(R.id.add_friend);
        joined = (TextView) view.findViewById(R.id.joined);
        postKarma = (TextView) view.findViewById(R.id.post_karma);
        commentKarma = (TextView) view.findViewById(R.id.comment_karma);
        noTrophyAvaiable = (TextView) view.findViewById(R.id.no_trophy_available);

        trophyCase = (RecyclerView) view.findViewById(R.id.trophy_list);
        LinearLayoutManager llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        trophyCase.setLayoutManager(llm);

        fetchUserData();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void fetchUserData() {
        RedditApiInterface apiService;
        Call<UserResponse> call;

        if (!AuthSharedPrefHelper.isLoggedIn()) {
            apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
            call = apiService.getUserOverview(username);
        } else {
            apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);
            call = apiService.getOAuthUserOverview(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    username
            );
        }

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    final User user = response.body().getUser();

                    usernameView.setText(user.getUsername());
                    joined.setText("Redditor since " + user.getCreated());
                    postKarma.setText(user.getPostKarma());
                    commentKarma.setText(user.getCommentKarma());

                    if (!user.getUsername().equals(AuthSharedPrefHelper.getUsername())) {
                        addFriend.setVisibility(View.VISIBLE);
                    } else {
                        addFriend.setVisibility(View.GONE);
                    }

                    addFriend.setOnClickListener(v -> onClickAddFriend(user));

                    if (user.getIsFriend()) {
                        addFriend.setColorFilter(getResources().getColor(R.color.colorAccent));
                    } else {
                        addFriend.setColorFilter(getResources().getColor(android.R.color.black));
                    }

                    fetchUserTrophies();
                } else {
                    showToast("Overivew - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showToast(getContext().getString(R.string.error_server_connect));
            }
        });
    }

    private void onClickAddFriend(final User user) {
        RedditApiInterface apiService = RedditApiClient.getOAuthClient().create(RedditApiInterface.class);

        Call<Void> call;
        if (!user.getIsFriend()) {
            addFriend.setColorFilter(getResources().getColor(R.color.colorAccent));
            call = apiService.addFriend(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    username,
                    new AddFriend(username)
            );
        } else {
            addFriend.setColorFilter(getResources().getColor(android.R.color.black));
            call = apiService.removeFriend(
                    "bearer " + AuthSharedPrefHelper.getAccessToken(),
                    username
            );
        }

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    if (!user.getIsFriend()) {
                        addFriend.setColorFilter(getResources().getColor(android.R.color.black));
                    } else {
                        addFriend.setColorFilter(getResources().getColor(R.color.colorAccent));
                    }

                    showToast("Friend - " + response.message());
                } else {
                    user.setIsFriend(!user.getIsFriend());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast(getString(R.string.error_server_connect));
            }
        });
    }

    private void fetchUserTrophies() {
        RedditApiInterface apiService = RedditApiClient.getClient().create(RedditApiInterface.class);
        Call<UserTrophyResponse> call = apiService.getUserTrophies(username);

        call.enqueue(new Callback<UserTrophyResponse>() {
            @Override
            public void onResponse(Call<UserTrophyResponse> call, Response<UserTrophyResponse> response) {
                if (response.isSuccessful()) {

                    if (response.body().getTrophyList().size() > 0) {
                        noTrophyAvaiable.setVisibility(View.GONE);
                        UserTrophyAdapter adapter = new UserTrophyAdapter(
                                getActivity(),
                                response.body().getTrophyList()
                        );
                        trophyCase.setAdapter(adapter);
                    }

                    progressBar.setVisibility(View.GONE);
                    userOverview.setVisibility(View.VISIBLE);
                } else {
                    showToast("User Trophies - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserTrophyResponse> call, Throwable t) {
                showToast(getResources().getString(R.string.error_server_connect));
            }
        });
    }
}