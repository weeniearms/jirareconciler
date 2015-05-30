package com.infusion.jirareconciler.reconciliation;

import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.model.Issue;
import com.infusion.jirareconciler.model.Reconciliation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by rcieslak on 22/04/2015.
 */
public class Reconciler implements Serializable {
    private final Board board;
    private final BoardDetails boardDetails;
    private final Map<String, String[]> laneToIssuesMap = new HashMap<>();
    private final Map<String, String> issueToLaneMap = new HashMap<>();
    private final List<String> lanes;

    public Reconciler(Board board, BoardDetails boardDetails, List<String> lanes) {
        this.board = board;
        this.boardDetails = boardDetails;
        this.lanes = lanes;
    }

    public void addLane(String lane, String[] issues) {
        laneToIssuesMap.put(lane, issues);
        for (String issue : issues) {
            issueToLaneMap.put(issue, lane);
        }
    }

    public Reconciliation reconcile() {
        Reconciliation reconciliation = new Reconciliation(board.getName());
        Set<String> nonMatching = new HashSet<>();

        for (String issueId : boardDetails.getIssues()) {
            String jiraLane = boardDetails.getLane(issueId);
            if (!lanes.contains(jiraLane)) {
                continue;
            }

            String currentLane = issueToLaneMap.get(issueId);
            if (!jiraLane.equals(currentLane)) {
                Issue issue = new Issue(issueId, boardDetails.getTitle(issueId), currentLane, jiraLane);
                reconciliation.getIssues().add(issue);
                nonMatching.add(issueId);
            }
        }

        for (Map.Entry<String, String> entry : issueToLaneMap.entrySet()) {
            String issueId = entry.getKey();
            String currentLane = entry.getValue();
            if (nonMatching.contains(issueId)) {
                continue;
            }

            String jiraLane = boardDetails.getLane(issueId);
            if (!currentLane.equals(jiraLane)) {
                Issue issue = new Issue(issueId, boardDetails.getTitle(issueId), currentLane, jiraLane);
                reconciliation.getIssues().add(issue);
                nonMatching.add(issueId);
            }
        }

        return reconciliation;
    }
}
