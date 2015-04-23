package com.infusion.jirareconciler.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class Issue implements Serializable {
    private static final String JSON_ID = "id";
    private static final String JSON_BOARD_STATE = "board_state";
    private static final String JSON_JIRA_STATE = "jira_state";
    private static final String JSON_TITLE = "title";
    private final String id;
    private final String boardState;
    private final String jiraState;
    private final String title;

    public Issue(String id, String title, String boardState, String jiraState) {
        this.id = id;
        this.title = title;
        this.boardState = boardState;
        this.jiraState = jiraState;
    }

    public Issue(JSONObject json) throws JSONException {
        id = json.getString(JSON_ID);
        title = json.optString(JSON_TITLE);
        boardState = json.optString(JSON_BOARD_STATE);
        jiraState = json.getString(JSON_JIRA_STATE);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_ID, id);
        json.put(JSON_TITLE, title);
        json.put(JSON_BOARD_STATE, boardState);
        json.put(JSON_JIRA_STATE, jiraState);

        return json;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
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
