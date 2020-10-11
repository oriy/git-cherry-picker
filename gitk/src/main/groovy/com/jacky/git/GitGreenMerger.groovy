package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.IssueService

import static com.jacky.git.IssueServiceHelper.getOpenIssuesByLabel
import static com.jacky.git.PullRequestStatusService.BUILD_SUCCESS_DESCRIPTION
import static com.jacky.git.PullRequestStatusService.DEFAULT_BUILD_CONTEXT
import static org.eclipse.egit.github.core.CommitStatus.STATE_SUCCESS

/**
 * Created by
 * User: Oriy
 * Date: 06/03/2017.
 */
class GitGreenMerger {
    public static final String RUN_TESTS = 'run tests'

    final GitCommandExecutor gitExec
    final GitHubClient gitHubClient
    final PullRequestMergeService pullRequestMergeService
    final PullRequestStatusService pullRequestStatusService
    final IssueService issueService
    final GitKDataService gitKDataService
    final IRepositoryIdProvider repositoryId

    public GitGreenMerger(GitCommandExecutor gitExec, GitHubClient gitHubClient, IRepositoryIdProvider repositoryId) {
        this(gitExec, gitHubClient, new PullRequestMergeService(gitHubClient),
             new PullRequestStatusService(gitHubClient), new IssueService(gitHubClient),
             new GitKDataService(gitHubClient), repositoryId)
    }

    GitGreenMerger(GitCommandExecutor gitExec, GitHubClient gitHubClient, PullRequestMergeService pullRequestMergeService,
                   PullRequestStatusService pullRequestStatusService, IssueService issueService,
                   GitKDataService gitKDataService, IRepositoryIdProvider repositoryId) {
        this.gitExec = gitExec
        this.gitHubClient = gitHubClient
        this.pullRequestMergeService = pullRequestMergeService
        this.pullRequestStatusService = pullRequestStatusService
        this.issueService = issueService
        this.gitKDataService = gitKDataService
        this.repositoryId = repositoryId
    }

    public PullRequestMergeResult mergeAllMergable(String label, boolean defaultOnly, boolean dryRun) {
        List<Issue> openGitHubIssues = getOpenIssuesByLabel(issueService, repositoryId, label)
        if (!openGitHubIssues.isEmpty()) {
            gitExec.gitPull()
            return handleOpenPullRequests(openGitHubIssues.reverse(), defaultOnly, dryRun)
        } else {
            println('Found no PR merge candidates')
            return PullRequestMergeResult.empty()
        }
    }

    PullRequest getPullRequest(int prNumber) {
        pullRequestMergeService.getPullRequest(repositoryId, prNumber)
    }

    PullRequestMergeResult handleOpenPullRequests(List<Issue> openIssues, boolean defaultOnly, boolean dryRun) {
        int count = openIssues.size()
        PullRequestMergeResult pullRequestMergeResult = new PullRequestMergeResult()
        println("handling $count PRs")
        openIssues.eachWithIndex { issue, index ->
            int prNumber = issue.getNumber()
            print("[${index+1} / $count] ")
            PullRequest pullRequest = issue.getPullRequest()
            if (pullRequest != null && pullRequest.getUrl() != null) {
                println(" > handling PR $prNumber")
                pullRequest = getPullRequest(prNumber)
                handlePullRequest(pullRequest, defaultOnly, dryRun)
                if (!pullRequest.isMerged()) {
                    pullRequestMergeResult.addOpenPullRequest(pullRequest)
                }
            } else {
                println(" > skipping issue $prNumber")
                pullRequestMergeResult.addOpenIssue(issue)
            }
        }
        pullRequestMergeResult
    }

    public void markDefaultPullRequestBuildSuccess(PullRequest pullRequest, dryRun) {
        if (dryRun) {
            println('PR ' + pullRequest.number + ' default build status set \'Success\'')
        } else {
            pullRequestStatusService.updatePullRequestStatus(repositoryId, pullRequest, DEFAULT_BUILD_CONTEXT, BUILD_SUCCESS_DESCRIPTION, STATE_SUCCESS, pullRequest.getUrl())
        }
    }

    public void handlePullRequest(PullRequest pullRequest, boolean defaultOnly, boolean dryRun) {
        int prNumber = pullRequest.number
        GitMergeState mergeState = getPullRequestMergeState(repositoryId, pullRequest, defaultOnly)
        if (mergeState.isMergeable()) {
            println('PR ' + prNumber + ' is mergeable')
            if (dryRun) {
                println "Merging PR " + prNumber
            } else {
                try {
                    pullRequestMergeService.merge(repositoryId, prNumber, "Auto-merged", PullRequestMergeService.MergeMethod.SQUASH)
                    println "Auto-Merged PR " + prNumber
                    try {
                        gitKDataService.deleteReference(repositoryId, pullRequest.getHead().getRef())
                    }
                    catch (Exception e) {
                        println "FAILED deleting merged branch " + pullRequest.getHead().getRef()
                        println "error: " + e
                    }
                    gitExec.gitPull('--prune --progress')
                    pullRequest.setMerged(true)
                }
                catch (Exception e) {
                    println "FAILED merging PR " + prNumber
                    println "error: " + e
                }
            }
        } else {
            println('PR ' + prNumber + ' state: ' + mergeState.getDescription())
            if (mergeState.isBuildPending()) {
                println('PR ' + prNumber + ' ' + RUN_TESTS)
                issueService.createComment(repositoryId, prNumber, RUN_TESTS)
            }
        }
        println('')
    }

    GitMergeState getPullRequestMergeState(IRepositoryIdProvider repositoryId, PullRequest pullRequest, boolean defaultOnly) {
        PullRequestMarker pullRequestHead = pullRequest.getHead()

        if (pullRequest.getChangedFiles() == 0) {
            return new GitMergeState(pullRequestHead)
        }
        if (!pullRequest.getUser().getLogin().equalsIgnoreCase(gitHubClient.getUser())) {
            return new GitMergeState(false, "Auto-merge is only available for ${gitHubClient.getUser()}", false)
        }

        if (pullRequest.isMerged()) {
            return new GitMergeState(false, "already merged", false)
        }

        return pullRequestStatusService.getMergeState(repositoryId, pullRequestHead, defaultOnly)
    }
}
