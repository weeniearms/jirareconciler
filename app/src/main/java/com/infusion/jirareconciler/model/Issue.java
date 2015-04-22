package com.infusion.jirareconciler.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class Issue {
    private static final String JSON_ID = "id";
    private static final String JSON_BOARD_STATE = "board_state";
    private static final String JSON_JIRA_STATE = "jira_state";
    private final String id;
    private final String boardState;
    private final String jiraState;

    public Issue(String id, String boardState, String jiraState) {
        this.id = id;
        this.boardState = boardState;
        this.jiraState = jiraState;
    }

    public Issue(JSONObject json) throws JSONException {
        id = json.getString(JSON_ID);
        boardState = json.optString(JSON_BOARD_STATE);
        jiraState = json.getString(JSON_JIRA_STATE);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_ID, id);
        json.put(JSON_BOARD_STATE, boardState);
        json.put(JSON_JIRA_STATE, jiraState);

        return json;
    }

    public String getId() {
        return id;
    }

    public String getBoardState() {
        return boardState;
    }

    public String getJiraState() {
        return jiraState;
    }

    public boolean isMissing() {
        return boardState == null;
    }

    public boolean isInSync() {
        return jiraState.equals(boardState);
    }
}
