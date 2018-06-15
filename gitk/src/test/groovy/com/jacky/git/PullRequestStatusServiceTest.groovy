package com.jacky.git

import com.google.gson.internal.StringMap
import org.eclipse.egit.github.core.CommitStatus
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.GitHubRequest
import org.eclipse.egit.github.core.client.GitHubResponse
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

import static com.jacky.git.GitHubUtil.OWNER
import static com.jacky.git.PullRequestStatus.*
import static com.jacky.git.PullRequestStatusService.DEFAULT_BUILD_CONTEXT
import static com.jacky.git.PullRequestStatusService.STATUSES
import static org.eclipse.egit.github.core.CommitStatus.STATE_FAILURE
import static org.eclipse.egit.github.core.CommitStatus.STATE_PENDING
import static org.eclipse.egit.github.core.CommitStatus.STATE_SUCCESS
import static org.junit.Assert.assertEquals

/**
 * User: oriy
 * Date: 20/03/2017
 */
@RunWith(MockitoJUnitRunner.class)
class PullRequestStatusServiceTest {

    static final IRepositoryIdProvider repositoryId = GitHubUtil.createRepositoryId('repo')
    public static final String DATE = '2017-05-18T09:58:12Z'

    PullRequestStatusService pullRequestStatusService

    @Mock
    GitHubClient gitHubClient

    @Mock
    HttpURLConnection mockConnention

    @Mock
    PullRequest pullRequest

    @Captor
    ArgumentCaptor<GitHubRequest> requestCaptor = ArgumentCaptor.forClass(GitHubRequest.class)

    @Captor
    ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class)

    @Before
    public void init() {
        pullRequestStatusService = new PullRequestStatusService(gitHubClient)
    }

    @Test
    public void testGetCommitMergeStateOnPrSuccess() {
        StringMap<?> defaultContextStatus = createResponseStatusStringMap(1, DEFAULT_BUILD_CONTEXT, STATE_SUCCESS, 'buildUrl')
        StringMap<?> successfulResponseString = new StringMap<>()
        successfulResponseString.put(STATE, STATE_SUCCESS)
        successfulResponseString.put(STATUSES, [defaultContextStatus])
        GitHubResponse successfulPrResponse = new GitHubResponse(mockConnention, successfulResponseString)

        Mockito.when(gitHubClient.get(requestCaptor.capture())).thenReturn(successfulPrResponse)

        PullRequestMarker successfulPrMarker = new PullRequestMarker().setSha('cha123').setLabel('repo:successBranch')
        Mockito.when(pullRequest.getHead()).thenReturn(successfulPrMarker)

        GitMergeState actualState = pullRequestStatusService.getMergeState(repositoryId, successfulPrMarker)

        GitHubRequest request = requestCaptor.getValue()
        assertEquals("/repos/$OWNER/repo/commits/cha123/status".toString(), request.getUri())

        assertEquals('successBranch', actualState.getBranchToMerge())
        assertEquals('', actualState.getDescription())
        Assert.assertTrue(actualState.isMergeable())
        Assert.assertFalse(actualState.isBuildPending())

        Mockito.verify(gitHubClient).get(request)
        Mockito.verifyNoMoreInteractions(gitHubClient)
    }

    @Test
    public void testGetCommitMergeStateOnPendingDefaultBuild() {
        StringMap<?> successfulResponseString = new StringMap<>()
        successfulResponseString.put(STATE, STATE_SUCCESS)
        GitHubResponse successfulPrResponse = new GitHubResponse(mockConnention, successfulResponseString)

        Mockito.when(gitHubClient.get(requestCaptor.capture())).thenReturn(successfulPrResponse)

        PullRequestMarker successfulPrMarker = new PullRequestMarker().setSha('cha123').setLabel('repo:successBranch')
        Mockito.when(pullRequest.getHead()).thenReturn(successfulPrMarker)

        GitMergeState actualState = pullRequestStatusService.getMergeState(repositoryId, successfulPrMarker)

        GitHubRequest request = requestCaptor.getValue()
        assertEquals("/repos/$OWNER/repo/commits/cha123/status".toString(), request.getUri())

        Assert.assertNull(actualState.getBranchToMerge())
        Assert.assertFalse(actualState.isMergeable())
        Assert.assertTrue(actualState.isBuildPending())

        Mockito.verify(gitHubClient).get(request)
        Mockito.verifyNoMoreInteractions(gitHubClient)
    }

    @Test
    public void testGetCommitMergeStateOnDefaultContextOnlySuccess() {
        StringMap<?> defaultContextStatus = createResponseStatusStringMap(1, DEFAULT_BUILD_CONTEXT, STATE_SUCCESS, 'buildUrl')
        StringMap<?> otherFailingStatus = createResponseStatusStringMap(2, 'do not care', STATE_FAILURE, 'buildUrl')
        StringMap<?> combinedResponseString = new StringMap<>()
        combinedResponseString.put(STATUSES, [otherFailingStatus, defaultContextStatus])
        combinedResponseString.put(STATE, STATE_FAILURE)
        GitHubResponse combinedPrResponse = new GitHubResponse(mockConnention, combinedResponseString)

        Mockito.when(gitHubClient.get(requestCaptor.capture())).thenReturn(combinedPrResponse)

        PullRequestMarker successfulPrMarker = new PullRequestMarker().setSha('cha123').setLabel('repo:successBranch')
        Mockito.when(pullRequest.getHead()).thenReturn(successfulPrMarker)

        GitMergeState actualState = pullRequestStatusService.getMergeState(repositoryId, successfulPrMarker, true)

        GitHubRequest request = requestCaptor.getValue()
        assertEquals("/repos/$OWNER/repo/commits/cha123/status".toString(), request.getUri())

        assertEquals('successBranch', actualState.getBranchToMerge())
        assertEquals('', actualState.getDescription())
        Assert.assertTrue(actualState.isMergeable())
        Assert.assertFalse(actualState.isBuildPending())

        Mockito.verify(gitHubClient).get(request)
        Mockito.verifyNoMoreInteractions(gitHubClient)
    }

    @Test
    public void testGetCommitMergeStateOnPrFailure() {
        final String state = 'any_non_successful_state'
        StringMap<?> unsuccessfulResponseString = createResponseStatusStringMap(3, DEFAULT_BUILD_CONTEXT, state, 'buildUrl')
        StringMap<?> combinedResponseString = new StringMap<>()
        combinedResponseString.put(STATUSES, [unsuccessfulResponseString])
        combinedResponseString.put(STATE, state)
        GitHubResponse combinedPrResponse = new GitHubResponse(mockConnention, combinedResponseString)
        Mockito.when(gitHubClient.get(requestCaptor.capture())).thenReturn(combinedPrResponse)

        PullRequestMarker unsuccessfulPrMarker = new PullRequestMarker().setSha('cha123').setLabel('repo:failBranch')
        Mockito.when(pullRequest.getHead()).thenReturn(unsuccessfulPrMarker)

        GitMergeState actualState = pullRequestStatusService.getMergeState(repositoryId, unsuccessfulPrMarker)

        GitHubRequest request = requestCaptor.getValue()
        assertEquals("/repos/$OWNER/repo/commits/cha123/status".toString(), request.getUri())

        Assert.assertNull(actualState.getBranchToMerge())
        assertEquals('commit status "any_non_successful_state"', actualState.getDescription())
        Assert.assertFalse(actualState.isMergeable())
        Assert.assertFalse(actualState.isBuildPending())

        Mockito.verify(gitHubClient).get(request)
        Mockito.verifyNoMoreInteractions(gitHubClient)
    }

    @Test
    public void testGetCommitMergeStateOnPrBuildHasntStarted() {
        StringMap<?> unsuccessfulResponseString = createResponseStatusStringMap(5, DEFAULT_BUILD_CONTEXT, STATE_PENDING, '')
        StringMap<?> combinedResponseString = new StringMap<>()
        combinedResponseString.put(STATUSES, [unsuccessfulResponseString])
        combinedResponseString.put(STATE, STATE_PENDING)
        GitHubResponse combinedPrResponse = new GitHubResponse(mockConnention, combinedResponseString)

        Mockito.when(gitHubClient.get(requestCaptor.capture())).thenReturn(combinedPrResponse)

        PullRequestMarker pendingPrMarker = new PullRequestMarker().setSha('cha123').setLabel('repo:prBranch')
        Mockito.when(pullRequest.getHead()).thenReturn(pendingPrMarker)

        GitMergeState actualState = pullRequestStatusService.getMergeState(repositoryId, pendingPrMarker)

        GitHubRequest request = requestCaptor.getValue()
        assertEquals("/repos/$OWNER/repo/commits/cha123/status".toString(), request.getUri())

        Assert.assertNull(actualState.getBranchToMerge())
        assertEquals('commit status "pending"', actualState.getDescription())
        Assert.assertFalse(actualState.isMergeable())
        Assert.assertTrue(actualState.isBuildPending())

        Mockito.verify(gitHubClient).get(request)
        Mockito.verifyNoMoreInteractions(gitHubClient)
    }

    @Test
    public void testUpdatePullRequestStatus() {
        String expectedUri = "/repos/$OWNER/repo/statuses/c00c0aa6e4607ff8e7481f7599fe1502388f209c".toString()

        PullRequestMarker unsuccessfulPrMarker = new PullRequestMarker().setSha('c00c0aa6e4607ff8e7481f7599fe1502388f209c').setLabel('repo:branch')
        Mockito.when(pullRequest.getHead()).thenReturn(unsuccessfulPrMarker)

        String context = 'review context'
        String description = 'description'
        String state = 'state'
        String targetUrl = 'targetUrl'

        pullRequestStatusService.updatePullRequestStatus(repositoryId, pullRequest, context, description, state, targetUrl)

        Mockito.verify(gitHubClient).post(Mockito.eq(expectedUri), paramsCaptor.capture(), Mockito.eq(CommitStatus.class))

        Map<String, String> params = paramsCaptor.value
        Assert.assertEquals(4, params.size())
        assertEquals(context, params.get(CONTEXT))
        assertEquals(state, params.get(STATE))
        assertEquals(description, params.get(DESCRIPTION))
        assertEquals(targetUrl, params.get(TARGET_URL))
    }

    static StringMap<?> createResponseStatusStringMap(int id, String context, String state, String targetUrl) {
        StringMap<?> responseString = new StringMap<>()
        responseString.put(ID, id)
        responseString.put(CONTEXT, context)
        responseString.put(STATE, state)
        responseString.put(TARGET_URL, targetUrl)
        responseString.put(UPDATED_AT, DATE)
        responseString.put(CREATED_AT, DATE)
        responseString
    }
}
