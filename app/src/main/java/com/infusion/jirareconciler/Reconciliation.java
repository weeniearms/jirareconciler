package com.infusion.jirareconciler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class Reconciliation {
    private static final String JSON_ID = "id";
    private static final String JSON_BOARD = "board";
    private static final String JSON_DATE = "date";
    private static final String JSON_ISSUES = "issues";
    private final UUID id;
    private final String board;
    private final Date date;
    private final List<Issue> issues;

    public Reconciliation(String sprint) {
        this.id = UUID.randomUUID();
        this.board = sprint;
        this.date = new Date();
        this.issues = new ArrayList<>();
    }

    public Reconciliation(JSONObject json) throws JSONException {
        id = UUID.fromString(json.getString(JSON_ID));
        board = json.getString(JSON_BOARD);
        date = new Date(json.getLong(JSON_DATE));
        issues = new ArrayList<>();

        JSONArray array = json.getJSONArray(JSON_ISSUES);
        for (int i = 0; i < array.length(); i++) {
            issues.add(new Issue(array.getJSONObject(i)));
        }
    }

    public UUID getId() {
        return id;
    }

    public String getBoard() {
        return board;
    }

    public Date getDate() {
        return date;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_BOARD, board);
        json.put(JSON_DATE, date.getTime());

        JSONArray array = new JSONArray();
        for (Issue issue : issues) {
            array.put(issue.toJSON());
        }

        json.put(JSON_ISSUES, array);

        return json;
    }
}
