package induk.soft.meeting.indukmeeting.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.util.ArrayList;

import induk.soft.meeting.indukmeeting.R;
import induk.soft.meeting.indukmeeting.Utils.MainApplication;
import induk.soft.meeting.indukmeeting.Utils.Preferences;
import induk.soft.meeting.indukmeeting.activity.MainActivity;
import induk.soft.meeting.indukmeeting.model.MemberDTO;
import induk.soft.meeting.indukmeeting.model.TimeLineDTO;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by SIK on 2016-07-09.
 */
public class ListHomeAdapter extends BaseAdapter {

    private ArrayList<TimeLineDTO> listHome = new ArrayList<TimeLineDTO>();
    private String profile;

    @Override
    public int getCount() {
        return listHome.size();
    }

    @Override
    public Object getItem(int position) {
        return listHome.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        final TimeLineDTO item = listHome.get(position);
        Preferences preferences = new Preferences();
        String user = preferences.getPreferences(context, "user");

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_timeline, parent, false);
        }

        ImageView imageProfile = (ImageView) convertView.findViewById(R.id.image_timeline_profile);
        TextView textName = (TextView) convertView.findViewById(R.id.text_timeline_name);
        TextView textDate = (TextView) convertView.findViewById(R.id.text_timeline_date);
        TextView textContent = (TextView) convertView.findViewById(R.id.text_timeline_content);
        final TextView textDelete = (TextView) convertView.findViewById(R.id.text_timeline_delete);
        TextView textReply = (TextView) convertView.findViewById(R.id.text_timeline_reply);

        UserInfoTask userInfoTask = new UserInfoTask(context, imageProfile);
        userInfoTask.execute(item.getEmail());

        String time[] = item.getDate().split(",");
        String half = "";
        String date = "";
        if (Integer.parseInt(time[1]) < 10) {
            time[1] = String.valueOf(time[1].charAt(1));
        }

        if (Integer.parseInt(time[3]) > 12) {
            half = "오후";
            time[3] = String.valueOf(Integer.parseInt(time[3]) - 12);
        } else {
            half = "오전";
        }
        date = time[1] + "월 " + time[2] + "일 " + half + " " + time[3] + "시 " + time[4] + "분";

        textName.setText(item.getName());
        textContent.setText(item.getContents());
        textDate.setText(date);

        if (user.equals(item.getEmail())) {
            textDelete.setText("삭제");
        } else {
            textDelete.setText("");
        }

        textDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textDelete.getText().equals("삭제")) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("삭제")
                            .setMessage("삭제 하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ItemDeleteTask itemDeleteTask = new ItemDeleteTask(context);
                                    itemDeleteTask.execute(item.getIdx());
                                    context.startActivity(new Intent(context, MainActivity.class));
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();
                }
            }
        });

        return convertView;
    }

    public void addItem(String idx, String email, String name, String date, String contents) {
        TimeLineDTO tdto = new TimeLineDTO();
        tdto.setIdx(idx);
        tdto.setEmail(email);
        tdto.setName(name);
        tdto.setDate(date);
        tdto.setContents(contents);
        listHome.add(tdto);
    }

    public class UserInfoTask extends AsyncTask<String, String, String> {
        MemberDTO mdto = new MemberDTO();
        Context context;
        ImageView imageProfile;

        public UserInfoTask(Context context, ImageView imageProfile) {
            this.context = context;
            this.imageProfile = imageProfile;
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/user_info.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            String line = null;
            String result = "";
            try {
                HttpPost post = new HttpPost(URL + "?email=" + params[0]);
                HttpResponse response = client.execute(post);

                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));

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
                if (mdto.getProfile().equals("empty.jpg")) {
                    profile = "empty.jpg";
                } else {
                    profile = mdto.getEmail() + "_profile.jpg";
                }

                Glide.with(context)
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
                        .into(imageProfile);
            }
        }
    }

    public class ItemDeleteTask extends AsyncTask<String, String, String> {
        Context context;

        public ItemDeleteTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/item_delete.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            String line = null;
            String result = "";
            try {
                HttpPost post = new HttpPost(URL + "?idx=" + params[0]);
                HttpResponse response = client.execute(post);

                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));

                while ((line = bufreader.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(context, "삭제 되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
