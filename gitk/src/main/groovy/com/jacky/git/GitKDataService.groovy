package com.jacky.git

import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.DataService

import static org.eclipse.egit.github.core.client.IGitHubConstants.*

/**
 * User: oriy
 * Date: 08/05/2017
 */
class GitKDataService extends DataService {

    public static final String SEGMENT_HEADS = "/heads"

    public GitKDataService(GitHubClient client) {
        super(client)
    }

    public void deleteReference(IRepositoryIdProvider repository, String ref) throws IOException {

        // https://developer.github.com/v3/git/refs/#delete-a-reference
        // DELETE /repos/:owner/:repo/git/refs/heads/:ref

        StringBuilder uri = new StringBuilder();
        uri.append(SEGMENT_REPOS);
        uri.append('/').append(repository.generateId());
        uri.append(SEGMENT_GIT);
        uri.append(SEGMENT_REFS);
        uri.append(SEGMENT_HEADS);
        uri.append('/').append(ref);
        client.delete(uri.toString());
    }
}
