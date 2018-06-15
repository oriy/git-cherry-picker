package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.client.GitHubClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

import static com.jacky.git.GitHubUtil.OWNER

/**
 * User: oriy
 * Date: 08/05/2017
 */
@RunWith(MockitoJUnitRunner.class)
class GitKDataServiceTest {

    static final IRepositoryIdProvider repositoryId = GitHubUtil.createRepositoryId('repo')

    GitKDataService gitKDataService

    @Mock
    GitHubClient gitHubClient

    @Before
    public void init() {
        gitKDataService = new GitKDataService(gitHubClient)
    }

    @Test
    public void testDeleteReference() {
        gitKDataService.deleteReference(repositoryId, 'myRef')

        String expectedUri = "/repos/$OWNER/repo/git/refs/heads/myRef"
        Mockito.verify(gitHubClient).delete(Mockito.eq(expectedUri))
    }
}
