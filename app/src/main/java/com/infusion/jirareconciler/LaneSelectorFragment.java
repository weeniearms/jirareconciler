package com.infusion.jirareconciler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.infusion.jirareconciler.base.BaseDialogFragment;
import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;

import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by rcieslak on 29/04/2015.
 */
public class LaneSelectorFragment extends BaseDialogFragment {
    public static final String EXTRA_SELECTED_LANES = "com.infusion.jirareconciler.selected_lanes";
    public static final String EXTRA_BOARD_DETAILS = "com.infusion.jirareconciler.board_details";
    public static final String EXTRA_BOARD = "com.infusion.jirareconciler.board";
    private final Set<String> selectedLanes = new HashSet<>();
    private Board board;
    private BoardDetails boardDetails;

    @InjectView(R.id.lanes_list) ListView listView;

    public static LaneSelectorFragment newInstance(Board board, BoardDetails boardDetails) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_BOARD, board);
        args.putSerializable(EXTRA_BOARD_DETAILS, boardDetails);

        LaneSelectorFragment fragment = new LaneSelectorFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        board = (Board) getArguments().getSerializable(EXTRA_BOARD);
        boardDetails = (BoardDetails) getArguments().getSerializable(EXTRA_BOARD_DETAILS);

        for (String lane : boardDetails.getLanes()) {
            selectedLanes.add(lane);
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_lanes, null);
        ButterKnife.inject(this, view);

        listView.setAdapter(new LaneAdapter(boardDetails.getLanes()));

        final AlertDialog selectorDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.lane_picker_title)
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
                .setNeutralButton(R.string.toggle_all, null)
                .create();

        selectorDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = selectorDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedLanes.size() > 0) {
                            selectedLanes.clear();
                        }
                        else {
                            for (String lane : boardDetails.getLanes()) {
                                selectedLanes.add(lane);
                            }
                        }

                        ((LaneAdapter)listView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        });

        return selectorDialog;
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_BOARD, board);
        intent.putExtra(EXTRA_BOARD_DETAILS, boardDetails);
        intent.putExtra(EXTRA_SELECTED_LANES, selectedLanes.toArray(new String[selectedLanes.size()]));

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    class LaneAdapter extends ArrayAdapter<String> {
        public LaneAdapter(String[] lanes) {
            super(getActivity(), 0, lanes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_lane, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }

            String lane = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.setLane(lane);
            holder.laneCheckBox.setTag(lane);
            holder.laneCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    String lane = (String) checkBox.getTag();
                    if (checkBox.isChecked()) {
                        selectedLanes.add(lane);
                    }
                    else {
                        selectedLanes.remove(lane);
                    }
                }
            });


            return convertView;
        }

        class ViewHolder {
            @InjectView(R.id.lane_list_item_lane) TextView laneTextView;
            @InjectView(R.id.lane_list_item_selected) CheckBox laneCheckBox;

            public ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }

            public void setLane(String lane) {
                laneTextView.setText(lane);
                laneCheckBox.setChecked(selectedLanes.contains(lane));
            }
        }
    }
}
