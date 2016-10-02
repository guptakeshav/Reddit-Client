package com.keshavg.reddit.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.keshavg.reddit.R;
import com.keshavg.reddit.adapters.ViewPagerFragmentAdapter;
import com.keshavg.reddit.db.AuthSharedPrefHelper;
import com.keshavg.reddit.fragments.PostsFragment;
import com.keshavg.reddit.interfaces.PerformFunction;
import com.keshavg.reddit.models.Subreddit;
import com.keshavg.reddit.services.LoginService;
import com.keshavg.reddit.services.SubredditInfoService;
import com.keshavg.reddit.services.UserInfoService;
import com.keshavg.reddit.utils.ValidateUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String PROFILE = "Profile";
    public static final String FRONTPAGE = "Frontpage";
    public static final String SUBSCRIPTIONS = "Subscriptions";

    private CoordinatorLayout coordinatorLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionMenu fam;

    private SubMenu subredditsMenu;
    private MenuItem prevMenuItem;

    private TextView usernameView;
    private Button authButton;
    private Button logoutButton;

    private String currentPagerUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthSharedPrefHelper.createInstance(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        fam = (FloatingActionMenu) findViewById(R.id.fab_menu);
        fam.setClosedOnTouchOutside(true);

        FloatingActionButton fab_posts = (FloatingActionButton) findViewById(R.id.fab_posts);
        fab_posts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity(SearchActivity.POSTS);
            }
        });

        FloatingActionButton fab_subreddits = (FloatingActionButton) findViewById(R.id.fab_subreddits);
        fab_subreddits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity(SearchActivity.SUBREDDITS);
            }
        });

        FloatingActionButton fab_users = (FloatingActionButton) findViewById(R.id.fab_user);
        fab_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity(SearchActivity.USERS);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setupNavBar(navigationView);

        View navViewHeader = navigationView.getHeaderView(0);
        authButton = (Button) navViewHeader.findViewById(R.id.auth);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginService.fetchAuthCode(MainActivity.this);
            }
        });

        usernameView = (TextView) navViewHeader.findViewById(R.id.username);
        logoutButton = (Button) navViewHeader.findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginService.logoutUser(getApplicationContext(), new PerformFunction() {
                    @Override
                    public void execute() {
                        onLogout();
                    }
                });
            }
        });

        if (AuthSharedPrefHelper.isLoggedIn()) {
            if (AuthSharedPrefHelper.isSessionExpired()) {
                AuthSharedPrefHelper.clearPreferences();
                showToast(getString(R.string.error_session_expire));
            } else {
                showLoggedIn();
            }
        }

        changePosts("");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (authButton.getVisibility() == View.VISIBLE && AuthSharedPrefHelper.isLoggedIn()) {
            onLogin();
        }
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
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
                                onLogin();
                            }
                        }
                );
            }
        }
    }

    private void onLogin() {
        UserInfoService.fetchUsername(getApplicationContext(), new PerformFunction() {
            @Override
            public void execute() {
                showLoggedIn();
            }
        });

        reloadApp();
    }

    private void showLoggedIn() {
        authButton.setVisibility(View.GONE);
        logoutButton.setVisibility(View.VISIBLE);
        usernameView.setVisibility(View.VISIBLE);

        usernameView.setText("Welcome, " + AuthSharedPrefHelper.getUsername());
    }

    private void onLogout() {
        usernameView.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
        authButton.setVisibility(View.VISIBLE);

        reloadApp();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fab_menu);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fam.isOpened()) {
            fam.close(true);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.reload_app) {
            reloadApp();
        } else if (id == R.id.create_post) {
            createPost();
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadApp() {
        getSubscriptionsList();
        setupViewPager(currentPagerUrl);
    }

    private void createPost() {
        if (ValidateUtil.loginValidation(coordinatorLayout, MainActivity.this)) {
            Intent i = new Intent(MainActivity.this, SubmitPostActivity.class);
            startActivity(i);
        }
    }

    private void setupNavBar(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        menu.add(PROFILE);
        menu.add(FRONTPAGE);
        prevMenuItem = menu.getItem(1);
        prevMenuItem.setChecked(true);

        subredditsMenu = menu.addSubMenu(SUBSCRIPTIONS);
        getSubscriptionsList();
    }

    private void getSubscriptionsList() {
        final SubredditInfoService subredditInfoService = new SubredditInfoService();
        subredditInfoService.fetchSubscriptions(getApplicationContext(), new PerformFunction() {
            @Override
            public void execute() {
                subredditsMenu.clear();
                for (Subreddit subreddit : subredditInfoService.getSubreddits()) {
                    subredditsMenu.add(subreddit.getSubredditName());
                }
            }
        });
    }

    private void setupViewPager(String url) {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        String[] sortByList = {"hot", "new", "rising", "controversial", "top"};
        for (String sortBy : sortByList) {
            adapter.addFragment(PostsFragment.newInstance(url, sortBy, 0, false, true, false), sortBy);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getTitle().equals(PROFILE)) {
            openProfile(AuthSharedPrefHelper.getUsername());
        } else {
            prevMenuItem.setChecked(false);
            item.setChecked(true);
            prevMenuItem = item;

            if (item.getTitle().equals(FRONTPAGE)) {
                changePosts("");
            } else {
                changePosts(item.getTitle().toString());
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void changePosts(String subreddit) {
        setTitle("Reddit - " + prevMenuItem.getTitle());
        currentPagerUrl = subreddit;
        setupViewPager(currentPagerUrl);
    }

    private void openSearchActivity(String type) {
        Intent i = new Intent(MainActivity.this, SearchActivity.class);
        i.putExtra(SearchActivity.TYPE, type);
        startActivity(i);
        fam.close(true);
    }

    private void openProfile(String username) {
        if (ValidateUtil.loginValidation(coordinatorLayout, MainActivity.this)) {
            Intent i = new Intent(MainActivity.this, ProfileActivity.class);
            i.putExtra(ProfileActivity.USERNAME, username);
            startActivity(i);
        }
    }
}