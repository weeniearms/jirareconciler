package com.infusion.jirareconciler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rcieslak on 22/04/2015.
 */
public class BoardDetails implements Serializable {
    private static final String JSON_COLUMNS_DATA = "columnsData";
    private static final String JSON_COLUMNS = "columns";
    private static final String JSON_COLUMN_NAME = "name";
    private static final String JSON_COLUMN_STATUS_IDS = "statusIds";
    private static final String JSON_ISSUES_DATA = "issuesData";
    private static final String JSON_ISSUES = "issues";
    private static final String JSON_ISSUE_ID = "key";
    private static final String JSON_ISSUE_STATUS_ID = "statusId";
    private final List<String> issues = new ArrayList<>();
    private final List<String> lanes = new ArrayList<>();
    private final Map<String, String> issueToLaneMap = new HashMap<>();
    private final Map<String, List<String>> laneToIssuesMap = new HashMap<>();

    public BoardDetails(JSONObject json) throws JSONException {
        Map<String, String> statusToLaneMap = new HashMap<>();

        JSONObject columnsData = json.getJSONObject(JSON_COLUMNS_DATA);
        JSONArray columns = columnsData.getJSONArray(JSON_COLUMNS);
        for (int i = 0; i < columns.length(); i++) {
            JSONObject column = columns.getJSONObject(i);
            String lane = column.getString(JSON_COLUMN_NAME);
            lanes.add(lane);
            laneToIssuesMap.put(lane, new ArrayList<String>());

            JSONArray statusIds = column.getJSONArray(JSON_COLUMN_STATUS_IDS);
            for (int j = 0; j < statusIds.length(); j++) {
                String statusId = statusIds.getString(j);
                statusToLaneMap.put(statusId, lane);
            }
        }

        JSONObject issuesData = json.getJSONObject(JSON_ISSUES_DATA);
        JSONArray issues = issuesData.getJSONArray(JSON_ISSUES);
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            String id = issue.getString(JSON_ISSUE_ID);
            String statusId = issue.getString(JSON_ISSUE_STATUS_ID);
            this.issues.add(id);

            String lane = statusToLaneMap.get(statusId);
            if (lane != null) {
                issueToLaneMap.put(id, lane);
                laneToIssuesMap.get(lane).add(id);
            }
        }
    }

    public String[] getIssues() {
        return issues.toArray(new String[issues.size()]);
    }

    public String[] getIssues(String lane) {
        List<String> issues = laneToIssuesMap.get(lane);
        return issues.toArray(new String[issues.size()]);
    }

    public String getLane(String issueId) {
        return issueToLaneMap.get(issueId);
    }

    public String[] getLanes() {
        return lanes.toArray(new String[lanes.size()]);
    }
}
