package com.keshavg.reddit;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.keshavg.reddit.Constants.BASE_URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PostsFragment postsFragment;
    private PostsFragment searchFragment;

    private Menu menu;
    private MenuItem prevMenuItem;

    private String[] categories = {"hot", "new", "rising", "controversial", "top"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postsFragment = PostsFragment.newInstance(BASE_URL + "/api/v1/hot");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.posts_fragment_container, postsFragment)
                .commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSearchDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        createMenuBar();
    }

    private void createMenuBar() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();
        for (String category : categories) {
            menu.add(category);
        }
        
        setTitle("Reddit - hot");
        menu.getItem(0).setChecked(true);
        prevMenuItem = menu.getItem(0);

        final SubMenu subMenu = menu.addSubMenu("Subreddits");
        try {
            getSubredditsList(subMenu);
        } catch (Exception e) {
            e.printStackTrace();
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
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONArray subreddits = new JSONArray(response.body().string());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                for (int idx = 0; idx < subreddits.length(); ++idx) {
                                    subMenu.add("r/" + subreddits.getString(idx));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
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
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                setTitle("Reddit - " + prevMenuItem.getTitle().toString());
                prevMenuItem.setChecked(true);
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        prevMenuItem.setChecked(false);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        setTitle("Reddit - " + item.getTitle().toString());
        item.setChecked(true);

        prevMenuItem = item;

        String url = BASE_URL + "/api/v1/" + item.getTitle().toString();
        postsFragment.fetchNewPosts(url);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showSearchDialog() {
        View promptView = LayoutInflater.from(MainActivity.this).inflate(R.layout.input_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.search_text);

        alertDialogBuilder
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String searchQuery = editText.getText().toString();
                        setTitle("Results - " + searchQuery);
                        prevMenuItem.setChecked(false);

                        String url = BASE_URL + "/api/v1/search/" + searchQuery;

                        /**
                         * Checking if already on the search fragment
                         * Then only update the list of posts
                         * No need to create a new fragment
                         */
                        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                            searchFragment.fetchNewPosts(url);
                        } else {
                            searchFragment = PostsFragment.newInstance(url);
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.posts_fragment_container, searchFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
