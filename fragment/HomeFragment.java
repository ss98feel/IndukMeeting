package induk.soft.meeting.indukmeeting.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import induk.soft.meeting.indukmeeting.activity.MainActivity;
import induk.soft.meeting.indukmeeting.activity.WriteActivity;
import induk.soft.meeting.indukmeeting.adapter.ListHomeAdapter;
import induk.soft.meeting.indukmeeting.model.TimeLineDTO;

/**
 * Created by Hong on 2016-07-13.
 */
public class HomeFragment extends Fragment {
    private ListView listHome;
    private ListHomeAdapter listHomeAdapter;
    private FloatingActionButton fabWrite;
    private TextView textHomeRefresh;

    TimeLineReadTask timeLineReadTask;
    private String line, result;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        listHomeAdapter = new ListHomeAdapter();
        listHome = (ListView) view.findViewById(R.id.list_home);
        timeLineReadTask = new TimeLineReadTask();
        timeLineReadTask.execute();

        textHomeRefresh = (TextView) view.findViewById(R.id.text_home_refresh);
        textHomeRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
            }
        });

        fabWrite = (FloatingActionButton) view.findViewById(R.id.fbtn_write);
        fabWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), WriteActivity.class));
                getActivity().finish();
            }
        });
        return view;
    }

    public class TimeLineReadTask extends AsyncTask<String, Boolean, Boolean> {
        TimeLineDTO tdto = new TimeLineDTO();

        @Override
        protected Boolean doInBackground(String... params) {
            String URL = MainApplication.SERVER_URL + "LocalServer/timeline_read.jsp"; //자신의 웹서버 주소를 저장합니다.
            DefaultHttpClient client = new DefaultHttpClient();//HttpClient 통신을 합니다.
            try {
                HttpPost post = new HttpPost(URL);
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
                        tdto.setIdx(json.getString("idx"));
                        tdto.setEmail(json.getString("email"));
                        tdto.setName(json.getString("name"));
                        tdto.setDate(json.getString("date"));
                        tdto.setContents(json.getString("contents"));
                        listHomeAdapter.addItem(tdto.getIdx(), tdto.getEmail(), tdto.getName(), tdto.getDate(), tdto.getContents());
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Boolean... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            if(s == true) {
                listHomeAdapter.notifyDataSetChanged();
                listHome.setAdapter(listHomeAdapter);
            } else {
                Toast.makeText(getActivity(), "실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
