package com.keshavg.reddit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav.g on 22/08/16.
 */
public class PostsFragment extends Fragment {

    private NetworkTasks networkTasks;
    private Boolean loadingFlag;
    private FetchPosts fetchPosts;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private List<Post> posts;
    private PostsAdapter postsAdapter;

    private String url;
    private String afterParam;

    public PostsFragment() {
    }

    public static PostsFragment newInstance(String url) {
        PostsFragment fragment = new PostsFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    private class FetchPosts extends AsyncTask<String, Void, List<Post>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingFlag = true;
            progressBar.setVisibility(View.VISIBLE);

            posts = new ArrayList<Post>();
        }

        @Override
        protected List<Post> doInBackground(String... params) {
            JSONObject jsonObject = networkTasks.fetchJSONFromUrl(params[0]);
            try {
                afterParam = jsonObject.getString("after");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return networkTasks.fetchPostsList(jsonObject);
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            super.onPostExecute(posts);

            postsAdapter.addAll(posts);
            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
            loadingFlag = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkTasks = new NetworkTasks();
        loadingFlag = false;
        posts = new ArrayList<Post>();
        postsAdapter = new PostsAdapter(getContext(), posts);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_posts);
        fetchNewPosts(getArguments().getString("url"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        postsAdapter = new PostsAdapter(getContext(), posts);

        recList = (RecyclerView) rootView.findViewById(R.id.posts_list);
        recList.setAdapter(postsAdapter);
        llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);
        recList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                loadMorePosts();
            }
        });

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.posts_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewPosts(url);
            }
        });
    }

    /**
     * Fetching more posts when the end of list has reached
     */
    private void loadMorePosts() {
        int totalItemCount = llm.getItemCount();
        int lastVisibleItem = llm.findLastVisibleItemPosition();
        int visibleThreshold = 2;

        if (!loadingFlag && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
            String paramUrl = url + "/" + afterParam;

            fetchPosts = new FetchPosts();
            fetchPosts.execute(paramUrl);
        }
    }

    /**
     * Get all posts from the starting for a particular link
     *
     * @param url
     */
    public void fetchNewPosts(String url) {
        this.url = url;
        postsAdapter.clear();

        if (loadingFlag == true) {
            fetchPosts.cancel(true);
        }

        fetchPosts = new FetchPosts();
        fetchPosts.execute(url);
    }
}
