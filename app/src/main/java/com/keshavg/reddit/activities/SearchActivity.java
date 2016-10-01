package com.keshavg.reddit.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.keshavg.reddit.R;
import com.keshavg.reddit.providers.SearchSuggestionsProvider;
import com.keshavg.reddit.adapters.ViewPagerFragmentAdapter;
import com.keshavg.reddit.fragments.CommentsFragment;
import com.keshavg.reddit.fragments.PostsFragment;
import com.keshavg.reddit.fragments.SubredditsFragment;
import com.keshavg.reddit.fragments.UserOverviewFragment;

public class SearchActivity extends AppCompatActivity {
    private SearchRecentSuggestions suggestions;
    private String searchType;

    private SearchView editText;
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

        suggestions = new SearchRecentSuggestions(
                this,
                SearchSuggestionsProvider.AUTHORITY,
                SearchSuggestionsProvider.MODE
        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        editText = (SearchView) findViewById(R.id.search_text);
        editText.setSubmitButtonEnabled(true);
        editText.setIconifiedByDefault(false);
        editText.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()) );

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById((R.id.viewpager));
        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        if (searchQuery != null) {
            if (searchType.equals("USERS")) {
                editText.setQuery(searchQuery, true);
            } else if (searchType.equals("SUBREDDIT_POSTS")) {
                showSubredditPosts("r/" + searchQuery);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.clear_history) {
            suggestions.clearHistory();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_toolbar, menu);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction) || Intent.ACTION_VIEW.equals(queryAction)) {
            preSearch();

            searchQuery = intent.getDataString(); // from search-bar
            if (searchQuery == null) {
                searchQuery = intent.getStringExtra(SearchManager.QUERY); // from suggestions
                editText.setQuery(searchQuery, false);
            }
            suggestions.saveRecentQuery(searchQuery, null);

            if (searchType.equals("POSTS")) {
                performPostsSearch();
            } else if (searchType.equals("SUBREDDITS")) {
                performSubredditsSearch();
            } else if (searchType.equals("USERS")) {
                performUsersSearch();
            }

            postSearch();
        }
    }

    private void preSearch() {
        adapter.clear();
        tabLayout.setVisibility(View.VISIBLE);
    }

    private void postSearch() {
        editText.clearFocus();

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void performPostsSearch() {
        String[] sortByList = {"relevance", "top", "new", "comments"};
        for (String sortBy : sortByList) {
            adapter.addFragment(
                    PostsFragment.newInstance(searchQuery, sortBy, 1, false, true, false),
                    sortBy);
        }
    }

    private void performSubredditsSearch() {
        adapter.addFragment(
                SubredditsFragment.newInstance(searchQuery),
                "Subreddits");
    }

    private void performUsersSearch() {
        adapter.addFragment(
                UserOverviewFragment.newInstance(searchQuery),
                "Overview");
        adapter.addFragment(
                PostsFragment.newInstance("user/" + searchQuery + "/submitted", "", 0, true, true, false),
                "Submitted");
        adapter.addFragment(
                CommentsFragment.newInstance("user/" + searchQuery + "/comments", "", true),
                "Comments");
        adapter.addFragment(
                CommentsFragment.newInstance("user/" + searchQuery + "/gilded", "", true),
                "Gilded");
    }

    public void showSubredditPosts(String subreddit) {
        preSearch();

        editText.setQuery(subreddit, false);

        String[] sortByList = {"hot", "new", "rising", "controversial", "top"};
        for (String sortBy : sortByList) {
            adapter.addFragment(
                    PostsFragment.newInstance(subreddit, sortBy, 0, false, true, false),
                    sortBy);
        }

        postSearch();
    }
}