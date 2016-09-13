package com.keshavg.reddit;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.keshavg.reddit.Constants.BASE_URL;

public class SearchActivity extends AppCompatActivity {
    private String type;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            type = extras.getString("Type");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final EditText editText = (EditText) findViewById(R.id.search_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    if (v.getText().toString().trim().matches("")) {
                        Toast.makeText(SearchActivity.this,
                                getResources().getText(R.string.empty_search),
                                Toast.LENGTH_SHORT)
                                .show();
                        return false;
                    }

                    if (type.equals("posts")) {
                        performPostsSearch(v.getText().toString());
                    } else if (type.equals("subreddits")) {
                        performSubredditsSearch(v.getText().toString());
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

        viewPager.setOffscreenPageLimit(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void performPostsSearch(String searchQuery) {
        String[] sortByList = {"relevance", "top", "new", "comments"};

        tabLayout.setVisibility(View.VISIBLE);

        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        for (String sortBy : sortByList) {
            String url = BASE_URL + "/api/v1/search/posts/" + searchQuery + "/" + sortBy;
            adapter.addFragment(PostsFragment.newInstance(url), sortBy);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void performSubredditsSearch(String searchQuery) {
        tabLayout.setVisibility(View.VISIBLE);

        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        String url = BASE_URL + "/api/v1/search/subreddits/" + searchQuery;
        Log.d("Searching", url);
        adapter.addFragment(SubredditsFragment.newInstance(url), "Subreddits");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}