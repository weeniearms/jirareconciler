package com.infusion.jirareconciler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.infusion.jirareconciler.base.App;
import com.infusion.jirareconciler.model.ReconciliationJSONSerializer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rcieslak on 24/04/2015.
 */
@Module(
        injects = {
                BoardSelectorFragment.class, CaptureFragment.class, CaptureActivity.class,
                ReconciliationActivity.class, ReconciliationFragment.class,
                ReconciliationListActivity.class, ReconciliationListFragment.class,
                SettingsActivity.class, SettingsFragment.class, LaneSelectorFragment.class
        })
public class AppModule {
    private static final String RECONCILIATIONS_FILENAME = "reconciliations.json";
    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton Context providesContext() {
        return app;
    }

    @Provides @Singleton ReconciliationJSONSerializer provideReconciliationJSONSerializer() {
        return new ReconciliationJSONSerializer(app, RECONCILIATIONS_FILENAME);
    }

    @Provides @Singleton SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
