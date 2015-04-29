package com.infusion.jirareconciler;

import android.support.v4.app.Fragment;

import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class CaptureActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return CaptureFragment.newInstance(
                (Board) getIntent().getSerializableExtra(CaptureFragment.EXTRA_BOARD),
                (BoardDetails) getIntent().getSerializableExtra(CaptureFragment.EXTRA_BOARD_DETAILS),
                (String[]) getIntent().getSerializableExtra(CaptureFragment.EXTRA_LANES));
    }
}
