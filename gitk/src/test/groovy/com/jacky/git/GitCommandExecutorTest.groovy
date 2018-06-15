package com.jacky.git

import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.jacky.git.GitHubUtil.TEST_COMMIT_SHA
import static com.jacky.git.GitHubUtil.TEST_REPOSITORY
import static com.jacky.git.GitHubUtil.TEST_REPOSITORY_URL
import static org.junit.Assert.assertTrue

/**
 * Created by
 * User: Oriy
 * Date: 10/07/2016.
 */
class GitCommandExecutorTest {

    GitCommandExecutor gitCommandExecutor

    @Rule
    public GitTestRule gitTestRule = new GitTestRule(TEST_REPOSITORY)

    File tmpDir
    File repoDir

    @Before
    void setup() {
        tmpDir = gitTestRule.root
        repoDir = gitTestRule.repoDir
        gitCommandExecutor = new GitCommandExecutor().repoDir(tmpDir).discardOutput(false)
        gitCommandExecutor.gitClone(TEST_REPOSITORY_URL)
    }

    @Test
    void cloneFromUrlTest() {
        assertTrue(new File(tmpDir.getAbsolutePath() + '/' + TEST_REPOSITORY, 'README.md').exists())
    }

    @Test
    void getLogDiffTest() {
        gitCommandExecutor.repoDir(repoDir)
        def result = gitCommandExecutor.gitCherryDiff('origin/master', 'origin/test-cherry')
        assertTrue(result.output.startsWith("+ $TEST_COMMIT_SHA third commit"))
    }
}
