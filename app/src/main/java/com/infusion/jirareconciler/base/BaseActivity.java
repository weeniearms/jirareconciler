package com.infusion.jirareconciler.base;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by rcieslak on 24/04/2015.
 */
public class BaseActivity extends ActionBarActivity {
    private ObjectGraph activityGraph;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App application = (App) getApplication();
        activityGraph = application.getApplicationGraph().plus(getModules().toArray());
        activityGraph.inject(this);
    }

    @Override protected void onDestroy() {
        activityGraph = null;
        super.onDestroy();
    }
    protected List<Object> getModules() {
        return Arrays.asList();
    }

    public void inject(Object object) {
        activityGraph.inject(object);
    }
}
