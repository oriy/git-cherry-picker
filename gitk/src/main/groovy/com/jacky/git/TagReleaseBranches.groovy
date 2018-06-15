package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.RepositoryBranch
import org.eclipse.egit.github.core.TypedResource
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.RepositoryService

import java.util.regex.Pattern

import static com.jacky.git.GitHubUtil.createGitHubClient
import static com.jacky.git.GitHubUtil.createRepositoryId

/**
 * User: oriy
 * Date: 12/05/2017
 */
class TagReleaseBranches {

    static final Map<String, Pattern> REPO_RELEASE_BRANCHES =
        [
                'social':Pattern.compile('v[0-9]+'),
                'search':Pattern.compile('release-[0-9]+\\.1\\.0'),
                'javascript':Pattern.compile('release-[0-9]+\\.1\\.0')
        ]

    public static void main(String[] args) {
        CliBuilder cli = new CliBuilder(usage: 'GitGreenMergerMain')
        cli.h(longOpt: 'help', 'prints this message')
        cli.r(longOpt: 'repo', args: 1, 'repository name [REQUIRED] ')
        OptionAccessor options = cli.parse(args)
        if (!options || !options.r) {
            cli.usage()
            return
        }
        String repoName = options.r.toLowerCase()

        if (!REPO_RELEASE_BRANCHES.containsKey(repoName)) {
            cli.usage()
            return
        }
        Pattern releaseBranchNamePattern = REPO_RELEASE_BRANCHES.get(repoName)

        GitCommandExecutor gitExec = new GitCommandExecutor().verbose(true)
        GitHubUtil.setRepoGitConfig(gitExec)
        GitHubClient gitHubClient = createGitHubClient()
        IRepositoryIdProvider repositoryId = createRepositoryId(repoName)

        gitExec.gitCheckout('master')
        gitExec.gitPull()

        RepositoryService repositoryService = new RepositoryService(gitHubClient)
        List<RepositoryBranch> branches = repositoryService.getBranches(repositoryId)
        for (RepositoryBranch branch : branches) {
            String branchName = branch.getName()
            if (releaseBranchNamePattern.matcher(branchName).matches()) {
                println "HANDLING $branchName"
                TypedResource commitResource = branch.getCommit()
                String commitSha = commitResource.getSha()
                gitExec.execute(new GitCommand("git tag --force $branchName $commitSha"))
            }
            else {
                println "skipping $branchName"
            }
        }
        gitExec.execute(new GitCommand('git push --tags'))
    }
}
