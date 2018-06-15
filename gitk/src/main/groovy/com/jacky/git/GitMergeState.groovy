package com.jacky.git

import groovy.transform.EqualsAndHashCode
import org.eclipse.egit.github.core.PullRequestMarker

/**
 * Created by
 * User: Oriy
 * Date: 06/03/2017.
 */
@EqualsAndHashCode
class GitMergeState {
    final boolean mergeable
    final String branchToMerge
    final String description
    final boolean buildPending

    GitMergeState(boolean mergeable, String description, boolean buildPending) {
        this.mergeable = mergeable
        this.branchToMerge = null
        this.description = description
        this.buildPending = buildPending
    }

    GitMergeState(PullRequestMarker head) {
        this.mergeable = true
        this.buildPending = false
        this.branchToMerge = GitHubUtil.extractBranchName(head)
        this.description = ''
    }
}
