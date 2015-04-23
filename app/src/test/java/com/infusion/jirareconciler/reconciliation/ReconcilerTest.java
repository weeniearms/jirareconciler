package com.infusion.jirareconciler.reconciliation;


import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.model.Reconciliation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReconcilerTest {
    @InjectMocks
    private Reconciler reconciler;

    @Mock
    private Board board;

    @Mock
    private BoardDetails boardDetails;

    @Test
    public void shouldReturnReconciliationWithBoardName() {
        // Given
        when(board.getName()).thenReturn("Test dashboard");
        when(boardDetails.getIssues()).thenReturn(new String[0]);

        // When
        Reconciliation reconciliation = reconciler.reconcile();

        // Then
        assertEquals("Test dashboard", reconciliation.getBoard());
    }

    @Test
    public void shouldReturnReconciliationWithIssuesMissingOnBoard() {
        // Given
        when(boardDetails.getIssues()).thenReturn(new String[] { "ISSUE-123" });
        when(boardDetails.getLanes()).thenReturn(new String[] { "LANE1" });
        when(boardDetails.getLane("ISSUE-123")).thenReturn("LANE1");
        when(boardDetails.getIssues("LANE1")).thenReturn(new String[] { "ISSUE-123" });
        reconciler.addLane("LANE1", new String[0]);

        // When
        Reconciliation reconciliation = reconciler.reconcile();

        // Then
        assertEquals(1, reconciliation.getIssues().size());
        assertEquals("ISSUE-123", reconciliation.getIssues().get(0).getId());
        assertNull(reconciliation.getIssues().get(0).getBoardState());
        assertEquals("LANE1", reconciliation.getIssues().get(0).getJiraState());
    }

    @Test
    public void shouldReturnReconciliationWithIssuesDifferentOnBoard() {
        // Given
        when(boardDetails.getIssues()).thenReturn(new String[] { "ISSUE-123" });
        when(boardDetails.getLanes()).thenReturn(new String[] { "LANE1", "LANE2" });
        when(boardDetails.getLane("ISSUE-123")).thenReturn("LANE1");
        when(boardDetails.getIssues("LANE1")).thenReturn(new String[] { "ISSUE-123" });
        when(boardDetails.getIssues("LANE2")).thenReturn(new String[0]);
        reconciler.addLane("LANE1", new String[0]);
        reconciler.addLane("LANE2", new String[] { "ISSUE-123" });

        // When
        Reconciliation reconciliation = reconciler.reconcile();

        // Then
        assertEquals(1, reconciliation.getIssues().size());
        assertEquals("ISSUE-123", reconciliation.getIssues().get(0).getId());
        assertEquals("LANE2", reconciliation.getIssues().get(0).getBoardState());
        assertEquals("LANE1", reconciliation.getIssues().get(0).getJiraState());
    }
}