package com.jacky.git

import org.eclipse.egit.github.core.PullRequestMarker
import org.junit.Test

import static com.jacky.git.ConfigurationUtil.configuration
import static com.jacky.git.GitHubUtil.OWNER
import static com.jacky.git.GitHubUtil.THIS_REPOSITORY
import static org.junit.Assert.assertEquals

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitHubUtilTest {

    @Test
    void getProjectNameFromUrlTest() {
        String userPass = "$configuration.gitUserName:$configuration.gitUserPass"
        def projectName = GitHubUtil.getProjectNameFromUrl("https://$userPass@github.com/$OWNER/git-cherry-picker.git")
        assertEquals(THIS_REPOSITORY, projectName)
    }

    @Test
    public void testExtractBranchName() {
        PullRequestMarker pullRequestMarker = new PullRequestMarker().setLabel("user:branch")
        assertEquals("branch", GitHubUtil.extractBranchName(pullRequestMarker))
    }
}
