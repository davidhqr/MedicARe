package dev.medicare;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager viewPager = findViewById(R.id.main_viewPager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(1);
    }

    public static class PagerAdapter extends FragmentPagerAdapter {

        private Fragment statusFragment, arFragment, historyFragment;

        private PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    statusFragment = new StatusFragment();
                    return statusFragment;
                case 1:
                    arFragment = new ArFragment();
                    return arFragment;
                case 2:
                    historyFragment = new HistoryFragment();
                    return historyFragment;
                default:
                    return null;
            }
        }
    }
}
