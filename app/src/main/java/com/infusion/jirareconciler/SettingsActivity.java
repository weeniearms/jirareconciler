package com.infusion.jirareconciler;

import android.support.v4.app.Fragment;

/**
 * Created by rcieslak on 20/04/2015.
 */
public class SettingsActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new SettingsFragment();
    }
}
