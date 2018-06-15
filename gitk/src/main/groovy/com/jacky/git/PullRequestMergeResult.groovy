package com.jacky.git

import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.PullRequest

/**
 * User: oriy
 * Date: 14/08/2017
 */
class PullRequestMergeResult {

    List<Issue> openIssues = []
    List<PullRequest> openPullRequests = []

    public addOpenIssue(Issue issue) {
        openIssues.add(issue)
    }

    public addOpenPullRequest(PullRequest openPullRequest) {
        openPullRequests.add(openPullRequest)
    }

    boolean hasOpenIssues() {
        !openIssues.isEmpty()
    }

    boolean hasOpenPullRequests() {
        !openPullRequests.isEmpty()
    }

    static PullRequestMergeResult empty() {
        return new PullRequestMergeResult()
    }
}
