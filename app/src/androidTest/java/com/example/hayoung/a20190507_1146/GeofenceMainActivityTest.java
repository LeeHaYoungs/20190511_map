package com.example.hayoung.a20190507_1146;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GeofenceMainActivityTest {

    @Rule
    public ActivityTestRule<GeofenceMainActivity> mActivityTestRule = new ActivityTestRule<>(GeofenceMainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void geofenceMainActivityTest() {
    }
}
