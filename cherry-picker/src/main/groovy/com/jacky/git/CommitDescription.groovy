package com.jacky.git

import groovy.transform.EqualsAndHashCode
import org.eclipse.egit.github.core.CommitUser
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User

/**
 * Created by
 * User: Oriy
 * Date: 24/05/2017.
 */
@EqualsAndHashCode
class CommitDescription {
    final String commitHash
    final User user
    final String userName
    final String userEmail
    final String commitUserName
    final String commitMessage

    public CommitDescription(CherryPicksResult commit, RepositoryCommit repositoryCommit) {
        this.commitHash = commit.commitHash
        this.user = repositoryCommit.getAuthor()
        CommitUser commitUser = repositoryCommit.getCommit().getAuthor()
        this.commitUserName = commitUser.getName()
        this.userName = (user != null) ? user.getLogin() : commitUserName
        this.userEmail = commitUser.getEmail().toLowerCase()
        this.commitMessage = commit.commitMessage
    }

    public CommitDescription(PullRequest pullRequest, RepositoryCommit repositoryCommit) {
        this.commitHash = pullRequest.getHead().getSha()
        this.user = repositoryCommit.getAuthor()
        CommitUser commitUser = repositoryCommit.getCommit().getAuthor()
        this.commitUserName = commitUser.getName()
        this.userName = (user != null) ? user.getLogin() : commitUserName
        this.userEmail = commitUser.getEmail().toLowerCase()
        this.commitMessage = pullRequest.getTitle()
    }
}
