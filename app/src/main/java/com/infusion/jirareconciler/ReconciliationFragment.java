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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationFragment extends Fragment {
    public static final String EXTRA_RECONCILIATION_ID = "com.infusion.jirareconciler.reconciliation_id";
    private Reconciliation reconciliation;
    private JiraHelper jiraHelper;

    @InjectView(R.id.reconciliation_board_text_view) TextView boardTextView;
    @InjectView(R.id.reconciliation_date_text_view) TextView dateTextView;
    @InjectView(R.id.reconciliation_issues_list_view) ListView issuesListView;

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
        ButterKnife.inject(this, view);

        if (NavUtils.getParentActivityName(getActivity()) != null) {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        boardTextView.setText(reconciliation.getBoard());
        dateTextView.setText(reconciliation.getDate().toString());

        issuesListView.setAdapter(new IssueAdapter(reconciliation.getIssues()));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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

    @OnItemClick(R.id.reconciliation_issues_list_view)
    public void navigateToIssue(int position) {
        Issue issue = (Issue) issuesListView.getAdapter().getItem(position);
        Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jiraHelper.getIssueUrl(issue.getId())));
        startActivity(browseIntent);
    }

    class IssueAdapter extends ArrayAdapter<Issue> {
        public IssueAdapter(List<Issue> issues) {
            super(getActivity(), 0, issues);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Issue issue = reconciliation.getIssues().get(position);
            Issue previousIssue = position == 0 ? null : reconciliation.getIssues().get(position - 1);
            boolean isGroupStart = position == 0 ||
                    (issue.getBoardState() != previousIssue.getBoardState()) &&
                            (issue.getBoardState() != null && !issue.getBoardState().equals(previousIssue.getBoardState()) ||
                                    previousIssue.getBoardState() != null && !previousIssue.getBoardState().equals(issue.getBoardState()));

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_issue, null);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.setIssue(issue, isGroupStart);

            return convertView;
        }

        class ViewHolder {
            @InjectView(R.id.issue_list_item_id) TextView issueIdTextView;
            @InjectView(R.id.issue_list_item_title) TextView titleTextView;
            @InjectView(R.id.issue_list_item_board_state) TextView boardStateTextView;
            @InjectView(R.id.issue_list_item_jira_state) TextView jiraStateTextView;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }

            public void setIssue(Issue issue, boolean isGroupStart) {
                issueIdTextView.setText(issue.getId());
                titleTextView.setText(issue.getTitle());
                boardStateTextView.setText(issue.getBoardState());
                jiraStateTextView.setText(issue.getJiraState());

                boardStateTextView.setVisibility(isGroupStart ? View.VISIBLE : View.GONE);
            }
        }
    }
}
