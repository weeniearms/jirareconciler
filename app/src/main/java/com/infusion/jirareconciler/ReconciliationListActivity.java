package com.infusion.jirareconciler;

import android.support.v4.app.Fragment;

public class ReconciliationListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ReconciliationListFragment();
    }
}
