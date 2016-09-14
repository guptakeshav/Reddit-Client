package com.keshavg.reddit;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class CommentsActivity extends AppCompatActivity {
    private String url;
    private String title;

    private TextView textView;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            title = extras.getString("Title");
            url = "api/v1" + extras.getString("Url");
            url = url.substring(0, url.length() - 1);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        textView = (TextView) findViewById(R.id.post_title);
        textView.setText(title);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setUpViewPager();
    }

    private void setUpViewPager() {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        String[] sortByList = {"best", "top", "new", "controversial", "old", "qa&a"};
        for (String sortBy: sortByList) {
            adapter.addFragment(CommentsFragment.newInstance(url + "/" + sortBy), sortBy);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}