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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshavgupta on 9/13/16.
 */
public class CommentsFragment extends Fragment {
    private String url;

    private List<Comment> comments;
    private CommentsAdapter commentsAdapter;

    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recList;
    private LinearLayoutManager llm;

    private ProgressBar progressBar;

    public static CommentsFragment newInstance(String url) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    private class FetchComments extends AsyncTask<String, Void, List<Comment>> {
        private Boolean ioExceptionFlag, jsonExceptionFlag;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ioExceptionFlag = false;
            jsonExceptionFlag = false;

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
                Toast.makeText(getActivity(), getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                        .show();
            } else if(jsonExceptionFlag == true) {
                Toast.makeText(getActivity(), String.format(getString(R.string.json_exception), "comments"), Toast.LENGTH_SHORT)
                        .show();
            } else {
                commentsAdapter.addAll(comments);
            }

            progressBar.setVisibility(View.GONE);
            swipeContainer.setRefreshing(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.url = getArguments().getString("url");

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getContext(), url, comments);

        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressbar_comments);

        fetchNewComments(url);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recList = (RecyclerView) view.findViewById(R.id.recycler_list);
        recList.setAdapter(commentsAdapter);
        llm = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recList.setLayoutManager(llm);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewComments(url);
            }
        });
    }

    /**
     * Function to fetch comments from the starting
     * @param url
     */
    public void fetchNewComments(String url) {
        this.url = url;
        commentsAdapter.clear();
        new FetchComments().execute(url);
    }
}
