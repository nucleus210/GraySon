package com.example.root.grayson;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
                        RemoteControlFragment.OnRemoteControlListener,
                        BluetoothActivityFragment.OnFragmentInteractionListener,
                        ImageUploadFragment.OnFragmentInteractionListener,
                        FireBaseLoginFragment.OnLoginFragmentListener,
                        FireBaseStorageFragment.OnStorageFragmentListener {

    TabLayout mTabLayout;
    ViewPager mViewPager;
    private int[] tabIcons = {
            R.drawable.ic_bluetooth_black_24dp,
            R.drawable.ic_settings_remote_black_24dp,
            R.drawable.ic_cloud_queue_white_12dp,
            R.drawable.ic_file_upload_white_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = findViewById(R.id.my_pager);
        mTabLayout = findViewById(R.id.my_tab);
        mTabLayout.setupWithViewPager(mViewPager);
        SetUpViewPager(mViewPager);
        setupTabIcons();
    }

    public void SetUpViewPager(ViewPager viewPager) {
        GrayViewPagerAdapter adapter = new GrayViewPagerAdapter(getSupportFragmentManager());
        adapter.AddFragmentPage(new BluetoothActivityFragment(), "Blue");
        adapter.AddFragmentPage(new RemoteControlFragment(), "Remote");
        adapter.AddFragmentPage(new FireBaseLoginFragment(), "Login");
        adapter.AddFragmentPage(new FireBaseStorageFragment(), "Cloud");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onRemoteControlListener() {

    }

    @Override
    public void onBtFragmentListener() {

    }

    @Override
    public void onImageSendFragment(Uri uri) {

    }

    @Override
    public void onLoginFragmentListener(Uri uri) {

    }

    @Override
    public void onStorageFragmentListener(Uri uri) {

    }

    public class GrayViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragments = new ArrayList<>();
        private List<String> mTitleList = new ArrayList<>();

        GrayViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void AddFragmentPage(Fragment mFragment, String mTitle) {
            mFragments.add(mFragment);
            mTitleList.add(mTitle);

        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    private void setupTabIcons() {
        Objects.requireNonNull(mTabLayout.getTabAt(0)).setIcon(tabIcons[0]);
        Objects.requireNonNull(mTabLayout.getTabAt(1)).setIcon(tabIcons[1]);
        Objects.requireNonNull(mTabLayout.getTabAt(2)).setIcon(tabIcons[2]);
        Objects.requireNonNull(mTabLayout.getTabAt(3)).setIcon(tabIcons[3]);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }
}
