package com.keshavg.reddit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.ViewPagerFragmentAdapter;
import com.keshavg.reddit.fragments.CommentsFragment;
import com.keshavg.reddit.models.Comment;
import com.keshavg.reddit.services.LoginService;
import com.keshavg.reddit.utils.Constants;
import com.keshavg.reddit.utils.ValidateUtil;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    public static final String COMMENT = "COMMENT";
    public static final String ID = "ID";
    public static final String TITLE = "TITLE";
    public static final String URL = "URL";
    public static final String PARENT_ID = "PARENT_ID";

    public static final int COMMENT_SUBMIT_REQUEST_CODE = 100;
    public static final int RESULT_SUBMIT_COMMENT = 101;
    public static final int RESULT_EDIT_COMMENT = 102;

    private String url;
    private String title;

    private CoordinatorLayout coordinatorLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerFragmentAdapter adapter;
    private List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            title = extras.getString(TITLE);
            url = extras.getString(URL);
            url = url.substring(1, url.length());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView titleView = (TextView) findViewById(R.id.post_title);
        titleView.setText(title);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        fragments = new ArrayList<>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.comment_create);
        fab.setOnClickListener(v -> onClickCreateComment());

        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        setUpViewPager();
    }

    private void setUpViewPager() {
        adapter.clear();

        String[] sortByList = {"best", "top", "new", "controversial", "old", "qa&a"};
        for (String sortBy : sortByList) {
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
        switch (requestCode) {
            case COMMENT_SUBMIT_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_SUBMIT_COMMENT:
                        int position = tabLayout.getSelectedTabPosition();

                        String parentId = data.getStringExtra(PARENT_ID);
                        Comment submittedComment = (Comment) data.getSerializableExtra(COMMENT);

                        if (parentId.startsWith(Constants.POST_PREFIX)) { // comment directly to the post
                            ((CommentsFragment) fragments.get(position)).getCommentsAdapter()
                                    .add(0, submittedComment);
                        } else {
                            ((CommentsFragment) fragments.get(position)).getCommentsAdapter()
                                    .add(parentId, submittedComment);
                        }

                        break;

                    case RESULT_EDIT_COMMENT:
                        position = tabLayout.getSelectedTabPosition();

                        String id = data.getStringExtra(ID);
                        Comment editComment = (Comment) data.getSerializableExtra(COMMENT);

                        ((CommentsFragment) fragments.get(position)).getCommentsAdapter()
                                .edit(id, editComment);

                        break;
                }

                break;

            case LoginService.AUTH_CODE_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        LoginService.fetchAccessToken(
                                getApplicationContext(),
                                data.getStringExtra(LoginService.AUTH_CODE),
                                this::setUpViewPager
                        );
                }
        }
    }

    private void onClickCreateComment() {
        if (ValidateUtil.loginValidation(coordinatorLayout, CommentsActivity.this)) {
            Intent i = new Intent(CommentsActivity.this, SubmitCommentActivity.class);
            i.putExtra(PARENT_ID, Constants.POST_PREFIX + url.split(Constants.LINK_SEPARATOR)[3]);
            startActivityForResult(i, COMMENT_SUBMIT_REQUEST_CODE);
        }
    }
}