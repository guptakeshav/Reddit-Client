package com.keshavg.reddit;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.GONE;

public class SearchActivity extends AppCompatActivity {
    private String searchType;

    private EditText editText;
    private String searchQuery;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerFragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            searchType = extras.getString("TYPE");

            if (extras.containsKey("SEARCH_QUERY")) {
                searchQuery = extras.getString("SEARCH_QUERY");
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editText = (EditText) findViewById(R.id.search_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (v.getText().toString().trim().matches("")) {
                        Toast.makeText(getApplicationContext(),
                                getResources().getText(R.string.empty_search),
                                Toast.LENGTH_SHORT)
                                .show();
                        return false;
                    }

                    tabLayout.setVisibility(View.VISIBLE);
                    searchQuery = v.getText().toString();

                    if (searchType.equals("POSTS")) {
                        performPostsSearch();
                    } else if (searchType.equals("SUBREDDITS")) {
                        performSubredditsSearch();
                    } else if (searchType.equals("USERS")) {
                        performUsersSearch();
                    }

                    InputMethodManager imm = (InputMethodManager)
                            SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }

                return false;
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById((R.id.viewpager));
        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        if (searchQuery != null) {
            if (searchType.equals("USERS")) {
                tabLayout.setVisibility(View.VISIBLE);
                editText.setText(searchQuery);
                performUsersSearch();
            } else if (searchType.equals("SUBREDDITS")) {
                tabLayout.setVisibility(View.VISIBLE);
                showSubredditPosts("r/" + searchQuery);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (adapter.getCount() > 0) {
            editText.setText(null);
            tabLayout.setVisibility(GONE);
            adapter.clear();
            viewPager.setAdapter(adapter);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void performPostsSearch() {
        adapter.clear();

        String[] sortByList = {"relevance", "top", "new", "comments"};
        for (String sortBy : sortByList) {
            adapter.addFragment(PostsFragment.newInstance(searchQuery, sortBy, 1, false, true, false), sortBy);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void performSubredditsSearch() {
        adapter.clear();

        adapter.addFragment(SubredditsFragment.newInstance(searchQuery), "Subreddits");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void performUsersSearch() {
        adapter.clear();

        adapter.addFragment(UserOverviewFragment.newInstance(searchQuery), "Overview");
        adapter.addFragment(PostsFragment.newInstance("user/" + searchQuery + "/submitted", "", 0, true, true, false), "Submitted");
        adapter.addFragment(CommentsFragment.newInstance("user/" + searchQuery + "/comments", "", true), "Comments");
        adapter.addFragment(CommentsFragment.newInstance("user/" + searchQuery + "/gilded", "", true), "Gilded");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void showSubredditPosts(String subreddit) {
        adapter.clear();
        editText.setText(subreddit);

        String[] sortByList = {"hot", "new", "rising", "controversial", "top"};
        for (String sortBy : sortByList) {
            adapter.addFragment(PostsFragment.newInstance(subreddit, sortBy, 0, false, true, false), sortBy);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}