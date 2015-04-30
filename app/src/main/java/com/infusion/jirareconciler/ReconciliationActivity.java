package com.infusion.jirareconciler;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.infusion.jirareconciler.base.BaseActivity;
import com.infusion.jirareconciler.cards.IssueCardsFileProvider;
import com.infusion.jirareconciler.cards.IssueCardsGenerator;
import com.infusion.jirareconciler.jira.JiraHelper;
import com.infusion.jirareconciler.model.Issue;
import com.infusion.jirareconciler.model.Reconciliation;
import com.infusion.jirareconciler.model.ReconciliationStore;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationActivity extends BaseActivity {
    public static final String EXTRA_RECONCILIATION_ID = "com.infusion.jirareconciler.reconciliation_id";
    private Reconciliation reconciliation;
    private ObjectAnimator fade;
    private View containerHeader;
    private TextView boardTextView;
    private TextView dateTextView;

    @Inject JiraHelper jiraHelper;
    @Inject IssueCardsGenerator generator;

    @Inject ReconciliationStore reconciliationStore;

    @InjectView(R.id.app_bar) Toolbar toolbar;
    @InjectView(R.id.reconciliation_issues_list_view) ListView issuesListView;
    @InjectView(R.id.reconciliation_export) ImageButton exportIssuesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconciliation);

        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        UUID reconciliationId = (UUID) getIntent().getSerializableExtra(EXTRA_RECONCILIATION_ID);
        reconciliation = reconciliationStore.getReconciliation(reconciliationId);

        if (NavUtils.getParentActivityName(this) != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View headerView = LayoutInflater.from(this).inflate(R.layout.list_header, issuesListView, false);
        containerHeader = headerView.findViewById(R.id.header_container);
        boardTextView = (TextView) headerView.findViewById(R.id.header_title);
        dateTextView = (TextView) headerView.findViewById(R.id.header_description);

        fade = ObjectAnimator.ofFloat(containerHeader, "alpha", 0f, 1f);
        fade.setInterpolator(new DecelerateInterpolator());
        fade.setDuration(400);

        issuesListView.setAdapter(new IssueAdapter(reconciliation.getIssues()));
        issuesListView.addHeaderView(headerView);
        issuesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem != 0) {
                    exportIssuesButton.setTranslationY(0);
                }

                if (view != null && view.getChildCount() > 0 && firstVisibleItem == 0) {
                    int translation = view.getChildAt(0) == null ? 0 : view.getChildAt(0).getHeight() + view.getChildAt(0).getTop();
                    exportIssuesButton.setTranslationY(translation > 0 ? translation : 0);

                    if (view.getChildAt(0).getTop() < -dpToPx(16)) {
                        toggleHeader(false, false);
                    } else {
                        toggleHeader(true, true);
                    }
                } else {
                    toggleHeader(false, false);
                }

                if (getSupportActionBar() != null) {
                    if (firstVisibleItem == 0) {
                        getSupportActionBar().setElevation(0);
                    }
                    else {
                        getSupportActionBar().setElevation(dpToPx(4));
                    }
                }
            }
        });

        setTitle(reconciliation.getBoard());
        boardTextView.setText(reconciliation.getBoard());
        dateTextView.setText(reconciliation.getDate().toString());

        exportIssuesButton.setVisibility(View.INVISIBLE);
        for (Issue issue : reconciliation.getIssues()) {
            if (issue.getBoardState() == null || issue.getBoardState().equals("")) {
                exportIssuesButton.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_reconciliation, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            case R.id.menu_item_delete_item:
                reconciliationStore.deleteReconciliation(reconciliation);
                reconciliationStore.saveReconciliations();
                setResult(Activity.RESULT_OK);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnItemClick(R.id.reconciliation_issues_list_view)
    public void navigateToIssue(int position) {
        Issue issue = reconciliation.getIssues().get(position);
        Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jiraHelper.getIssueUrl(issue.getId())));
        startActivity(browseIntent);
    }
    @OnClick(R.id.reconciliation_export)
    public void exportIssues() {
        try {
            String issuesFile = "issues.pdf";
            generator.generateCards(reconciliation.getIssues(), issuesFile);

            Intent exportIntent = new Intent(Intent.ACTION_VIEW);
            exportIntent.setDataAndType(
                    Uri.parse("content://" + IssueCardsFileProvider.AUTHORITY + "/" + issuesFile),
                    "application/pdf");
            startActivity(exportIntent);
        } catch (IOException | DocumentException e) {
            Toast.makeText(this, "Error occured while generating cards", Toast.LENGTH_SHORT).show();
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int)(dp * (displayMetrics.densityDpi / 160f));
    }

    private void toggleHeader(boolean visible, boolean force) {
        if ((force && visible) || (visible && containerHeader.getAlpha() == 0f)) {
            fade.setFloatValues(containerHeader.getAlpha(), 1f);
            fade.start();
        }
        else if (force || (!visible && containerHeader.getAlpha() == 1f)){
            fade.setFloatValues(containerHeader.getAlpha(), 0f);
            fade.start();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(!visible);
        }
    }

    class IssueAdapter extends ArrayAdapter<Issue> {
        public IssueAdapter(List<Issue> issues) {
            super(ReconciliationActivity.this, 0, issues);

            Collections.sort(issues, new Comparator<Issue>() {
                @Override
                public int compare(Issue lhs, Issue rhs) {
                    int result = compareState(lhs.getBoardState(), rhs.getBoardState());

                    if (result == 0) {
                        result = compareState(lhs.getJiraState(), rhs.getJiraState());
                    }

                    return result;
                }

                private int compareState(String lhs, String rhs) {
                    if (lhs == null && rhs == null) return 0;
                    if (lhs == null) return -1;
                    if (rhs == null) return 1;

                    return lhs.compareTo(rhs);
                }
            });
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ReconciliationActivity.this.getLayoutInflater().inflate(R.layout.list_item_issue, null);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            Issue issue = reconciliation.getIssues().get(position);
            Issue previousIssue = position == 0 ? null : reconciliation.getIssues().get(position - 1);
            boolean isGroupStart = position == 0 ||
                    (issue.getBoardState() != previousIssue.getBoardState()) &&
                            (issue.getBoardState() != null && !issue.getBoardState().equals(previousIssue.getBoardState()) ||
                                    previousIssue.getBoardState() != null && !previousIssue.getBoardState().equals(issue.getBoardState()));
            holder.setIssue(issue, isGroupStart);

            return convertView;
        }

        class ViewHolder {
            @InjectView(R.id.issue_list_item_id) TextView issueIdTextView;
            @InjectView(R.id.issue_list_item_title) TextView titleTextView;
            @InjectView(R.id.issue_list_item_board_state) TextView boardStateTextView;
            @InjectView(R.id.issue_list_item_jira_state) TextView jiraStateTextView;
            @InjectView(R.id.issue_list_item_header) View headerContainer;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }

            public void setIssue(Issue issue, boolean isGroupStart) {
                issueIdTextView.setText(issue.getId());
                titleTextView.setText(issue.getTitle());
                boardStateTextView.setText(
                        issue.getBoardState() == null || issue.getBoardState().equals("") ?
                                getString(R.string.missing) :
                                issue.getBoardState());
                jiraStateTextView.setText(issue.getJiraState());

                headerContainer.setVisibility(isGroupStart ? View.VISIBLE : View.GONE);
            }
        }
    }
}
