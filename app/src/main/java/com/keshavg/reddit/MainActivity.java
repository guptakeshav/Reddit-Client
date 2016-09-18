package com.keshavg.reddit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.IOException;
import java.util.List;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String OAUTH_URL = "https://www.reddit.com/api/v1/authorize";
    private final String CLIENT_ID = "v438H_DJQuG0oQ";
    private final String CLIENT_SECRET = "";
    private final String RESPONSE_TYPE = "code";
    private final String STATE = "TEST";
    private final String REDIRECT_URI = "http://localhost";
    private final String DURATION = "permanent";
    private final String SCOPE = "modothers,modposts,report,subscribe,livemanage,history,creddits," +
            "modflair,modwiki,vote,wikiread,mysubreddits,flair,modself,submit,modcontributors," +
            "account,modtraffic,read,modlog,modmail,edit,modconfig,save,privatemessages,identity,wikiedit";

    private SharedPreferences pref;
    private String currentPagerUrl;

    private FloatingActionMenu fam;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private Menu menu;
    private SubMenu subredditsMenu;
    private MenuItem prevMenuItem;

    private final int AUTH_REQUEST_CODE = 1;
    private final String GRANT_TYPE = "authorization_code";
    private Button auth;
    private TextView username;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        pref = getSharedPreferences("AuthPref", MODE_PRIVATE);
        changePosts("Frontpage");

        fam = (FloatingActionMenu) findViewById(R.id.fab_menu);
        fam.setClosedOnTouchOutside(true);

        FloatingActionButton fab_posts = (FloatingActionButton) findViewById(R.id.fab_posts);
        fab_posts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity("posts");
            }
        });

        FloatingActionButton fab_subreddits = (FloatingActionButton) findViewById(R.id.fab_subreddits);
        fab_subreddits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearchActivity("subreddits");
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
        auth = (Button) navViewHeader.findViewById(R.id.auth);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAuthButton();
            }
        });
        username = (TextView) navViewHeader.findViewById(R.id.username);
        logout = (Button) navViewHeader.findViewById(R.id.logout);
    }

    public void changePosts(String category) {
        setTitle("Reddit - " + category);
        currentPagerUrl = category;
        setupViewPager(currentPagerUrl);
    }

    private void setupViewPager(String url) {
        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager());

        String[] sortByList = {"hot", "new", "rising", "controversial", "top"};
        for (String sortBy : sortByList) {
            adapter.addFragment(PostsFragment.newInstance(url, sortBy), sortBy);
        }
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void openSearchActivity(String type) {
        Intent i = new Intent(MainActivity.this, SearchActivity.class);
        i.putExtra("Type", type);
        MainActivity.this.startActivity(i);
        fam.close(true);
    }

    private void setupNavBar(NavigationView navigationView) {
        menu = navigationView.getMenu();
        menu.add("Frontpage");
        menu.getItem(0).setChecked(true);
        prevMenuItem = menu.getItem(0);

        subredditsMenu = menu.addSubMenu("Subreddits");
        fetchSubredditNames();
    }

    private void fetchSubredditNames() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<List<String>> call = apiService.getSubredditNames();
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> subredditNames = response.body();
                for (String subredditName : subredditNames) {
                    subredditsMenu.add("r/" + subredditName);
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Toast.makeText(getApplicationContext(),
                        "Error fetching the list of subreddits",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
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
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.reload_app) {
            setupViewPager(currentPagerUrl);
            fetchSubredditNames();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        prevMenuItem.setChecked(false);
        item.setChecked(true);
        prevMenuItem = item;

        changePosts(item.getTitle().toString());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onClickAuthButton() {
        String url = OAUTH_URL
                + "?client_id=" + CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&state=" + STATE
                + "&redirect_uri=" + REDIRECT_URI
                + "&duration=" + DURATION
                + "&scope=" + SCOPE;

        Intent i = new Intent(MainActivity.this, WebViewActivity.class);
        i.putExtra("Url", url);
        startActivityForResult(i, AUTH_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTH_REQUEST_CODE) {
            String authCode = data.getStringExtra("AUTH_CODE");

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .authenticator(new Authenticator() {
                        @Override
                        public Request authenticate(Route route, okhttp3.Response response) throws IOException {
                            String credential = okhttp3.Credentials.basic(CLIENT_ID, CLIENT_SECRET);
                            return response.request().newBuilder()
                                    .header("Authorization", credential)
                                    .build();
                        }
                    }).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.reddit.com")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiInterface apiService = retrofit.create(ApiInterface.class);

            Call<AuthAccessResponse> call = apiService.getAccessToken(GRANT_TYPE, authCode, REDIRECT_URI);
            call.enqueue(new Callback<AuthAccessResponse>() {
                @Override
                public void onResponse(Call<AuthAccessResponse> call, Response<AuthAccessResponse> response) {

                    if (response.isSuccessful()) {
                        String accessToken = response.body().getAccessToken();
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("ACCESS_TOKEN", accessToken);
                        editor.commit();

                        loggedIn();
                    } else {
                        showToast(response.message());
                    }
                }

                @Override
                public void onFailure(Call<AuthAccessResponse> call, Throwable t) {
                    showToast(getString(R.string.server_error));
                }
            });
        }
    }

    private void loggedIn() {
        getUsername();
    }

    private void getUsername() {
        String accessToken = pref.getString("ACCESS_TOKEN", "");
        Log.d("Access Token", accessToken);
        Retrofit retrofit = ApiClient.getOauthClient();
        ApiInterface apiService = retrofit.create(ApiInterface.class);

        Call<User> call = apiService.getUsername("bearer " + accessToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    String name = response.body().getName();

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("USERNAME", name);
                    editor.commit();

                    auth.setVisibility(View.GONE);
                    logout.setVisibility(View.VISIBLE);
                    username.setVisibility(View.VISIBLE);
                    username.setText("Welcome, " + name);
                } else {
                    showToast(response.message());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showToast(getString(R.string.server_error));
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}