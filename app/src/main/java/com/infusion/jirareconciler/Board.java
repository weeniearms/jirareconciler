package com.infusion.jirareconciler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class Board implements Serializable {
    private static final String JSON_ID = "id";
    private static final String JSON_NAME = "name";
    private final String id;
    private final String name;

    public Board(JSONObject json) throws JSONException {
        this.id = json.getString(JSON_ID);
        this.name = json.getString(JSON_NAME);
    }

    public Board(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
