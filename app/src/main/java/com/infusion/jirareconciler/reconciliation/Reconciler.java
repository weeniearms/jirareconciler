package com.infusion.jirareconciler.reconciliation;

import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.model.Issue;
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
    private final Map<String, String[]> laneToIssuesMap = new HashMap<>();
    private final Map<String, String> issueToLaneMap = new HashMap<>();

    public Reconciler(Board board, BoardDetails boardDetails) {
        this.board = board;
        this.boardDetails = boardDetails;
    }

    public void addLane(String lane, String[] issues) {
        laneToIssuesMap.put(lane, issues);
        for (String issue : issues) {
            issueToLaneMap.put(issue, lane);
        }
    }

    public Reconciliation reconcile() {
        Reconciliation reconciliation = new Reconciliation(board.getName());

        for (String issueId : boardDetails.getIssues()) {
            String jiraLane = boardDetails.getLane(issueId);
            String currentLane = issueToLaneMap.get(issueId);
            if (!jiraLane.equals(currentLane)) {
                Issue issue = new Issue(issueId, boardDetails.getTitle(issueId), currentLane, jiraLane);
                reconciliation.getIssues().add(issue);
            }
        }

        return reconciliation;
    }
}
