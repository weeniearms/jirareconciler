package com.infusion.jirareconciler.base;

import android.app.Application;

import com.infusion.jirareconciler.AppModule;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by rcieslak on 24/04/2015.
 */
public class App extends Application {
    private ObjectGraph applicationGraph;

    @Override public void onCreate() {
        super.onCreate();

        applicationGraph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {
        return Arrays.<Object>asList(new AppModule(this));
    }

    ObjectGraph getApplicationGraph() {
        return applicationGraph;
    }
}
