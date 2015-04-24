package com.infusion.jirareconciler.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationJSONSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationJSONSerializer.class);
    private final Context context;
    private final String fileName;

    public ReconciliationJSONSerializer(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    public void saveReconciliations(List<Reconciliation> reconciliations) throws IOException, JSONException {
        JSONArray array = new JSONArray();

        for (Reconciliation reconciliation : reconciliations) {
            array.put(reconciliation.toJSON());
        }

        Writer writer = null;
        try {
            OutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(outputStream);
            writer.write(array.toString());
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public List<Reconciliation> loadReconciliations() throws IOException, JSONException {
        ArrayList<Reconciliation> reconciliations = new ArrayList<>();

        BufferedReader reader = null;
        try {
            InputStream inputStream = context.openFileInput(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            for (int i = 0; i < array.length(); i++) {
                reconciliations.add(new Reconciliation(array.getJSONObject(i)));
            }
        }
        catch (FileNotFoundException e) {
            LOG.debug("Reconciliations file not found", e);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        return reconciliations;
    }
}
