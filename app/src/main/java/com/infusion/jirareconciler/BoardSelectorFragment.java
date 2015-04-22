package com.infusion.jirareconciler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class BoardSelectorFragment extends DialogFragment {
    public static final String EXTRA_SELECTED_BOARD = "com.infusion.jirareconciler.selected_board";
    public static final String EXTRA_BOARDS = "com.infusion.jirareconciler.boards";
    private Board board;
    private Board[] boards;
    private Spinner boardSpinner;

    public static BoardSelectorFragment newInstance(Board[] boards) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_BOARDS, boards);

        BoardSelectorFragment fragment = new BoardSelectorFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boards = (Board[]) getArguments().getSerializable(EXTRA_BOARDS);
        board = boards[0];

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_board, null);

        boardSpinner = (Spinner) view.findViewById(R.id.dialog_board_picker);
        ArrayAdapter<Board> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, boards);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boardSpinner.setAdapter(adapter);
        boardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                board = boards[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.board_picker_title)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendResult(Activity.RESULT_OK);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendResult(Activity.RESULT_CANCELED);
                            }
                        })
                .create();

        return dialog;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SELECTED_BOARD, board);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
