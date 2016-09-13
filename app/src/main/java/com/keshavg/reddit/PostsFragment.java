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
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

    private RedditPostsDbHelper dbHelper;

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
        private String url;
        private Boolean ioExceptionFlag, jsonExceptionFlag;
        private Boolean clearAdapterFlag;

        public FetchPosts(Boolean clearAdapterFlag) {
            this.clearAdapterFlag = clearAdapterFlag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ioExceptionFlag = false;
            jsonExceptionFlag = false;

            loadingFlag = true;
            progressBar.setVisibility(View.VISIBLE);
            posts = new ArrayList<>();
        }

        @Override
        protected List<Post> doInBackground(String... params) {
            url = params[0];

            List<Post> posts = null;
            try {
                JSONObject jsonObject = networkTasks.fetchJSONFromUrl(url);
                afterParam = jsonObject.getString("after");
                JSONArray redditPosts = jsonObject.getJSONArray("data");
                posts = networkTasks.fetchPostsList(redditPosts);
            } catch (IOException ioE) {
                ioE.printStackTrace();
                ioExceptionFlag = true;
            } catch (JSONException jsonE) {
                jsonE.printStackTrace();
                jsonExceptionFlag = true;
            }

            return posts;
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            super.onPostExecute(posts);

            if (ioExceptionFlag == true || jsonExceptionFlag == true) {
                if (clearAdapterFlag == true) {
                    postsAdapter.clear();
                    postsAdapter.addAll(dbHelper.getPosts(url));
                }

                if (ioExceptionFlag == true) {
                    Toast.makeText(getContext(), getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                            .show();
                } else if (jsonExceptionFlag == true) {
                    Toast.makeText(getContext(), String.format(getString(R.string.json_exception), "posts"), Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                if (clearAdapterFlag == true) {
                    postsAdapter.clear();
                    dbHelper.removePosts(url);
                }

                postsAdapter.addAll(posts);
                dbHelper.insertPosts(posts, url);
            }

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
        posts = new ArrayList<>();
        postsAdapter = new PostsAdapter(getActivity(), posts, Glide.with(getContext()));
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_posts);

        dbHelper = new RedditPostsDbHelper(getContext());

        fetchNewPosts(getArguments().getString("url"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        recList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
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

        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
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

            fetchPosts = new FetchPosts(false);
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

        /**
         * If trying to load some other posts,
         * Cancel loading them
         */
        if (loadingFlag == true) {
            fetchPosts.cancel(true);
        }

        fetchPosts = new FetchPosts(true);
        fetchPosts.execute(url);
    }
}