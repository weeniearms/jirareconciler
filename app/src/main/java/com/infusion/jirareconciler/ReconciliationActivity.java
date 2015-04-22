package com.infusion.jirareconciler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import java.util.List;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationActivity extends ActionBarActivity {
    private ViewPager viewPager;
    private List<Reconciliation> reconciliations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewPager = new ViewPager(this);
        viewPager.setId(R.id.view_pager);
        setContentView(viewPager);

        reconciliations = ReconciliationStore.get(this).getReconciliations();

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int i) {
                Reconciliation reconciliation = reconciliations.get(i);
                return ReconciliationFragment.newInstance(reconciliation.getId());
            }

            @Override
            public int getCount() {
                return reconciliations.size();
            }
        });

        for (int i = 0; i < reconciliations.size(); i++) {
            if(reconciliations.get(i).getId().equals(getIntent().getSerializableExtra(ReconciliationFragment.EXTRA_RECONCILIATION_ID))) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
