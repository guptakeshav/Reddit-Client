package com.keshavg.reddit.activities;

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

public class ProfileActivity extends AppCompatActivity {
    private String username;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            username = extras.getString("USERNAME");
        }

        setTitle(username);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        setUpViewPager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpViewPager() {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

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
