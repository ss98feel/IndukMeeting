package induk.soft.meeting.indukmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import induk.soft.meeting.indukmeeting.R;

/**
 * Created by Hong on 2016-07-15.
 */
public class RefreshActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);
        startActivity(new Intent(RefreshActivity.this, MainActivity.class));
        finish();
    }
}
