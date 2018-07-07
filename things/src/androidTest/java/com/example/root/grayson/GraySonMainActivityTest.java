package com.example.root.grayson;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GraySonMainActivityTest {

    @Rule
    public ActivityTestRule<GraySonMainActivity> mActivityTestRule = new ActivityTestRule<>(GraySonMainActivity.class);

    @Test
    public void graySonMainActivityTest() {
    }
}
