package com.jacky.git

import org.junit.rules.TemporaryFolder

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class GitTestRule extends TemporaryFolder {

    final String repoName
    File repoDir

    public GitTestRule(String repoName) {
        this.repoName = repoName
    }

    @Override
    protected void before() throws Throwable {
        super.before()
        repoDir = newFolder(repoName)
    }
}
