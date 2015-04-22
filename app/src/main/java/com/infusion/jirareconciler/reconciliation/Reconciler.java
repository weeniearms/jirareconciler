package com.infusion.jirareconciler.reconciliation;

import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.model.Reconciliation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rcieslak on 22/04/2015.
 */
public class Reconciler implements Serializable {
    private final Board board;
    private final BoardDetails boardDetails;
    private final Map<String, String[]> lanes = new HashMap<>();

    public Reconciler(Board board, BoardDetails boardDetails) {
        this.board = board;
        this.boardDetails = boardDetails;
    }

    public void addLane(String lane, String[] issues) {
        lanes.put(lane, issues);
    }

    public Reconciliation reconcile() {
        Reconciliation reconciliation = new Reconciliation(board.getName());
        return reconciliation;
    }
}
