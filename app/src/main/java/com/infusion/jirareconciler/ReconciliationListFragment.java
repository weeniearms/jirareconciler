package com.infusion.jirareconciler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.jira.JiraHelper;
import com.infusion.jirareconciler.model.Reconciliation;
import com.infusion.jirareconciler.model.ReconciliationStore;

import java.util.List;

/**
 * Created by rcieslak on 20/04/2015.
 */
public class ReconciliationListFragment extends ListFragment {
    private static final int REQUEST_CAPTURE = 0;
    private static final int REQUEST_BOARD = 1;
    private static final int REQUEST_RECONCILIATION = 2;
    private static final String DIALOG_BOARD = "board";
    private List<Reconciliation> reconciliations;
    private ProgressDialog progressDialog;
    private JiraHelper jiraHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        updateReconciliations();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);

        jiraHelper = new JiraHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getActivity().getMenuInflater().inflate(R.menu.reconciliation_list_item_context, menu);
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
                        ReconciliationAdapter adapter = (ReconciliationAdapter) getListAdapter();
                        ReconciliationStore reconciliationStore = ReconciliationStore.get(getActivity());

                        for (int i = adapter.getCount() - 1; i >= 0; i--) {
                            if (getListView().isItemChecked(i)) {
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

        return view;
    }

    private void updateReconciliations() {
        reconciliations = ReconciliationStore.get(getActivity()).getReconciliations();

        setListAdapter(new ReconciliationAdapter(reconciliations));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_reconciliation_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_scan_ticket:
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(ReconciliationListFragment.this);
                integrator.initiateScan();
                return true;
            case R.id.menu_item_new_reconciliation:
                new FetchBoardsTask().execute();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        showReconciliation(reconciliations.get(position));
    }

    private void showReconciliation(Reconciliation reconciliation) {
        Intent intent = new Intent(getActivity(), ReconciliationActivity.class);
        intent.putExtra(ReconciliationFragment.EXTRA_RECONCILIATION_ID, reconciliation.getId());
        startActivityForResult(intent, REQUEST_RECONCILIATION);
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
            case REQUEST_BOARD:
                Board board = (Board) data.getSerializableExtra(BoardSelectorFragment.EXTRA_SELECTED_BOARD);
                new FetchBoardDetailsTask().execute(board);
                break;
            case REQUEST_CAPTURE:
                Reconciliation reconciliation = (Reconciliation) data.getSerializableExtra(CaptureFragment.EXTRA_RECONCILIATION);
                ReconciliationStore reconciliationStore = ReconciliationStore.get(getActivity());
                reconciliationStore.addReconciliation(reconciliation);
                reconciliationStore.saveReconciliations();

                updateReconciliations();

                showReconciliation(reconciliation);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ReconciliationAdapter extends ArrayAdapter<Reconciliation> {
        public ReconciliationAdapter(List<Reconciliation> reconciliations) {
            super(getActivity(), 0, reconciliations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_reconciliation, null);
            }

            Reconciliation reconciliation = reconciliations.get(position);

            TextView sprintTextView = (TextView) convertView.findViewById(R.id.reconciliation_list_item_sprint);
            sprintTextView.setText(reconciliation.getBoard());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.reconciliation_list_item_date);
            dateTextView.setText(reconciliation.getDate().toString());

            return convertView;
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

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            BoardSelectorFragment dialog = BoardSelectorFragment.newInstance(boards);
            dialog.setTargetFragment(ReconciliationListFragment.this, REQUEST_BOARD);
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
        protected void onPostExecute(BoardDetails boardDetails) {
            progressDialog.dismiss();

            if (boardDetails.getLanes().length == 0) {
                Toast.makeText(getActivity(), R.string.no_lanes, Toast.LENGTH_SHORT).show();
                return;
            }

            if (boardDetails.getIssues().length == 0) {
                Toast.makeText(getActivity(), R.string.no_issues, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent captureIntent = new Intent(getActivity(), CaptureActivity.class);
            captureIntent.putExtra(CaptureFragment.EXTRA_BOARD, board);
            captureIntent.putExtra(CaptureFragment.EXTRA_BOARD_DETAILS, boardDetails);
            startActivityForResult(captureIntent, 0);
        }
    }
}
