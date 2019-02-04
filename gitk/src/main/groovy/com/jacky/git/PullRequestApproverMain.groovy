package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider

import static com.jacky.git.GitHubUtil.createRepositoryId

/**
 * User: oriy
 * Date: 23/04/2017
 */
class PullRequestApproverMain {

    public static void main(String[] args) {
        CliBuilder cli = new CliBuilder(usage: 'PullRequestApproverMain')
        cli.h(longOpt: 'help', 'prints this message')
        cli.r(longOpt: 'repo', args: 1, 'repository name [REQUIRED] ')
        cli.pr(longOpt: 'pull-request', args: 1, 'Pull-Request-id [REQUIRED]')
        OptionAccessor options = cli.parse(args)
        if (!options || !options.r || !options.pr) {
            cli.usage()
            return
        }
        String repoName = options.r

        IRepositoryIdProvider repositoryId = createRepositoryId(repoName)

        println "handling $repositoryId git repository"

        PullRequestReviewsService pullRequestReviewsService = new PullRequestReviewsService(GitHubUtil.createApproverGitHubClients())

        int pullRequestId = String.valueOf(options.pr).toInteger()
        println "* pull request id $pullRequestId"

        pullRequestReviewsService.approvePullRequest(repositoryId, pullRequestId)
    }
}
