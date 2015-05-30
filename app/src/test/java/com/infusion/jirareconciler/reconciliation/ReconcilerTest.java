package com.infusion.jirareconciler.reconciliation;


import com.infusion.jirareconciler.jira.Board;
import com.infusion.jirareconciler.jira.BoardDetails;
import com.infusion.jirareconciler.model.Reconciliation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReconcilerTest {
    private Reconciler reconciler;

    @Mock
    private Board board;

    @Mock
    private BoardDetails boardDetails;

    @Before
    public void setUp() {
        List<String> lanes = new ArrayList<>();
        lanes.add("LANE1");
        lanes.add("LANE2");
        reconciler = new Reconciler(board, boardDetails, lanes);
    }

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

    @Test
    public void shouldReturnOnlyIssuesFromReconciledLanes() {
        // Given
        when(boardDetails.getIssues()).thenReturn(new String[] { "ISSUE-1", "ISSUE-2", "ISSUE-3", "ISSUE-4" });
        when(boardDetails.getLanes()).thenReturn(new String[] { "LANE1", "LANE2", "LANE3" });
        when(boardDetails.getLane("ISSUE-1")).thenReturn("LANE1");
        when(boardDetails.getLane("ISSUE-2")).thenReturn("LANE2");
        when(boardDetails.getLane("ISSUE-3")).thenReturn("LANE3");
        when(boardDetails.getLane("ISSUE-4")).thenReturn("LANE3");
        when(boardDetails.getIssues("LANE1")).thenReturn(new String[] { "ISSUE-1" });
        when(boardDetails.getIssues("LANE2")).thenReturn(new String[] { "ISSUE-2" });
        when(boardDetails.getIssues("LANE3")).thenReturn(new String[] { "ISSUE-3, ISSUE-4" });
        reconciler.addLane("LANE1", new String[] { "ISSUE-2" });
        reconciler.addLane("LANE2", new String[] { "ISSUE-3" });

        // When
        Reconciliation reconciliation = reconciler.reconcile();

        // Then
        assertEquals(3, reconciliation.getIssues().size());
        assertEquals("ISSUE-1", reconciliation.getIssues().get(0).getId());
        assertEquals("ISSUE-2", reconciliation.getIssues().get(1).getId());
        assertEquals("ISSUE-3", reconciliation.getIssues().get(2).getId());
        assertNull(reconciliation.getIssues().get(0).getBoardState());
        assertEquals("LANE1", reconciliation.getIssues().get(0).getJiraState());
        assertEquals("LANE1", reconciliation.getIssues().get(1).getBoardState());
        assertEquals("LANE2", reconciliation.getIssues().get(1).getJiraState());
        assertEquals("LANE2", reconciliation.getIssues().get(2).getBoardState());
        assertEquals("LANE3", reconciliation.getIssues().get(2).getJiraState());
    }
}