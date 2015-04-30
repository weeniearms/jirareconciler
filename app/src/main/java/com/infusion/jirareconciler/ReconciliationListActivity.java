package com.infusion.jirareconciler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.infusion.jirareconciler.base.BaseActivity;
import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.jira.JiraHelper;
import com.infusion.jirareconciler.model.Reconciliation;
import com.infusion.jirareconciler.model.ReconciliationStore;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class ReconciliationListActivity extends BaseActivity {
    private static final int REQUEST_CAPTURE = 0;
    private static final int REQUEST_RECONCILIATION = 1;
    private static final String DIALOG_BOARD = "board";
    private static final String DIALOG_LANE = "lane";
    private List<Reconciliation> reconciliations;
    private ProgressDialog progressDialog;

    @Inject ReconciliationStore reconciliationStore;
    @Inject JiraHelper jiraHelper;

    @InjectView(R.id.reconciliation_list) ListView listView;
    @InjectView(R.id.app_bar) Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reconciliation_list);

        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.reconciliation_list_item_context, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_delete_item:
                        ReconciliationAdapter adapter = (ReconciliationAdapter) listView.getAdapter();

                        for (int i = adapter.getCount() - 1; i >= 0; i--) {
                            if (listView.isItemChecked(i)) {
                                reconciliationStore.deleteReconciliation(adapter.getItem(i));
                            }
                        }

                        mode.finish();
                        updateReconciliations();

                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) { }
        });

        updateReconciliations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_reconciliation_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_scan_ticket:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.initiateScan();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnItemClick(R.id.reconciliation_list)
    public void openReconciliation(int position) {
        showReconciliation(reconciliations.get(position));
    }

    @OnClick(R.id.reconciliation_list_add)
    public void newReconciliation() {
        new FetchBoardsTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult != null) {
                    Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jiraHelper.getIssueUrl(scanResult.getContents())));
                    startActivity(browseIntent);
                }
                break;
            case REQUEST_RECONCILIATION:
                updateReconciliations();
                break;
            case REQUEST_CAPTURE:
                Reconciliation reconciliation = (Reconciliation) data.getSerializableExtra(CaptureActivity.EXTRA_RECONCILIATION);
                reconciliationStore.addReconciliation(reconciliation);
                reconciliationStore.saveReconciliations();

                updateReconciliations();

                showReconciliation(reconciliation);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateReconciliations() {
        reconciliations = reconciliationStore.getReconciliations();

        listView.setAdapter(new ReconciliationAdapter(reconciliations));
    }

    private void showReconciliation(Reconciliation reconciliation) {
        Intent intent = new Intent(this, ReconciliationActivity.class);
        intent.putExtra(ReconciliationActivity.EXTRA_RECONCILIATION_ID, reconciliation.getId());
        startActivityForResult(intent, REQUEST_RECONCILIATION);
    }

    class ReconciliationAdapter extends ArrayAdapter<Reconciliation> {
        public ReconciliationAdapter(List<Reconciliation> reconciliations) {
            super(ReconciliationListActivity.this, 0, reconciliations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_reconciliation, null);
                convertView.setTag(new ViewHolder(convertView));
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.setReconciliation(reconciliations.get(position));

            return convertView;
        }

        class ViewHolder {
            @InjectView(R.id.reconciliation_list_item_board)
            TextView boardTextview;
            @InjectView(R.id.reconciliation_list_item_date) TextView dateTextView;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }

            public void setReconciliation(Reconciliation reconciliation) {
                boardTextview.setText(reconciliation.getBoard());
                dateTextView.setText(reconciliation.getDate().toString());
            }
        }
    }

    private class FetchBoardsTask extends AsyncTask<Void, Void, Board[]> {
        @Override
        protected Board[] doInBackground(Void... params) {
            List<Board> boards = jiraHelper.fetchBoards();
            return boards.toArray(new Board[boards.size()]);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(getString(R.string.loading_boards));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Board[] boards) {
            progressDialog.dismiss();

            FragmentManager fragmentManager = ReconciliationListActivity.this.getSupportFragmentManager();
            BoardSelectorFragment dialog = BoardSelectorFragment.newInstance(boards);
            dialog.setOnBoardSelectedListener(new BoardSelectorFragment.OnBoardSelectedListener() {
                @Override
                public void boardSelected(Board board) {
                    new FetchBoardDetailsTask().execute(board);
                }
            });
            dialog.show(fragmentManager, DIALOG_BOARD);
        }
    }

    private class FetchBoardDetailsTask extends AsyncTask<Board, Void, BoardDetails> {
        private Board board;

        @Override
        protected BoardDetails doInBackground(Board... params) {
            board = params[0];
            return jiraHelper.fetchBoardDetails(board.getId());
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(getString(R.string.loading_board_details));
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(final BoardDetails boardDetails) {
            progressDialog.dismiss();

            if (boardDetails.getLanes().length == 0) {
                Toast.makeText(ReconciliationListActivity.this, R.string.no_lanes, Toast.LENGTH_SHORT).show();
                return;
            }

            if (boardDetails.getIssues().length == 0) {
                Toast.makeText(ReconciliationListActivity.this, R.string.no_issues, Toast.LENGTH_SHORT).show();
                return;
            }

            FragmentManager fragmentManager = ReconciliationListActivity.this.getSupportFragmentManager();
            LaneSelectorFragment dialog = LaneSelectorFragment.newInstance(boardDetails);
            dialog.setOnLanesSelectedListener(new LaneSelectorFragment.OnLanesSelectedListener() {
                @Override
                public void lanesSelected(String[] lanes) {
                    if (lanes == null || lanes.length == 0) {
                        Toast.makeText(ReconciliationListActivity.this, R.string.no_lanes_selected, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent captureIntent = new Intent(ReconciliationListActivity.this, CaptureActivity.class);
                    captureIntent.putExtra(CaptureActivity.EXTRA_BOARD, board);
                    captureIntent.putExtra(CaptureActivity.EXTRA_BOARD_DETAILS, boardDetails);
                    captureIntent.putExtra(CaptureActivity.EXTRA_LANES, lanes);
                    startActivityForResult(captureIntent, 0);
                }
            });
            dialog.show(fragmentManager, DIALOG_LANE);
        }
    }
}
