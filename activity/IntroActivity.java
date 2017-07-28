package induk.soft.meeting.indukmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import induk.soft.meeting.indukmeeting.R;
import induk.soft.meeting.indukmeeting.Utils.Preferences;

/**
 * Created by SIK on 2016-07-09.
 */
public class IntroActivity extends Activity {

    private LinearLayout linearKakaoLogin, linearNaverLogin, linearFacebookLogin, linearEmailLogin;
    private String user;
    private Preferences preferences = new Preferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        user = preferences.getPreferences(this, "user");
        if(!user.equals("")) {
            startActivity(new Intent(IntroActivity.this, MainActivity.class));
            finish();
        }
        init();

        linearKakaoLogin.setOnClickListener(listener);
        linearNaverLogin.setOnClickListener(listener);
        linearFacebookLogin.setOnClickListener(listener);
        linearEmailLogin.setOnClickListener(listener);
    }

    private void init() {
        linearKakaoLogin = (LinearLayout) findViewById(R.id.linear_kakao_login);
        linearNaverLogin = (LinearLayout) findViewById(R.id.linear_naver_login);
        linearFacebookLogin = (LinearLayout) findViewById(R.id.linear_facebook_login);
        linearEmailLogin = (LinearLayout) findViewById(R.id.linear_email_login);
    }

    LinearLayout.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.linear_kakao_login:
                    Toast.makeText(IntroActivity.this, "카카오로그인", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.linear_naver_login:
                    Toast.makeText(IntroActivity.this, "네이버로그인", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.linear_facebook_login:
                    Toast.makeText(IntroActivity.this, "페이스북로그인", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.linear_email_login:
                    startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                    finish();
                    break;
            }
        }
    };
}
