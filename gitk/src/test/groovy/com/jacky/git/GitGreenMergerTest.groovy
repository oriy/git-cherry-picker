package com.jacky.git


import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.GitHubRequest
import org.eclipse.egit.github.core.service.IssueService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

import static com.jacky.git.PullRequestStatusService.BUILD_SUCCESS_DESCRIPTION
import static com.jacky.git.PullRequestStatusService.DEFAULT_BUILD_CONTEXT
import static org.eclipse.egit.github.core.CommitStatus.STATE_SUCCESS
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertSame
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.verifyNoMoreInteractions
import static org.mockito.Mockito.when

/**
 * User: oriy
 * Date: 07/03/2017
 */
@RunWith(MockitoJUnitRunner.Silent.class)
class GitGreenMergerTest {

    static final int PR_ID = 213
    static final IRepositoryIdProvider repositoryId = GitHubUtil.createRepositoryId('repo')

    GitGreenMerger gitGreenMerger

    @Mock
    GitCommandExecutor gitCommandExecutor

    @Mock
    GitHubClient gitHubClient

    @Mock
    PullRequestMergeService pullRequestMergeService

    @Mock
    PullRequestStatusService pullRequestStatusService

    @Mock
    IssueService issueService

    @Mock
    GitKDataService gitKDataService

    @Mock
    HttpURLConnection mockConnention

    @Mock
    PullRequest pullRequest

    @Mock
    User user

    PullRequestMarker prHead

    @Captor
    ArgumentCaptor<GitHubRequest> requestCaptor = ArgumentCaptor.forClass(GitHubRequest.class)

    @Before
    public void init() {
        gitGreenMerger = new GitGreenMerger(gitCommandExecutor, gitHubClient,
                                            pullRequestMergeService, pullRequestStatusService,
                                            issueService, gitKDataService, repositoryId)

        prHead = new PullRequestMarker().setLabel('repo:branch')
        when(pullRequestMergeService.getPullRequest(repositoryId, PR_ID)).thenReturn(pullRequest)
        when(pullRequest.getNumber()).thenReturn(PR_ID)
        when(pullRequest.getChangedFiles()).thenReturn(1)
        when(pullRequest.getHead()).thenReturn(prHead)
        when(pullRequest.getUser()).thenReturn(user)
        when(user.getLogin()).thenReturn('admin')
        when(gitHubClient.getUser()).thenReturn('admin')
    }

    @Test
    public void testGetPullRequest() {
        PullRequest actualPullRequest = gitGreenMerger.getPullRequest(PR_ID)
        assertSame(pullRequest, actualPullRequest)

        verify(pullRequestMergeService).getPullRequest(repositoryId, PR_ID)
    }

    @Test
    public void testMarkDefaultPullRequestBuildSuccess() {
        final String prUrl = 'url'
        when(pullRequest.getUrl()).thenReturn(prUrl)

        gitGreenMerger.markDefaultPullRequestBuildSuccess(pullRequest, false)

        verify(pullRequest).getUrl()
        verify(pullRequestStatusService).updatePullRequestStatus(repositoryId, pullRequest, DEFAULT_BUILD_CONTEXT, BUILD_SUCCESS_DESCRIPTION, STATE_SUCCESS, prUrl)
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testMarkDefaultPullRequestBuildSuccessDryRun() {
        gitGreenMerger.markDefaultPullRequestBuildSuccess(pullRequest, true)

        verify(pullRequest).getNumber()
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestReturnsFalseWhenMerged() {
        when(pullRequest.isMerged()).thenReturn(true)

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        verify(gitHubClient).getUser()
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestMergesOnPrSuccess() {
        when(pullRequest.isMerged()).thenReturn(false)
        when(pullRequestStatusService.getMergeState(repositoryId, prHead, false)).thenReturn(new GitMergeState(prHead))

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        verify(gitHubClient).getUser()
        verify(pullRequestStatusService).getMergeState(repositoryId, prHead, false)
        verify(pullRequestMergeService).merge(repositoryId, PR_ID, "Auto-merged", PullRequestMergeService.MergeMethod.MERGE)
        verify(gitCommandExecutor).gitPull('--prune --progress')
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestMergesIfNoFileHasChanged() {
        when(pullRequest.isMerged()).thenReturn(false)
        when(pullRequest.getChangedFiles()).thenReturn(0)

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        verify(pullRequestMergeService).merge(repositoryId, PR_ID, "Auto-merged", PullRequestMergeService.MergeMethod.MERGE)
        verify(gitCommandExecutor).gitPull('--prune --progress')
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestRetestPullRequestIfBuildPending() {
        when(pullRequest.isMerged()).thenReturn(false)
        when(pullRequestStatusService.getMergeState(repositoryId, prHead, false)).thenReturn(new GitMergeState(false, 'waiting', true))

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        verify(gitHubClient).getUser()
        verify(pullRequestStatusService).getMergeState(repositoryId, prHead, false)
        verify(issueService).createComment(repositoryId, PR_ID, gitGreenMerger.RUN_TESTS)
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testPullRequestMergeStateReturnsFalseForAnotherUser() {
        User otherUser = Mockito.mock(User.class)
        when(otherUser.getLogin()).thenReturn('other')
        when(pullRequest.getUser()).thenReturn(otherUser)

        GitMergeState actualState = gitGreenMerger.getPullRequestMergeState(repositoryId, pullRequest, false)

        assertNull(actualState.getBranchToMerge())
        assertEquals('Auto-merge is only available for admin', actualState.getDescription())
        assertFalse(actualState.isMergeable())
    }

    @Test
    public void testGetCommitMergeStateOnPrSuccess() {
        when(pullRequest.isMerged()).thenReturn(false)
        GitMergeState gitMergeState = Mockito.mock(GitMergeState.class)
        when(pullRequestStatusService.getMergeState(repositoryId, prHead, false)).thenReturn(gitMergeState)

        GitMergeState actualState = gitGreenMerger.getPullRequestMergeState(repositoryId, pullRequest, false)

        assertSame(gitMergeState, actualState)

        verify(gitHubClient).getUser()
        verify(pullRequestStatusService).getMergeState(repositoryId, prHead, false)
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testGetCommitMergeStateOnDefaultContextOnlySuccess() {
        when(pullRequest.isMerged()).thenReturn(false)
        GitMergeState gitMergeState = Mockito.mock(GitMergeState.class)
        when(pullRequestStatusService.getMergeState(repositoryId, prHead, true)).thenReturn(gitMergeState)

        GitMergeState actualState = gitGreenMerger.getPullRequestMergeState(repositoryId, pullRequest, true)

        assertSame(gitMergeState, actualState)

        verify(gitHubClient).getUser()
        verify(pullRequestStatusService).getMergeState(repositoryId, prHead, true)
        verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }
}
