package com.infusion.jirareconciler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

/**
 * Created by rcieslak on 20/04/2015.
 */
public class SettingsFragment extends Fragment {
    public static final String PREF_JIRA_URL = "jira_url";
    public static final String PREF_JIRA_USER = "jira_user";
    public static final String PREF_JIRA_PASSWORD = "jira_password";
    private SharedPreferences preferences;

    @InjectView(R.id.setting_url) EditText urlEditText;
    @InjectView(R.id.setting_user)  EditText userEditText;
    @InjectView(R.id.setting_password)  EditText passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);

        urlEditText.setText(preferences.getString(PREF_JIRA_URL, null));
        userEditText.setText(preferences.getString(PREF_JIRA_USER, null));
        passwordEditText.setText(preferences.getString(PREF_JIRA_PASSWORD, null));

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (NavUtils.getParentActivityName(getActivity()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
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
