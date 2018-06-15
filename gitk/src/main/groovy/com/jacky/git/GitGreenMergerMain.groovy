package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequest
import org.eclipse.egit.github.core.client.GitHubClient

import static com.jacky.git.GitHubUtil.createGitHubClient
import static com.jacky.git.GitHubUtil.createRepositoryId

/**
 * User: oriy
 * Date: 23/04/2017
 */
class GitGreenMergerMain {

    public static void main(String[] args) {
        CliBuilder cli = new CliBuilder(usage: 'GitGreenMergerMain')
        cli.h(longOpt: 'help', 'prints this message')
        cli.r(longOpt: 'repo', args: 1, 'repository name [REQUIRED] ')
        cli.pr(longOpt: 'pull-request', args: 1, 'Pull-Request-id [OPTIONAL]')
        cli.l(longOpt: 'label', args: 1, 'Pull-Request label to handle [OPTIONAL]')
        cli.n(longOpt: 'default-only', 'check default context status only')
        cli.d(longOpt: 'dryrun', 'dry run mode')
        OptionAccessor options = cli.parse(args)
        if (!options || !options.r || (!options.pr && !options.l )) {
            cli.usage()
            return
        }
        String repoName = options.r
        boolean defaultOnly = options.n
        boolean dryRun = options.d

        GitCommandExecutor gitExec = new GitCommandExecutor().verbose(true)
        GitHubUtil.setRepoGitConfig(gitExec)
        GitHubClient gitHubClient = createGitHubClient()
        IRepositoryIdProvider repositoryId = createRepositoryId(repoName)

        println "handling $repositoryId git repository ${dryRun ? '[DRY-RUN]' : ''}"

        GitGreenMerger gitGreenMerger = new GitGreenMerger(gitExec, gitHubClient, repositoryId)

        if (options.pr) {
            int pullRequestId = String.valueOf(options.pr).toInteger()
            println "* pull request id $pullRequestId"

            PullRequest pullRequest = gitGreenMerger.getPullRequest(pullRequestId)
            gitGreenMerger.markDefaultPullRequestBuildSuccess(pullRequest, dryRun)

            gitGreenMerger.handlePullRequest(pullRequest, defaultOnly, dryRun)
        }
        else {
            String label = options.l
            println "* pull requests with label '$label'"

            gitGreenMerger.mergeAllMergable(label, defaultOnly, dryRun)
        }
    }
}
