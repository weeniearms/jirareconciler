package com.infusion.jirareconciler.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by rcieslak on 24/04/2015.
 */
public class BaseFragment extends Fragment {
    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity) getActivity()).inject(this);
    }
}
