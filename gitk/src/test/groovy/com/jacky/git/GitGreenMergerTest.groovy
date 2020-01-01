package com.jacky.git


import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.GitHubRequest
import org.eclipse.egit.github.core.service.IssueService
import org.junit.Assert
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
        Mockito.when(pullRequestMergeService.getPullRequest(repositoryId, PR_ID)).thenReturn(pullRequest)
        Mockito.when(pullRequest.getNumber()).thenReturn(PR_ID)
        Mockito.when(pullRequest.getChangedFiles()).thenReturn(1)
        Mockito.when(pullRequest.getHead()).thenReturn(prHead)
        Mockito.when(pullRequest.getUser()).thenReturn(user)
        Mockito.when(user.getLogin()).thenReturn('admin')
        Mockito.when(gitHubClient.getUser()).thenReturn('admin')
    }

    @Test
    public void testGetPullRequest() {
        PullRequest actualPullRequest = gitGreenMerger.getPullRequest(PR_ID)
        Assert.assertSame(pullRequest, actualPullRequest)

        Mockito.verify(pullRequestMergeService).getPullRequest(repositoryId, PR_ID)
    }

    @Test
    public void testMarkDefaultPullRequestBuildSuccess() {
        final String prUrl = 'url'
        Mockito.when(pullRequest.getUrl()).thenReturn(prUrl)

        gitGreenMerger.markDefaultPullRequestBuildSuccess(pullRequest, false)

        Mockito.verify(pullRequest).getUrl()
        Mockito.verify(pullRequestStatusService).updatePullRequestStatus(repositoryId, pullRequest, DEFAULT_BUILD_CONTEXT, BUILD_SUCCESS_DESCRIPTION, STATE_SUCCESS, prUrl)
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testMarkDefaultPullRequestBuildSuccessDryRun() {
        gitGreenMerger.markDefaultPullRequestBuildSuccess(pullRequest, true)

        Mockito.verify(pullRequest).getNumber()
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestReturnsFalseWhenMerged() {
        Mockito.when(pullRequest.isMerged()).thenReturn(true)

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        Mockito.verify(gitHubClient).getUser()
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestMergesOnPrSuccess() {
        Mockito.when(pullRequest.isMerged()).thenReturn(false)
        Mockito.when(pullRequestStatusService.getMergeState(repositoryId, prHead, false)).thenReturn(new GitMergeState(prHead))

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        Mockito.verify(gitHubClient).getUser()
        Mockito.verify(pullRequestStatusService).getMergeState(repositoryId, prHead, false)
        Mockito.verify(pullRequestMergeService).merge(repositoryId, PR_ID, "Auto-merged", PullRequestMergeService.MergeMethod.SQUASH)
        Mockito.verify(gitKDataService).deleteReference(repositoryId, prHead.getRef())
        Mockito.verify(gitCommandExecutor).gitPull('--prune --progress')
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestMergesIfNoFileHasChanged() {
        Mockito.when(pullRequest.isMerged()).thenReturn(false)
        Mockito.when(pullRequest.getChangedFiles()).thenReturn(0)

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        Mockito.verify(pullRequestMergeService).merge(repositoryId, PR_ID, "Auto-merged", PullRequestMergeService.MergeMethod.SQUASH)
        Mockito.verify(gitKDataService).deleteReference(repositoryId, prHead.getRef())
        Mockito.verify(gitCommandExecutor).gitPull('--prune --progress')
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testHandlePullRequestRetestPullRequestIfBuildPending() {
        Mockito.when(pullRequest.isMerged()).thenReturn(false)
        Mockito.when(pullRequestStatusService.getMergeState(repositoryId, prHead, false)).thenReturn(new GitMergeState(false, 'waiting', true))

        gitGreenMerger.handlePullRequest(pullRequest, false, false)

        Mockito.verify(gitHubClient).getUser()
        Mockito.verify(pullRequestStatusService).getMergeState(repositoryId, prHead, false)
        Mockito.verify(issueService).createComment(repositoryId, PR_ID, gitGreenMerger.RETEST_THIS_PLEASE)
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testPullRequestMergeStateReturnsFalseForAnotherUser() {
        User otherUser = Mockito.mock(User.class)
        Mockito.when(otherUser.getLogin()).thenReturn('other')
        Mockito.when(pullRequest.getUser()).thenReturn(otherUser)

        GitMergeState actualState = gitGreenMerger.getPullRequestMergeState(repositoryId, pullRequest, false)

        Assert.assertNull(actualState.getBranchToMerge())
        Assert.assertEquals('Auto-merge is only available for admin', actualState.getDescription())
        Assert.assertFalse(actualState.isMergeable())
    }

    @Test
    public void testGetCommitMergeStateOnPrSuccess() {
        Mockito.when(pullRequest.isMerged()).thenReturn(false)
        GitMergeState gitMergeState = Mockito.mock(GitMergeState.class)
        Mockito.when(pullRequestStatusService.getMergeState(repositoryId, prHead, false)).thenReturn(gitMergeState)

        GitMergeState actualState = gitGreenMerger.getPullRequestMergeState(repositoryId, pullRequest, false)

        Assert.assertSame(gitMergeState, actualState)

        Mockito.verify(gitHubClient).getUser()
        Mockito.verify(pullRequestStatusService).getMergeState(repositoryId, prHead, false)
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }

    @Test
    public void testGetCommitMergeStateOnDefaultContextOnlySuccess() {
        Mockito.when(pullRequest.isMerged()).thenReturn(false)
        GitMergeState gitMergeState = Mockito.mock(GitMergeState.class)
        Mockito.when(pullRequestStatusService.getMergeState(repositoryId, prHead, true)).thenReturn(gitMergeState)

        GitMergeState actualState = gitGreenMerger.getPullRequestMergeState(repositoryId, pullRequest, true)

        Assert.assertSame(gitMergeState, actualState)

        Mockito.verify(gitHubClient).getUser()
        Mockito.verify(pullRequestStatusService).getMergeState(repositoryId, prHead, true)
        Mockito.verifyNoMoreInteractions(gitCommandExecutor, gitHubClient, pullRequestMergeService, pullRequestStatusService, issueService, gitKDataService)
    }
}
