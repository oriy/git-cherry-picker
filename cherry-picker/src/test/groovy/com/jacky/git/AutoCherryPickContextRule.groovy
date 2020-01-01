package com.jacky.git

import org.junit.rules.ExternalResource

import static com.jacky.git.GitHubUtil.OWNER
import static com.jacky.git.GitHubUtil.TEST_REPOSITORY

/**
 * Created by
 * User: Oriy
 * Date: 09/07/2016.
 */
class AutoCherryPickContextRule extends ExternalResource {

    AutoCherryPickContext context

    @Override
    protected void before() throws Throwable {
        context = new AutoCherryPickContext(OWNER, TEST_REPOSITORY, 'origin/from:origin/to', 'test@mail.com', false, true)
    }
}
