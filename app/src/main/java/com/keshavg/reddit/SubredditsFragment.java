package com.keshavg.reddit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshav.g on 22/08/16.
 */
public class SubredditsFragment extends Fragment {
    private NetworkTasks networkTasks;
    private Boolean loadingFlag;
    private FetchSubreddits fetchSubreddits;

    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private List<Subreddit> subreddits;
    private SubredditsAdapter subredditsAdapter;

    private String url;
    private String afterParam;

    public SubredditsFragment() {
    }

    public static SubredditsFragment newInstance(String url) {
        SubredditsFragment fragment = new SubredditsFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    private class FetchSubreddits extends AsyncTask<String, Void, List<Subreddit>> {
        private Boolean ioExceptionFlag, jsonExceptionFlag;
        private Boolean clearAdapterFlag;

        public FetchSubreddits(Boolean clearAdapterFlag) {
            this.clearAdapterFlag = clearAdapterFlag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ioExceptionFlag = false;
            jsonExceptionFlag = false;

            loadingFlag = true;
            progressBar.setVisibility(View.VISIBLE);
            subreddits = new ArrayList<>();
        }

        @Override
        protected List<Subreddit> doInBackground(String... params) {
            List<Subreddit> subreddits = null;

            try {
                JSONObject jsonObject = networkTasks.fetchJSONFromUrl(params[0]);
                afterParam = jsonObject.getString("after");
                JSONArray subredditsJSON = jsonObject.getJSONArray("data");
                subreddits = networkTasks.fetchSubredditsList(subredditsJSON);
            } catch (IOException ioE) {
                ioE.printStackTrace();
                ioExceptionFlag = true;
            } catch (JSONException jsonE) {
                jsonE.printStackTrace();
                jsonExceptionFlag = true;
            }

            return subreddits;
        }

        @Override
        protected void onPostExecute(List<Subreddit> subreddits) {
            super.onPostExecute(subreddits);

            if (ioExceptionFlag == true) {
                Toast.makeText(getContext(), getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                        .show();
            } else if(jsonExceptionFlag == true) {
                Toast.makeText(getContext(), String.format(getString(R.string.json_exception), "subreddits"), Toast.LENGTH_SHORT)
                        .show();
            } else {
                if (clearAdapterFlag == true) {
                    subredditsAdapter.clear();
                }
                subredditsAdapter.addAll(subreddits);
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
        subreddits = new ArrayList<>();
        subredditsAdapter = new SubredditsAdapter(getActivity(), subreddits);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_posts);

        fetchNewSubreddits(getArguments().getString("url"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        recList = (RecyclerView) rootView.findViewById(R.id.recycler_list);
        recList.setAdapter(subredditsAdapter);
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
                fetchNewSubreddits(url);
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

            fetchSubreddits = new FetchSubreddits(false);
            fetchSubreddits.execute(paramUrl);
        }
    }

    /**
     * Get all posts from the starting for a particular link
     *
     * @param url
     */
    public void fetchNewSubreddits(String url) {
        this.url = url;

        /**
         * If trying to load some other posts,
         * Cancel loading them
         */
        if (loadingFlag == true) {
            fetchSubreddits.cancel(true);
        }

        fetchSubreddits = new FetchSubreddits(true);
        fetchSubreddits.execute(url);
    }
}