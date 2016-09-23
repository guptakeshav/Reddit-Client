package com.keshavg.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

public class CommentsActivity extends AppCompatActivity {
    @Setter
    public static final int COMMENT_SUBMIT_REQUEST_CODE = 1;

    private String url;
    private String title;
    private TextView titleTextView;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            title = extras.getString("TITLE");
            url = extras.getString("URL");
            url = url.substring(1, url.length() - 2);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleTextView = (TextView) findViewById(R.id.post_title);
        titleTextView.setText(title);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        fragments = new ArrayList<>();

        fab = (FloatingActionButton) findViewById(R.id.comment_create);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCreateComment();
            }
        });

        setUpViewPager();
    }

    private void setUpViewPager() {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        String[] sortByList = {"best", "top", "new", "controversial", "old", "qa&a"};
        for (int idx = 0; idx < sortByList.length; ++idx) {
            String sortBy = sortByList[idx];

            Fragment fragment = CommentsFragment.newInstance(url, sortBy, false);
            fragments.add(fragment);
            adapter.addFragment(fragment, sortBy);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        finish();
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
        if (requestCode == COMMENT_SUBMIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int position = tabLayout.getSelectedTabPosition();

                String parentId = data.getStringExtra("PARENT_ID");
                Comment submittedComment = (Comment) data.getSerializableExtra("COMMENT");

                if (parentId.startsWith("t3_")) { // comment directly to the post
                    ((CommentsFragment) fragments.get(position)).getCommentsAdapter()
                            .add(0, submittedComment);
                } else {
                    ((CommentsFragment) fragments.get(position)).getCommentsAdapter()
                            .add(parentId, submittedComment);
                }
            }
        }
    }

    private void onClickCreateComment() {
        Intent i = new Intent(CommentsActivity.this, SubmitCommentActivity.class);
        i.putExtra("PARENT_ID", "t3_" + url.split("/")[3]);
        startActivityForResult(i, COMMENT_SUBMIT_REQUEST_CODE);
    }
}