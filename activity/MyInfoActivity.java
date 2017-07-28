package induk.soft.meeting.indukmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import induk.soft.meeting.indukmeeting.R;
import induk.soft.meeting.indukmeeting.Utils.BitmapRound;
import induk.soft.meeting.indukmeeting.Utils.MainApplication;
import induk.soft.meeting.indukmeeting.Utils.Permissions;
import induk.soft.meeting.indukmeeting.Utils.Preferences;
import induk.soft.meeting.indukmeeting.model.MemberDTO;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Hong on 2016-07-15.
 */
public class MyInfoActivity extends AppCompatActivity {

    private static final String TEMP_PHOTO_FILE = "temp.jpg";       // 임시 저장파일
    private static final int REQ_CODE_PICK_IMAGE = 0;
    private TextView textMyinfoEmail, textMyinfoName;
    private ImageView imageMyinfoProfile;
    private LinearLayout linearMyinfo;
    private String user, line, result;
    private Preferences preferences = new Preferences();

    UserInfoTask userInfoTask;
    ProfileUpdateTask profileUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        init();

        userInfoTask = new UserInfoTask();
        userInfoTask.execute(user);

        Permissions.verifyStoragePermissions(this);
        linearMyinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");              // 모든 이미지
                intent.putExtra("crop", "true");        // Crop기능 활성화
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
            }
        });
    }

    private void init() {
        textMyinfoEmail = (TextView) findViewById(R.id.text_myinfo_email);
        textMyinfoName = (TextView) findViewById(R.id.text_myinfo_name);
        imageMyinfoProfile = (ImageView) findViewById(R.id.image_myinfo_profile);
        linearMyinfo = (LinearLayout) findViewById(R.id.linear_myinfo);
        user = preferences.getPreferences(MyInfoActivity.this, "user");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MyInfoActivity.this, MainActivity.class));
        finish();
    }

    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                    TEMP_PHOTO_FILE);
            if(f.exists()) {
                Log.i("File : ", f.getName());
                f.delete();
            }
            try {
                f.createNewFile();      // 외장메모리에 temp.jpg 파일 생성
            } catch (IOException e) {

            }
            return f;
        } else
            return null;
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_PICK_IMAGE) {
            if(resultCode == Activity.RESULT_OK) {
                 String filePath = Environment.getExternalStorageDirectory() + "/temp.jpg";

                Bitmap selectedImage = BitmapFactory.decodeFile(filePath); // temp.jpg파일을 Bitmap으로 디코딩한다.
                imageMyinfoProfile.setImageBitmap(BitmapRound.getRoundedBitmap(selectedImage));

                profileUpdateTask = new ProfileUpdateTask(filePath);
                profileUpdateTask.execute(textMyinfoEmail.getText().toString());
            }
        }
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
                textMyinfoEmail.setText(mdto.getEmail());
                textMyinfoName.setText(mdto.getName());
                String profile = "";
                if (mdto.getProfile().equals("empty.jpg")) {
                    profile = "empty.jpg";
                } else {
                    profile = user + "_profile.jpg";
                }
                Glide.with(MyInfoActivity.this)
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
                        .into(imageMyinfoProfile);
            }
        }
    }

    public class ProfileUpdateTask extends AsyncTask<String, String, String> {
        MemberDTO mdto = new MemberDTO();
        String filepath;
        String profilename;

        public ProfileUpdateTask(String filepath) {
            this.filepath = filepath;
        }

        @Override
        protected String doInBackground(String... params) {
            profilename = params[0] + "_profile";
            String URL = MainApplication.SERVER_URL + "LocalServer/user_profile_update.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            HttpPost post = new HttpPost(URL + "?email=" + params[0] + "&profilename=" + profilename);
            //헤더 설정
            post.setHeader("Accept-Charset", "UTF-8");
            post.setHeader("ENCTYPE", "multipart/form-data");

            FileBody bin = new FileBody(new File(filepath));

            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.setCharset(Charset.forName("UTF-8"));
            meb.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            meb.addPart("profile", bin);
            HttpEntity entity = meb.build();

            post.setEntity(entity);
            try {
                HttpResponse response = client.execute(post);
                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
                line = null;
                result = "";

                while ((line = bufreader.readLine()) != null) {
                    result += line;
                }
                Log.i("result : ",result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MyInfoActivity.this, "이미지가 업로드 되었습니다.", Toast.LENGTH_SHORT).show();

            Glide.with(MyInfoActivity.this)
                    .load(MainApplication.SERVER_URL + "/LocalServer/upload/" + user + "_profile.jpg")
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
                    .into(imageMyinfoProfile);

            File f = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE);
            if(f.exists()) {
                f.delete();
            }
        }
    }
}
