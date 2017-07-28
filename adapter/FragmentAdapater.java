package induk.soft.meeting.indukmeeting.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Hong on 2016-07-13.
 */
public class FragmentAdapater extends FragmentPagerAdapter {
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private Context context;

    public FragmentAdapater(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
    }
}
