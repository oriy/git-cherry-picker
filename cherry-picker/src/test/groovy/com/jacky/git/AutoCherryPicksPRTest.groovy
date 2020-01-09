package com.jacky.git

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.jacky.git.AutoCherryPicksPR.shouldCommitBeCherryPicked
import static com.jacky.git.GitHubUtil.*
import static org.junit.Assert.*

class AutoCherryPicksPRTest {

    AutoCherryPicksPR autoCherryPicksPullRequest

    @Rule
    public GitTestRule gitTestRule = new GitTestRule(TEST_REPOSITORY)

    @Rule
    public AutoCherryPickContextRule contextRule = new AutoCherryPickContextRule()

    File tmpDir
    File repoDir
    AutoCherryPickContext context

    @Before
    void setup() {
        context = contextRule.context
        autoCherryPicksPullRequest = new AutoCherryPicksPR(context)
        tmpDir = gitTestRule.root
        repoDir = gitTestRule.repoDir
    }

    @After
    void removeBranch() {
        if (gitTestRule.repoDir.exists()) {
            autoCherryPicksPullRequest.gitExec.gitDeleteRemoteBranch(TEST_COMMIT_BRANCH)
        }
    }

    @Test
    public void testShouldCommitBeCherryPickedReturnsTrue() throws Exception {
        CherryPicksResult cherryPicksResult = generateCherryPickResult("this is a regular commit message")
        assertTrue(shouldCommitBeCherryPicked(cherryPicksResult.commitHash, cherryPicksResult.commitMessage))
    }

    @Test
    public void testShouldCommitBeCherryPickedReturnsFalseForNoCherryPickPhrase() throws Exception {
        CherryPicksResult cherryPicksResult = generateCherryPickResult("this commit message will not be cherry picked \n\n no-cherry-pick")
        assertFalse(shouldCommitBeCherryPicked(cherryPicksResult.commitHash, cherryPicksResult.commitMessage))
    }

    private static CherryPicksResult generateCherryPickResult(String commitMessage) {
        CherryPicksResult cherryPicksResult = new CherryPicksResult(
                existInBranch: true,
                commitHash: "abba7868768",
                commitMessage: commitMessage)
        cherryPicksResult
    }

    @Test
    public void testAutoCherryPickPullRequestOnConflict() throws Exception {
        boolean shouldSendEmail = autoCherryPicksPullRequest.runAutoCherryPicks(context.repositoryUrl, 'origin/test-cherry', 'origin/master', tmpDir.getAbsolutePath(), false, context.dryRun)

        assertTrue(shouldSendEmail || context.dryRun)
        assertEquals('<li><font color=\'#dd0000\'> <b><a href=\'https://github.com/oriy/cherry-playground/issues/2\'>CREATED ISSUE</a>' +
                " - CONFLICT</b> merging commit: <b>$TEST_COMMIT_SHA</b><br/>" +
                ' User: <b>oriy</b>, Author: Ori Yechieli <b>ori.yechieli@gmail.com</b>;<br/>' +
                ' Message: <b>third commit</b>;<br/><b>Please handle manually!</b>' +
                '</font></li>\n<br/><br/>', autoCherryPicksPullRequest.gitMergeMail.getBody())
    }
}
