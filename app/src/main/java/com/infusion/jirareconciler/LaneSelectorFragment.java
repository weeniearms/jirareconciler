package com.infusion.jirareconciler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by rcieslak on 29/04/2015.
 */
public class LaneSelectorFragment extends BaseDialogFragment {
    public static final String EXTRA_BOARD_DETAILS = "com.infusion.jirareconciler.board_details";
    private final Set<String> selectedLanes = new HashSet<>();
    private BoardDetails boardDetails;

    @InjectView(R.id.lanes_list) ListView listView;
    private OnLanesSelectedListener lanesSelectedListener;

    public static LaneSelectorFragment newInstance(BoardDetails boardDetails) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_BOARD_DETAILS, boardDetails);

        LaneSelectorFragment fragment = new LaneSelectorFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
                                List<String> lanes = new ArrayList<>();
                                for (String lane : boardDetails.getLanes()) {
                                    if (selectedLanes.contains(lane)) {
                                        lanes.add(lane);
                                    }
                                }

                                if (lanesSelectedListener != null) {
                                    lanesSelectedListener.lanesSelected(lanes.toArray(new String[lanes.size()]));
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
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

    public void setOnLanesSelectedListener(OnLanesSelectedListener lanesSelectedListener) {
        this.lanesSelectedListener = lanesSelectedListener;
    }

    public interface OnLanesSelectedListener {
        void lanesSelected(String[] lanes);
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
