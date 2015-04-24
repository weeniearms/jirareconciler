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

/**
 * Created by rcieslak on 20/04/2015.
 */
public class SettingsFragment extends Fragment {
    public static final String PREF_JIRA_URL = "jira_url";
    public static final String PREF_JIRA_USER = "jira_user";
    public static final String PREF_JIRA_PASSWORD = "jira_password";

    @InjectView(R.id.setting_url) EditText urlEditText;
    @InjectView(R.id.setting_user)  EditText userEditText;
    @InjectView(R.id.setting_password)  EditText passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.inject(this, view);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        urlEditText.setText(preferences.getString(PREF_JIRA_URL, null));
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                preferences.edit().putString(PREF_JIRA_URL, s.toString()).commit();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        userEditText.setText(preferences.getString(PREF_JIRA_USER, null));
        userEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                preferences.edit().putString(PREF_JIRA_USER, s.toString()).commit();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        passwordEditText.setText(preferences.getString(PREF_JIRA_PASSWORD, null));
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                preferences.edit().putString(PREF_JIRA_PASSWORD, s.toString()).commit();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

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
}
