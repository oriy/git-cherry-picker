package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.service.IssueService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

import static org.eclipse.egit.github.core.service.IssueService.FILTER_LABELS
import static org.eclipse.egit.github.core.service.IssueService.FILTER_STATE
import static org.eclipse.egit.github.core.service.IssueService.STATE_OPEN

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertSame
import static org.mockito.Mockito.when

/**
 * User: oriy
 * Date: 24/05/2017
 */
@RunWith(MockitoJUnitRunner.Silent.class)
class IssueServiceHelperTest {

    static final IRepositoryIdProvider repositoryId = GitHubUtil.createRepositoryId('repo')

    @Mock
    IssueService issueService

    @Test
    public void testGetOpenPRsByLabel() {
        String label = 'myLable1'
        List<Issue> expectedIssueList = new ArrayList<>()
        ArgumentCaptor<Map<String, String>> filterCaptor = ArgumentCaptor.forClass(Map.class)
        when(issueService.getIssues(Mockito.eq(repositoryId), filterCaptor.capture())).thenReturn(expectedIssueList)

        List<Issue> actualIssueList = IssueServiceHelper.getOpenIssuesByLabel(issueService, repositoryId, label)

        Map<String, String> filterMap = filterCaptor.getValue()
        assertEquals(STATE_OPEN, filterMap.get(FILTER_STATE))
        assertEquals(label, filterMap.get(FILTER_LABELS))
        assertSame(expectedIssueList, actualIssueList)
    }
}
