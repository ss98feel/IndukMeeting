package induk.soft.meeting.indukmeeting.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by SIK on 2016-07-17.
 */
public class Preferences {
    public void savePreferences(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public String getPreferences(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        return pref.getString(key, "");
    }
}
