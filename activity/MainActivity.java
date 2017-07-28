package induk.soft.meeting.indukmeeting.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import induk.soft.meeting.indukmeeting.R;
import induk.soft.meeting.indukmeeting.Utils.MainApplication;
import induk.soft.meeting.indukmeeting.Utils.Preferences;
import induk.soft.meeting.indukmeeting.adapter.FragmentAdapater;
import induk.soft.meeting.indukmeeting.fragment.HomeFragment;
import induk.soft.meeting.indukmeeting.model.MemberDTO;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView textNavEmail, textNavName;
    private ImageView imageNavProfile;
    private TabLayout tabMain;
    private ViewPager viewPager;
    private FragmentAdapater fragmentAdapater;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String user, line, result;
    private LinearLayout linearNavProfile;

    private Preferences preferences = new Preferences();

    UserInfoTask userInfoTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        userInfoTask = new UserInfoTask();
        userInfoTask.execute(user);

        fragmentAdapater.addFragment(new HomeFragment());
        fragmentAdapater.addFragment(new HomeFragment());
        fragmentAdapater.addFragment(new HomeFragment());
        fragmentAdapater.addFragment(new HomeFragment());
        viewPager.setAdapter(fragmentAdapater);

        tabMain.setupWithViewPager(viewPager);
        tabMain.getTabAt(0).setIcon(R.drawable.main_navi_story);
        for (int i = 1; i < tabMain.getTabCount(); i++) {
            tabMain.getTabAt(i).setIcon(R.mipmap.ic_launcher);
        }
        tabMain.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.drawable.main_navi_story);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.main_navi_club);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.main_navi_search);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.main_navi_my);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                    case 1:
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                    case 2:
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                    case 3:
                        tab.setIcon(R.mipmap.ic_launcher);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        linearNavProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyInfoActivity.class));
                finish();
            }
        });
    }

    private void init() {
        textNavEmail = (TextView) findViewById(R.id.text_nav_email);
        textNavName = (TextView) findViewById(R.id.text_nav_name);
        imageNavProfile = (ImageView) findViewById(R.id.image_nav_profile);
        tabMain = (TabLayout) findViewById(R.id.tab_main);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        fragmentAdapater = new FragmentAdapater(getSupportFragmentManager(), MainActivity.this);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        user = preferences.getPreferences(MainActivity.this, "user");
        linearNavProfile = (LinearLayout) findViewById(R.id.linear_nav_profile);
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
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_menu_revert)
                    .setTitle("로그아웃")
                    .setMessage("로그아웃 하시겠습니까?")
                    .setPositiveButton("아니", null)
                    .setNegativeButton("응", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.remove("user");
                            editor.commit();
                            finish();
                        }
                    })
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class UserInfoTask extends AsyncTask<String, String, String> {
        MemberDTO mdto = new MemberDTO();

        @Override
        protected String doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/user_info.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            try {
                HttpPost post = new HttpPost(URL + "?email=" + params[0]);
                HttpResponse response = client.execute(post);

                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
                line = null;
                result = "";

                while ((line = bufreader.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                int start = result.indexOf("{\"rows\"");
                int end = result.indexOf("jsonend");
                String jsonValue = result.substring(start, end);
                JSONObject json = new JSONObject(jsonValue);
                JSONArray jArr = json.getJSONArray("rows");

                if (json != null) {
                    for (int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);
                        mdto.setEmail(json.getString("email"));
                        mdto.setName(json.getString("name"));
                        mdto.setProfile(json.getString("profile"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mdto.getEmail() != null) {
                textNavEmail.setText(mdto.getEmail());
                textNavName.setText(mdto.getName());
                String profile = "";
                if (mdto.getProfile().equals("empty.jpg")) {
                    profile = "empty.jpg";
                } else {
                    profile = user + "_profile.jpg";
                }
                Glide.with(MainActivity.this)
                        .load(MainApplication.SERVER_URL + "/LocalServer/upload/" + profile)
                        .bitmapTransform(new CropCircleTransformation(new BitmapPool() {
                            @Override
                            public int getMaxSize() {
                                return 0;
                            }

                            @Override
                            public void setSizeMultiplier(float sizeMultiplier) {

                            }

                            @Override
                            public boolean put(Bitmap bitmap) {
                                return false;
                            }

                            @Override
                            public Bitmap get(int width, int height, Bitmap.Config config) {
                                return null;
                            }

                            @Override
                            public Bitmap getDirty(int width, int height, Bitmap.Config config) {
                                return null;
                            }

                            @Override
                            public void clearMemory() {

                            }

                            @Override
                            public void trimMemory(int level) {

                            }
                        }))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(imageNavProfile);
            }
        }
    }
}
