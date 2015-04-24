package com.infusion.jirareconciler;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.infusion.jirareconciler.base.App;
import com.infusion.jirareconciler.jira.JiraHelper;
import com.infusion.jirareconciler.model.ReconciliationJSONSerializer;
import com.infusion.jirareconciler.model.ReconciliationStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rcieslak on 24/04/2015.
 */
@Module(
        library = true,
        injects = {
                BoardSelectorFragment.class, CaptureFragment.class, CaptureActivity.class,
                ReconciliationActivity.class, ReconciliationFragment.class,
                ReconciliationListActivity.class, ReconciliationListFragment.class,
                SettingsActivity.class, SettingsFragment.class
        })
public class AppModule {
    private static final String RECONCILIATIONS_FILENAME = "reconciliations.json";
    private final App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton ReconciliationStore provideReconciliationStore() {
        return new ReconciliationStore(new ReconciliationJSONSerializer(app, RECONCILIATIONS_FILENAME));
    }

    @Provides @Singleton JiraHelper provideJiraHelper() {
        return new JiraHelper(app);
    }

    @Provides @Singleton SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
