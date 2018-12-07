package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.PullRequestMarker
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.RepositoryId
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.util.UrlUtils

import static com.jacky.git.ConfigurationUtil.decryptPass
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_ISSUES

/**
 * Created by
 * User: Oriy
 * Date: 08/07/2016.
 */
class GitHubUtil {

    private static final Configuration configuration = ConfigurationUtil.configuration

    public static String THIS_REPOSITORY = 'git-cherry-picker'

    public static String REPOSITORIES_DIR = 'repos'
    public static String OWNER = configuration.organization
    public static String TEST_REPOSITORY = "cherry-playground"
    public static String TEST_REPOSITORY_URL = UrlUtils.createRemoteHttpsUrl(createRepositoryId(OWNER, TEST_REPOSITORY), "")

    public static String TEST_COMMIT_SHA = 'b6842539c0f6ce10e98c85c9fc4e97f29e777c5b'
    public static String TEST_COMMIT_BRANCH = "cp_$TEST_COMMIT_SHA"

    public static String CHERRY_PICK_TITLE = 'cherry pick'
    public static String CHERRY_PICK_BODY = 'Auto :cherries: pick'

    public static String getGitHubIssueUrl(IRepositoryIdProvider repositoryId, int prNumber) {
        "https://github.com/${repositoryId.generateId()}${SEGMENT_ISSUES}/${prNumber}"
    }

    public static IRepositoryIdProvider createRepositoryId(String owner = OWNER, String repoName) {
        RepositoryId.create(owner, repoName)
    }

    private static String decryptGitUserPass() {
        decryptPass(configuration.gitUserPass)
    }

    public static GitHubClient createGitHubClient() {
        createGitHubClient(configuration.gitUserName, decryptGitUserPass())
    }

    public static GitHubClient createGitHubClient(String userName, String password) {
        new GitHubClient().setCredentials(userName, password)
    }

    public static String getRepoUrl(String owner = OWNER, String repoName) {
        getRepoUrl(createRepositoryId(owner, repoName))
    }

    public static String getRepoUrl(IRepositoryIdProvider repositoryId) {
        String userPass = String.format("%s:%s", configuration.gitUserName, decryptGitUserPass())
        UrlUtils.createRemoteHttpsUrl(repositoryId, userPass)
    }

    public static String getProjectNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.indexOf(".git"))
    }

    public static String extractBranchName(PullRequestMarker head) {
        String branchNameWithPrefix = head.getLabel()
        return branchNameWithPrefix.substring(branchNameWithPrefix.lastIndexOf(':') + 1)
    }

    public static RepositoryCommit getRepositoryCommit(IRepositoryIdProvider repositoryId, String sha) {
        GitHubClient client = createGitHubClient()
        CommitService service = new CommitService(client)
        return service.getCommit(repositoryId, sha)
    }

    public static void setRepoGitConfig(GitCommandExecutor gitExec) {
        gitExec.gitConfigLocal('user.name', configuration.gitUserName)
        gitExec.gitConfigLocal('user.email', configuration.gitUserEmail)
    }

    public static Map<String, String> createMapFromStringArg(String sources) {
        def sourceMap = [:]
        def keyValueStr = sources.split(';')
        def from = ""
        def to = ""
        keyValueStr.each {
            it.split(',').eachWithIndex { branch, i ->
                if (i % 2 == 0) {
                    from = branch
                } else {
                    to = branch
                }
            }
            sourceMap[from] = to
        }
        return sourceMap
    }
}
