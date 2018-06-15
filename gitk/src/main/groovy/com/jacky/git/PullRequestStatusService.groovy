package com.jacky.git

import com.google.gson.internal.StringMap
import org.eclipse.egit.github.core.CommitStatus
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.PagedRequest
import org.eclipse.egit.github.core.service.GitHubService

import java.util.function.Supplier

import static com.jacky.git.PullRequestStatus.*
import static org.eclipse.egit.github.core.CommitStatus.STATE_PENDING
import static org.eclipse.egit.github.core.CommitStatus.STATE_SUCCESS
import static org.eclipse.egit.github.core.client.IGitHubConstants.*

/**
 * User: oriy
 * Date: 20/03/2017
 */
class PullRequestStatusService extends GitHubService {

    public static final String BUILD_SUCCESS_DESCRIPTION = 'Build almost finished'
    public static final String DEFAULT_BUILD_CONTEXT = 'default'
    public static final String SEGMENT_STATUS = "/status"
    public static final String STATUSES = "statuses"

    public PullRequestStatusService(GitHubClient client) {
        super(client)
    }

    public StringMap getCombinedCommitStatus(IRepositoryIdProvider repositoryId, PullRequestMarker pullRequestHead) {
        return getCombinedCommitStatus(repositoryId, pullRequestHead.getSha())
    }

    public StringMap getCombinedCommitStatus(IRepositoryIdProvider repositoryId, String pullRequestCommitSha) {

        // https://developer.github.com/v3/repos/statuses/#get-the-combined-status-for-a-specific-ref
        // GET /repos/:owner/:repo/commits/:ref/status

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS)
        uri.append('/').append(repositoryId.generateId())
                .append(SEGMENT_COMMITS)
                .append('/').append(pullRequestCommitSha)
                .append(SEGMENT_STATUS)

        PagedRequest<CommitStatus> request = new PagedRequest()
        request.setType(StringMap.class)
        request.setUri(uri)
        (StringMap) getClient().get(request).getBody()
    }

    public GitMergeState getMergeState(IRepositoryIdProvider repositoryId, PullRequestMarker pullRequestHead) {
        getMergeState(repositoryId, pullRequestHead, false)
    }

    public GitMergeState getMergeState(IRepositoryIdProvider repositoryId, PullRequestMarker pullRequestHead, boolean defaultOnly) {
        StringMap combinedCommitStatusResponse = getCombinedCommitStatus(repositoryId, pullRequestHead)
        String commitState = combinedCommitStatusResponse.get(STATE)
        Optional<PullRequestStatus> optionalDefaultPullRequestStatus = getOptionalDefaultPullRequestStatus(combinedCommitStatusResponse)
        if (!optionalDefaultPullRequestStatus.isPresent()) {
            return new GitMergeState(false, "commit status \"$STATE_PENDING\"", true)
        }
        if (defaultOnly) {
            commitState = optionalDefaultPullRequestStatus.get().getState()
        }
        if (STATE_SUCCESS != commitState) {
            boolean buildPending = (optionalDefaultPullRequestStatus.get().getTargetUrl() == null
                                     || optionalDefaultPullRequestStatus.get().getTargetUrl().isEmpty())
            return new GitMergeState(false, "commit status \"$commitState\"", buildPending)
        }
        return new GitMergeState(pullRequestHead)
    }

    public CommitStatus updatePullRequestStatus(IRepositoryIdProvider repositoryId, PullRequest pullRequest, String context, String description, String state, String targetUrl) {

        // https://developer.github.com/v3/repos/statuses/#create-a-status
        // POST /repos/:owner/:repo/statuses/:sha

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS)
                .append('/').append(repositoryId.generateId())
                .append(SEGMENT_STATUSES)
                .append('/').append(pullRequest.getHead().getSha())

        Map<String, String> params = new HashMap<String, String>(4, 1)
        params.put(CONTEXT, context)
        params.put(DESCRIPTION, description)
        params.put(STATE, state)
        params.put(TARGET_URL, targetUrl)
        getClient().post(uri.toString(), params, CommitStatus.class)
    }

    public String getPullRequestBuildUrl(IRepositoryIdProvider repositoryId, PullRequestMarker pullRequestHead) {
        StringMap combinedCommitStatusResponse = getCombinedCommitStatus(repositoryId, pullRequestHead)
        Optional<PullRequestStatus> optionalDefaultPullRequestStatus = getOptionalDefaultPullRequestStatus(combinedCommitStatusResponse)
        if (optionalDefaultPullRequestStatus.isPresent()) {
            return optionalDefaultPullRequestStatus.get().getTargetUrl()
        }
        return '.'
    }

    static Optional<PullRequestStatus> getOptionalDefaultPullRequestStatus(StringMap combinedCommitStatusResponse) {
        List<StringMap> statusMaps = (List<StringMap>) combinedCommitStatusResponse.get(STATUSES)
        for (StringMap stringMap : statusMaps) {
            PullRequestStatus pullRequestStatus = new PullRequestStatus(stringMap)
            if (DEFAULT_BUILD_CONTEXT == pullRequestStatus.getContext()) {
                return Optional.of(pullRequestStatus)
            }
        }
        return Optional.empty()
    }

    public CommitStatus updatePullRequestStatus(IRepositoryIdProvider repositoryId, PullRequest pullRequest, String context, String description, String state, Optional<String> optionalTargetUrl) {
        String targetUrl = optionalTargetUrl.orElseGet(new Supplier<String>() {
            String get() {
                return getPullRequestBuildUrl(repositoryId, pullRequest.getHead())
            }
        })
        println "setting git status '$state' for context '$context'"
        updatePullRequestStatus(repositoryId, pullRequest, context, description, state, targetUrl + '#liquik')
    }
}
