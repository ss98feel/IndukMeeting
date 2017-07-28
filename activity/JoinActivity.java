package induk.soft.meeting.indukmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import induk.soft.meeting.indukmeeting.R;
import induk.soft.meeting.indukmeeting.Utils.MainApplication;
import induk.soft.meeting.indukmeeting.Utils.Preferences;
import induk.soft.meeting.indukmeeting.model.MemberDTO;

/**
 * Created by SIK on 2016-06-29.
 */
public class JoinActivity extends Activity {

    private EditText editJoinName, editJoinEmail, editJoinPassword, editJoinPasswordCheck;
    private TextView textJoin;

    private String line, result;

    private Preferences preferences = new Preferences();

    InsertTask insertTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        init();

        textJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String name = editJoinName.getText().toString();
                String email = editJoinEmail.getText().toString();
                String password = editJoinPassword.getText().toString();
                String passwordCheck = editJoinPasswordCheck.getText().toString();

                if(!nameValidation(name)) {
                    Toast.makeText(JoinActivity.this, "이름은 한글과 영문만 입력해주세요(2-8자)", Toast.LENGTH_SHORT).show();
                } else if(!emailValidation(email)) {
                    Toast.makeText(JoinActivity.this, "이메일 형식에 맞지 않습니다", Toast.LENGTH_SHORT).show();
                } else if(!passwordValidation(password)) {
                    Toast.makeText(JoinActivity.this, "비밀번호는 4-12자로 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if(!password.equals(passwordCheck)) {
                    Toast.makeText(JoinActivity.this, "비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
                } else {
                    insertTask = new InsertTask();
                    insertTask.execute(name, email, password);
                }
            }
        });
    }

    private void init() {
        editJoinName = (EditText) findViewById(R.id.edit_join_name);
        editJoinEmail = (EditText) findViewById(R.id.edit_join_email);
        editJoinPassword = (EditText) findViewById(R.id.edit_join_password);
        editJoinPasswordCheck = (EditText) findViewById(R.id.edit_join_password_check);
        textJoin = (TextView) findViewById(R.id.text_join);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(JoinActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
        finish();
    }

    private boolean nameValidation(String name) {
        return Pattern.matches("^[a-zA-Z가-흐].{1,8}+$", name);
    }

    private boolean emailValidation(String email) {
        return Pattern.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$", email);
    }

    private boolean passwordValidation(String password) {
        return Pattern.matches("^[0-9].{3,11}+$", password);
    }

    public class InsertTask extends AsyncTask<String, String, String> {
        String jsonValue = "";
        JSONObject json;
        JSONArray jArr;
        String email = "";
        MemberDTO mdto = new MemberDTO();
        @Override
        protected String doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/join.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            try {
                email = params[1];
                HttpPost post = new HttpPost(URL + "?name=" + params[0] + "&email=" + params[1] + "&password=" + params[2]);
                HttpResponse response = client.execute(post);

                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
                line = null;
                result = "";

                while ((line = bufreader.readLine()) != null) {
                    result += line;
                }

                Log.i("result : ", result);
                int start = result.indexOf("{\"rows\"");
                int end = result.indexOf("jsonend");
                jsonValue = result.substring(start, end);
                json = new JSONObject(jsonValue);
                jArr = json.getJSONArray("rows");

                if(json != null) {
                    for(int i = 0; i < jArr.length(); i++) {
                        json = jArr.getJSONObject(i);
                        mdto.setEmail(json.getString("email"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(mdto.getEmail() != null) {
                Toast.makeText(JoinActivity.this, "이미 가입된 이메일입니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(JoinActivity.this, "회원가입이 완료 되었습니다!", Toast.LENGTH_SHORT).show();
                preferences.savePreferences(JoinActivity.this, "user", email);
                startActivity(new Intent(JoinActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                finish();
            }
        }
    }
}
