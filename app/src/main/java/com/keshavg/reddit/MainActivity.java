package com.keshavg.reddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.keshavg.reddit.Constants.BASE_URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String currentPagerUrl;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Menu menu;
    private MenuItem prevMenuItem;

    private String[] sortByList = {"hot", "new", "rising", "controversial", "top"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Frontpage");

        currentPagerUrl = "api/v1/Frontpage";
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(currentPagerUrl);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                MainActivity.this.startActivity(i);
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
    }

    private void setupViewPager(String url) {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());
        for (String sortBy: sortByList) {
            adapter.addFragment(PostsFragment.newInstance(BASE_URL + "/" + url + "/" + sortBy), sortBy);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupNavBar(NavigationView navigationView) {
        menu = navigationView.getMenu();
        menu.add("Frontpage");
        menu.getItem(0).setChecked(true);
        prevMenuItem = menu.getItem(0);

        SubMenu subMenu = menu.addSubMenu("Subreddits");
        try {
            getSubredditsList(subMenu);
        } catch (IOException ioE) {
            ioE.printStackTrace();
            Toast.makeText(MainActivity.this, getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void getSubredditsList(final SubMenu subMenu) throws IOException {
        String url = BASE_URL + "/api/v1/subreddits";

        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = new OkHttpClient().newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    final List<String> subredditsList = new ArrayList<>();

                    JSONArray subreddits = new JSONArray(response.body().string());
                    for (int idx = 0; idx < subreddits.length(); ++idx) {
                        subredditsList.add("r/" + subreddits.getString(idx));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (String subreddit : subredditsList) {
                                subMenu.add(subreddit);
                            }
                        }
                    });
                } catch (IOException ioE) {
                    ioE.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, getText(R.string.network_io_exception), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                } catch (JSONException jsonE) {
                    jsonE.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, String.format(getString(R.string.json_exception), "subreddits"), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.refresh_posts) {
            setupViewPager(currentPagerUrl);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        prevMenuItem.setChecked(false);

        setTitle("Reddit - " + item.getTitle().toString());
        item.setChecked(true);
        prevMenuItem = item;

        currentPagerUrl = "api/v1/" + item.getTitle().toString();
        setupViewPager(currentPagerUrl);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}