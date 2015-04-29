package com.infusion.jirareconciler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.infusion.jirareconciler.base.BaseDialogFragment;
import com.infusion.jirareconciler.jira.Board;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class BoardSelectorFragment extends BaseDialogFragment {
    public static final String EXTRA_BOARDS = "com.infusion.jirareconciler.boards";
    private OnBoardSelectedListener boardSelectedListener;
    private Board board;
    private Board[] boards;

    @InjectView(R.id.dialog_board_picker) Spinner boardSpinner;

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
        ButterKnife.inject(this, view);

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
                                if (boardSelectedListener != null) {
                                    boardSelectedListener.boardSelected(board);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        return dialog;
    }

    public void setOnBoardSelectedListener(OnBoardSelectedListener boardSelectedListener) {
        this.boardSelectedListener = boardSelectedListener;
    }

    public interface OnBoardSelectedListener {
        void boardSelected(Board board);
    }
}
