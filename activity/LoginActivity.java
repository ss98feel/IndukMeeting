package induk.soft.meeting.indukmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import induk.soft.meeting.indukmeeting.model.MemberDTO;

/**
 * Created by SIK on 2016-06-29.
 */
public class LoginActivity extends Activity {

    private ImageView imageLoginBack;
    private EditText editLoginEmail, editLoginPassword;
    private TextView textJoin, textLogin;

    private String line, result;

    private Preferences preferences = new Preferences();

    LogintTask logintTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        textLogin.setOnClickListener(listener);
        textJoin.setOnClickListener(listener);
    }

    private void init() {
        imageLoginBack = (ImageView) findViewById(R.id.image_login_back);
        editLoginEmail = (EditText) findViewById(R.id.edit_login_email);
        editLoginPassword = (EditText) findViewById(R.id.edit_login_password);
        textLogin = (TextView) findViewById(R.id.text_login);
        textJoin = (TextView) findViewById(R.id.text_join_page);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LoginActivity.this, IntroActivity.class));
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
        finish();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.text_join_page:
                    startActivity(new Intent(LoginActivity.this, JoinActivity.class));
                    overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                    finish();
                    break;
                case R.id.text_login:
                    String email = editLoginEmail.getText().toString();
                    String password = editLoginPassword.getText().toString();
                    logintTask = new LogintTask();
                    logintTask.execute(email, password);
                    break;
            }
        }
    };

    public class LogintTask extends AsyncTask<String, String, String> {
        MemberDTO mdto = new MemberDTO();
        @Override
        protected String doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/login.jsp";
            DefaultHttpClient client = new DefaultHttpClient();
            try {
                HttpPost post = new HttpPost(URL + "?email=" + params[0] + "&password=" + params[1]);
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

                if(json != null) {
                    for(int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);
                        mdto.setEmail(json.getString("email"));
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
            if(mdto.getEmail() != null) {
                Toast.makeText(LoginActivity.this, "로그인에 성공했습니다!", Toast.LENGTH_SHORT).show();
                preferences.savePreferences(LoginActivity.this, "user", mdto.getEmail());
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "로그인 정보를 확인해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
