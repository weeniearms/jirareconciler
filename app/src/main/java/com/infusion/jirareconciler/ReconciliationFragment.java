package com.infusion.jirareconciler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.infusion.jirareconciler.jira.JiraHelper;
import com.infusion.jirareconciler.model.Issue;
import com.infusion.jirareconciler.model.Reconciliation;
import com.infusion.jirareconciler.model.ReconciliationStore;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationFragment extends Fragment {
    public static final String EXTRA_RECONCILIATION_ID = "com.infusion.jirareconciler.reconciliation_id";
    private Reconciliation reconciliation;
    private TextView sprintTextView;
    private TextView dateTextView;
    private ListView issuesListView;
    private JiraHelper jiraHelper;

    public static ReconciliationFragment newInstance(UUID id) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_RECONCILIATION_ID, id);

        ReconciliationFragment reconciliationFragment = new ReconciliationFragment();
        reconciliationFragment.setArguments(args);

        return reconciliationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        jiraHelper = new JiraHelper(getActivity());

        UUID reconciliationId = (UUID) getArguments().getSerializable(EXTRA_RECONCILIATION_ID);
        reconciliation = ReconciliationStore.get(getActivity()).getReconciliation(reconciliationId);
        Collections.sort(reconciliation.getIssues(), new Comparator<Issue>() {
            @Override
            public int compare(Issue lhs, Issue rhs) {
                if (lhs.getBoardState() == null && rhs.getBoardState() == null) {
                    return 0;
                }

                if (lhs.getBoardState() == null) {
                    return -1;
                }

                if (rhs.getBoardState() == null) {
                    return 1;
                }

                return lhs.getBoardState().compareTo(rhs.getBoardState());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reconciliation, container, false);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sprintTextView = (TextView) view.findViewById(R.id.reconciliation_sprint_text_view);
        sprintTextView.setText(reconciliation.getBoard());

        dateTextView = (TextView) view.findViewById(R.id.reconciliation_date_text_view);
        dateTextView.setText(reconciliation.getDate().toString());

        issuesListView = (ListView) view.findViewById(R.id.reconciliation_issues_list_view);
        issuesListView.setAdapter(new IssueAdapter(reconciliation.getIssues()));
        issuesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Issue issue = (Issue) issuesListView.getAdapter().getItem(position);
                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jiraHelper.getIssueUrl(issue.getId())));
                startActivity(browseIntent);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_reconciliation, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_delete_item) {
            ReconciliationStore reconciliationStore = ReconciliationStore.get(getActivity());
            reconciliationStore.deleteReconciliation(reconciliation);
            reconciliationStore.saveReconciliations();
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class IssueAdapter extends ArrayAdapter<Issue> {
        public IssueAdapter(List<Issue> issues) {
            super(getActivity(), 0, issues);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Issue issue = reconciliation.getIssues().get(position);
            Issue previousIssue = position == 0 ? null : reconciliation.getIssues().get(position - 1);

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_issue, null);
            }

            TextView idTextView = (TextView) convertView.findViewById(R.id.issue_list_item_id);
            idTextView.setText(issue.getId());

            TextView boardStateTextView = (TextView) convertView.findViewById(R.id.issue_list_item_board_state);
            boardStateTextView.setText(issue.getBoardState());

            TextView titleTextView = (TextView) convertView.findViewById(R.id.issue_list_item_title);
            titleTextView.setText(issue.getTitle());

            TextView jiraStateTextView = (TextView) convertView.findViewById(R.id.issue_list_item_jira_state);
            jiraStateTextView.setText(issue.getJiraState());

            if (position == 0 ||
                    (issue.getBoardState() != previousIssue.getBoardState()) &&
                            (issue.getBoardState() != null && !issue.getBoardState().equals(previousIssue.getBoardState()) ||
                                    previousIssue.getBoardState() != null && !previousIssue.getBoardState().equals(issue.getBoardState()))) {
                boardStateTextView.setVisibility(View.VISIBLE);
            }
            else {
                boardStateTextView.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
