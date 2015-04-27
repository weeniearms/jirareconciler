package com.infusion.jirareconciler.reconciliation;

import com.infusion.jirareconciler.model.Reconciliation;
import com.infusion.jirareconciler.model.ReconciliationJSONSerializer;
import com.infusion.jirareconciler.model.ReconciliationStore;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by rcieslak on 27/04/2015.
 */
public class ReconciliationStoreTest {
    @Test
    public void shouldLoadReconciliationsOnConstruction() throws Throwable {
        // Given
        List<Reconciliation> reconciliations = mock(List.class);
        ReconciliationJSONSerializer serializer = mock(ReconciliationJSONSerializer.class);
        when(serializer.loadReconciliations()).thenReturn(reconciliations);

        // When
        ReconciliationStore store = new ReconciliationStore(serializer);

        // Then
        assertEquals(reconciliations, store.getReconciliations());
        verify(serializer).loadReconciliations();
        verifyZeroInteractions(reconciliations);
    }

    @Test
    public void shouldLoadEmptyReconciliationsOnSerializerError() throws Throwable {
        // Given
        ReconciliationJSONSerializer serializer = mock(ReconciliationJSONSerializer.class);
        when(serializer.loadReconciliations()).thenThrow(new IOException());

        // When
        ReconciliationStore store = new ReconciliationStore(serializer);

        // Then
        assertTrue(store.getReconciliations().isEmpty());
        verify(serializer).loadReconciliations();
    }

    @Test
     public void shouldRetrieveReconciliationById() throws Throwable {
        // Given
        UUID id = UUID.randomUUID();
        List<Reconciliation> reconciliations = Arrays.asList(
                mock(Reconciliation.class),
                mock(Reconciliation.class),
                mock(Reconciliation.class));
        when(reconciliations.get(0).getId()).thenReturn(UUID.randomUUID());
        when(reconciliations.get(1).getId()).thenReturn(id);
        when(reconciliations.get(2).getId()).thenReturn(UUID.randomUUID());

        ReconciliationJSONSerializer serializer = mock(ReconciliationJSONSerializer.class);
        when(serializer.loadReconciliations()).thenReturn(reconciliations);

        // When
        ReconciliationStore store = new ReconciliationStore(serializer);
        Reconciliation reconciliation = store.getReconciliation(id);

        // Then
        assertEquals(reconciliations.get(1), reconciliation);
    }

    @Test
    public void shouldAddReconciliation() throws Throwable {
        // Given
        List<Reconciliation> reconciliations = new ArrayList<>();
        reconciliations.add(mock(Reconciliation.class));
        reconciliations.add(mock(Reconciliation.class));
        reconciliations.add(mock(Reconciliation.class));

        Reconciliation reconciliation = mock(Reconciliation.class);

        ReconciliationJSONSerializer serializer = mock(ReconciliationJSONSerializer.class);
        when(serializer.loadReconciliations()).thenReturn(reconciliations);

        // When
        ReconciliationStore store = new ReconciliationStore(serializer);
        store.addReconciliation(reconciliation);

        // Then
        assertTrue(store.getReconciliations().contains(reconciliation));
    }

    @Test
    public void shouldDeleteReconciliation() throws Throwable {
        // Given
        List<Reconciliation> reconciliations = new ArrayList<>();
        reconciliations.add(mock(Reconciliation.class));
        reconciliations.add(mock(Reconciliation.class));
        reconciliations.add(mock(Reconciliation.class));

        Reconciliation reconciliation = reconciliations.get(1);

        ReconciliationJSONSerializer serializer = mock(ReconciliationJSONSerializer.class);
        when(serializer.loadReconciliations()).thenReturn(reconciliations);

        // When
        ReconciliationStore store = new ReconciliationStore(serializer);
        store.deleteReconciliation(reconciliation);

        // Then
        assertFalse(store.getReconciliations().contains(reconciliation));
    }

    @Test
    public void shouldSaveReconciliations() throws Throwable {
        // Given
        List<Reconciliation> reconciliations = mock(List.class);
        ReconciliationJSONSerializer serializer = mock(ReconciliationJSONSerializer.class);
        when(serializer.loadReconciliations()).thenReturn(reconciliations);

        // When
        ReconciliationStore store = new ReconciliationStore(serializer);
        store.saveReconciliations();

        // Then
        assertEquals(reconciliations, store.getReconciliations());
        verify(serializer).loadReconciliations();
        verify(serializer).saveReconciliations(reconciliations);
    }
}
