package induk.soft.meeting.indukmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import induk.soft.meeting.indukmeeting.R;
import induk.soft.meeting.indukmeeting.Utils.MainApplication;
import induk.soft.meeting.indukmeeting.Utils.Preferences;
import induk.soft.meeting.indukmeeting.model.MemberDTO;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Hong on 2016-07-13.
 */
public class WriteActivity extends Activity {

    private ImageView imageWriteBack, imageWriteCommit, imageWriteProfile, imageWriteUpload;
    private TextView textWriteName, textWriteType;
    private EditText editWriteContent;
    private String email, line, result;
    private Preferences preferences = new Preferences();

    UserInfoTask userInfoTask;
    TimeLineWriteTask timeLineWriteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        init();

        userInfoTask.execute(email);

        imageWriteBack.setOnClickListener(listener);
        imageWriteCommit.setOnClickListener(listener);
        imageWriteUpload.setOnClickListener(listener);
        textWriteType.setOnClickListener(listener);
    }

    private void init() {
        imageWriteBack = (ImageView) findViewById(R.id.image_write_back);
        imageWriteCommit = (ImageView) findViewById(R.id.image_write_commit);
        imageWriteProfile = (ImageView) findViewById(R.id.image_write_profile);
        imageWriteUpload = (ImageView) findViewById(R.id.image_write_upload);
        textWriteName = (TextView) findViewById(R.id.text_write_name);
        textWriteType = (TextView) findViewById(R.id.text_write_type);
        editWriteContent = (EditText) findViewById(R.id.edit_write_content);
        userInfoTask = new UserInfoTask();
        email = preferences.getPreferences(WriteActivity.this, "user");
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_write_back:
                    startActivity(new Intent(WriteActivity.this, MainActivity.class));
                    finish();
                    break;
                case R.id.image_write_commit:
                    SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy,MM,dd,HH,mm,ss", Locale.KOREA );
                    Date currentTime = new Date ();
                    String dTime = formatter.format (currentTime);
                    timeLineWriteTask = new TimeLineWriteTask();
                    timeLineWriteTask.execute(email, textWriteName.getText().toString(), dTime, editWriteContent.getText().toString());
                    break;
                case R.id.text_write_type:
                    if(textWriteType.getText().equals("실명"))
                        textWriteType.setText("익명");
                    else
                        textWriteType.setText("실명");
                    break;
                case R.id.image_write_upload:
                    break;
            }
        }
    };

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
                textWriteName.setText(mdto.getName());
                String profile = "";
                if (mdto.getProfile().equals("empty.jpg")) {
                    profile = "empty.jpg";
                } else {
                    profile = email + "_profile.jpg";
                }
                Glide.with(WriteActivity.this)
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
                        .into(imageWriteProfile);
            }
        }
    }

    public class TimeLineWriteTask extends AsyncTask<String, Boolean, Boolean> {
        MemberDTO mdto = new MemberDTO();

        @Override
        protected Boolean doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/timeline_write.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            try {
                HttpPost post = new HttpPost(URL + "?email=" + params[0] + "&name=" + params[1] + "&writedate=" + params[2] + "&contents=" + params[3]);
                HttpResponse response = client.execute(post);

                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
                line = null;
                result = "";

                while ((line = bufreader.readLine()) != null) {
                    result += line;
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if(s == true) {
                startActivity(new Intent(WriteActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(WriteActivity.this, "등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(WriteActivity.this, MainActivity.class));
        finish();
    }
}
