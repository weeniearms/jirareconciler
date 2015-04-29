package com.infusion.jirareconciler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import com.infusion.jirareconciler.base.BaseActivity;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

/**
 * Created by rcieslak on 20/04/2015.
 */
public class SettingsActivity extends BaseActivity {
    public static final String PREF_JIRA_URL = "jira_url";
    public static final String PREF_JIRA_USER = "jira_user";
    public static final String PREF_JIRA_PASSWORD = "jira_password";

    @Inject
    SharedPreferences preferences;

    @InjectView(R.id.app_bar) Toolbar toolbar;
    @InjectView(R.id.setting_url) EditText urlEditText;
    @InjectView(R.id.setting_user)  EditText userEditText;
    @InjectView(R.id.setting_password)  EditText passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        urlEditText.setText(preferences.getString(PREF_JIRA_URL, null));
        userEditText.setText(preferences.getString(PREF_JIRA_USER, null));
        passwordEditText.setText(preferences.getString(PREF_JIRA_PASSWORD, null));

        ActionBar actionBar = getSupportActionBar();
        if (NavUtils.getParentActivityName(this) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.setting_url)
    public void urlChanged(CharSequence url) {
        preferences.edit().putString(PREF_JIRA_URL, url.toString()).commit();
    }

    @OnTextChanged(R.id.setting_user)
    public void userChanged(CharSequence user) {
        preferences.edit().putString(PREF_JIRA_USER, user.toString()).commit();
    }

    @OnTextChanged(R.id.setting_password)
    public void passwordChanged(CharSequence password) {
        preferences.edit().putString(PREF_JIRA_PASSWORD, password.toString()).commit();
    }
}
