package com.infusion.jirareconciler.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class ReconciliationStore {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationStore.class);
    private final ReconciliationJSONSerializer serializer;
    private List<Reconciliation> reconciliations;

    public ReconciliationStore(ReconciliationJSONSerializer serializer) {
        this.serializer = serializer;

        try {
            reconciliations = serializer.loadReconciliations();
        } catch (Exception e) {
            reconciliations = new ArrayList<>();
            LOG.error("Error loading reconciliations", e);
        }
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
            LOG.debug("Reconciliations saved to file");
            return true;
        }
        catch (Exception e) {
            LOG.error("Error saving reconciliations", e);
            return false;
        }
    }
}
