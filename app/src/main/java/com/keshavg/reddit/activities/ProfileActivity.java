package com.keshavg.reddit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.ViewPagerFragmentAdapter;
import com.keshavg.reddit.fragments.CommentsFragment;
import com.keshavg.reddit.fragments.PostsFragment;
import com.keshavg.reddit.fragments.UserOverviewFragment;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.services.LoginService;

public class ProfileActivity extends AppCompatActivity {
    public static final String USERNAME = "USERNAME";

    private String username;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerFragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            username = extras.getString(USERNAME);
        }

        setTitle(username);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        setUpViewPager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoginService.AUTH_CODE_REQUEST) {
            if (resultCode == RESULT_OK) {
                LoginService.fetchAccessToken(
                        getApplicationContext(),
                        data.getStringExtra(LoginService.AUTH_CODE),
                        new PerformFunction() {
                            @Override
                            public void execute() {
                                setUpViewPager();
                            }
                        }
                );
            }
        }
    }

    private void setUpViewPager() {
        adapter.clear();

        adapter.addFragment(UserOverviewFragment.newInstance(username), "Overview");
        adapter.addFragment(CommentsFragment.newInstance("user/" + username + "/comments", "", true), "Comments");
        adapter.addFragment(PostsFragment.newInstance("user/" + username + "/submitted", "", 0, true, false, false), "Submitted");
        adapter.addFragment(CommentsFragment.newInstance("user/" + username + "/gilded", "", true), "Gilded");
        adapter.addFragment(PostsFragment.newInstance("user/" + username + "/upvoted", "", 0, true, false, false), "Upvoted");
        adapter.addFragment(PostsFragment.newInstance("user/" + username + "/downvoted", "", 0, true, false, false), "Downvoted");
        adapter.addFragment(PostsFragment.newInstance("user/" + username + "/hidden", "", 0, true, false, true), "Hidden");
        adapter.addFragment(PostsFragment.newInstance("user/" + username + "/saved", "", 0, true, false, false), "Saved");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}
