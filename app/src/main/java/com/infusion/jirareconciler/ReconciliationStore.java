package com.infusion.jirareconciler;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationStore {
    private static final String TAG = "ReconciliationStore";
    private static final String FILENAME = "reconciliations.json";
    private static ReconciliationStore reconciliationStore;
    private final ReconciliationJSONSerializer serializer;
    private Context appContext;
    private List<Reconciliation> reconciliations;

    private ReconciliationStore(Context context) {
        appContext = context;
        serializer = new ReconciliationJSONSerializer(appContext, FILENAME);

        try {
//            reconciliations = serializer.loadReconciliations();

            reconciliations = new ArrayList<>();
            Reconciliation reconciliation1 = new Reconciliation("SPRINT 1");
            reconciliations.add(reconciliation1);

            Reconciliation reconciliation2 = new Reconciliation("SPRINT 1");
            reconciliations.add(reconciliation2);

            Reconciliation reconciliation3 = new Reconciliation("SPRINT 2");
            Issue issue1 = new Issue("DEMO-12345", "IN PROGRESS", "TO DO");
            Issue issue2 = new Issue("DEMO-54321", null, "TO DO");
            reconciliation3.getIssues().add(issue1);
            reconciliation3.getIssues().add(issue2);
            reconciliations.add(reconciliation3);
        } catch (Exception e) {
            reconciliations = new ArrayList<>();
            Log.e(TAG, "Error loading reconciliations: ", e);
        }

    }

    public static ReconciliationStore get(Context context) {
        if (reconciliationStore == null) {
            reconciliationStore = new ReconciliationStore(context.getApplicationContext());
        }

        return reconciliationStore;
    }

    public List<Reconciliation> getReconciliations() {
        return reconciliations;
    }

    public Reconciliation getReconciliation(UUID id) {
        for (Reconciliation reconciliation : reconciliations) {
            if (reconciliation.getId().equals(id)) {
                return reconciliation;
            }
        }

        return null;
    }

    public void addReconciliation(Reconciliation reconciliation) {
        reconciliations.add(reconciliation);
    }

    public void deleteReconciliation(Reconciliation reconciliation) {
        reconciliations.remove(reconciliation);
    }

    public boolean saveReconciliations() {
        try {
            serializer.saveReconciliations(reconciliations);
            Log.d(TAG, "Reconciliations saved to file");
            return true;
        }
        catch (Exception e) {
            Log.e(TAG, "Error saving reconciliations: ", e);
            return false;
        }
    }
}
